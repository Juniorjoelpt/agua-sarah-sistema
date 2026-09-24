package com.aguasarah.caixa;

import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.contareceber.PagamentoContaReceber;
import com.aguasarah.contareceber.PagamentoContaReceberRepository;
import com.aguasarah.despesa.Despesa;
import com.aguasarah.despesa.DespesaRepository;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import com.aguasarah.venda.Venda;
import com.aguasarah.venda.VendaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VendaRepository vendaRepository;
    private final DespesaRepository despesaRepository;
    private final PagamentoContaReceberRepository pagamentoContaReceberRepository;

    public Caixa abrir(AbrirCaixaRequestDTO dto) {
        caixaRepository.findFirstByStatusOrderByDataAberturaDesc(StatusCaixa.ABERTO)
                .ifPresent(c -> { throw new RegraNegocioException("Ja existe um caixa aberto (id " + c.getId() + ")"); });

        Caixa caixa = Caixa.builder()
                .dataAbertura(LocalDateTime.now())
                .usuarioAbertura(usuarioLogado())
                .saldoInicialEspecie(dto.saldoInicialEspecie())
                .saldoInicialPix(dto.saldoInicialPix())
                .status(StatusCaixa.ABERTO)
                .build();
        return caixaRepository.save(caixa);
    }

    public ResumoCaixaDTO fechar(Long id) {
        Caixa caixa = buscarPorId(id);
        if (caixa.getStatus() == StatusCaixa.FECHADO) {
            throw new RegraNegocioException("Este caixa ja esta fechado");
        }
        caixa.setStatus(StatusCaixa.FECHADO);
        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setUsuarioFechamento(usuarioLogado());
        caixaRepository.save(caixa);
        return calcularResumo(id);
    }

    public Caixa buscarAberto() {
        return caixaRepository.findFirstByStatusOrderByDataAberturaDesc(StatusCaixa.ABERTO)
                .orElseThrow(() -> new RegraNegocioException("Nao ha nenhum caixa aberto no momento"));
    }

    public Caixa buscarPorId(Long id) {
        return caixaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Caixa nao encontrado: " + id));
    }

    public List<Caixa> listarHistorico(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null) {
            return caixaRepository.findByDataAberturaBetweenOrderByDataAberturaDesc(inicio.atStartOfDay(), fim.atTime(23, 59, 59));
        }
        return caixaRepository.findAllByOrderByDataAberturaDesc();
    }

    // usado tanto pelo historico simples da tela de Caixa quanto pelo
    // relatorio de Relatorios (mesma logica, um resumo calculado por caixa do periodo)
    public List<ResumoCaixaDTO> listarResumosPorPeriodo(LocalDate inicio, LocalDate fim) {
        return listarHistorico(inicio, fim).stream()
                .map(c -> calcularResumo(c.getId()))
                .toList();
    }

    public ResumoCaixaDTO calcularResumo(Long caixaId) {
        Caixa caixa = buscarPorId(caixaId);
        List<Venda> vendas = vendaRepository.findByCaixaId(caixaId);
        List<Despesa> despesas = despesaRepository.findByCaixaId(caixaId);
        List<PagamentoContaReceber> recebimentos = pagamentoContaReceberRepository.findByCaixaId(caixaId);

        BigDecimal totalEspecie = somaCampoVendas(vendas, Venda::getValorRecebidoEspecie);
        BigDecimal totalPix = somaCampoVendas(vendas, Venda::getValorRecebidoPix);
        BigDecimal totalDespesas = despesas.stream().map(Despesa::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalGaloesBonificados = vendas.stream()
                .mapToInt(v -> v.getQuantidadeBonificados() != null ? v.getQuantidadeBonificados() : 0)
                .sum();

        // recebimento de divida (fiado) e dinheiro de verdade entrando no caixa, mesmo nao sendo uma venda do dia
        BigDecimal totalRecebimentosEspecie = recebimentos.stream()
                .map(PagamentoContaReceber::getValorEspecie).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRecebimentosPix = recebimentos.stream()
                .map(PagamentoContaReceber::getValorPix).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoFinalEspecie = caixa.getSaldoInicialEspecie().add(totalEspecie).add(totalRecebimentosEspecie).subtract(totalDespesas);
        BigDecimal saldoFinalPix = caixa.getSaldoInicialPix().add(totalPix).add(totalRecebimentosPix);

        return new ResumoCaixaDTO(
                caixa.getId(), caixa.getStatus(),
                caixa.getDataAbertura(), caixa.getDataFechamento(),
                caixa.getSaldoInicialEspecie(), caixa.getSaldoInicialPix(),
                totalEspecie, totalPix, totalDespesas, totalGaloesBonificados,
                totalRecebimentosEspecie, totalRecebimentosPix,
                saldoFinalEspecie, saldoFinalPix
        );
    }

    private BigDecimal somaCampoVendas(List<Venda> vendas, java.util.function.Function<Venda, BigDecimal> campo) {
        return vendas.stream()
                .map(campo)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Usuario usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
