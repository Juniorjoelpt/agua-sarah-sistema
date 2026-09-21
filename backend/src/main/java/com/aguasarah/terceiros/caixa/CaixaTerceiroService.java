package com.aguasarah.terceiros.caixa;

import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.terceiros.contareceber.PagamentoContaReceberTerceiro;
import com.aguasarah.terceiros.contareceber.PagamentoContaReceberTerceiroRepository;
import com.aguasarah.terceiros.despesa.DespesaTerceiro;
import com.aguasarah.terceiros.despesa.DespesaTerceiroRepository;
import com.aguasarah.terceiros.frota.PrestacaoContasTerceiro;
import com.aguasarah.terceiros.frota.PrestacaoContasTerceiroRepository;
import com.aguasarah.terceiros.venda.VendaTerceiro;
import com.aguasarah.terceiros.venda.VendaTerceiroRepository;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaixaTerceiroService {

    private final CaixaTerceiroRepository caixaTerceiroRepository;
    private final VendaTerceiroRepository vendaTerceiroRepository;
    private final DespesaTerceiroRepository despesaTerceiroRepository;
    private final PagamentoContaReceberTerceiroRepository pagamentoContaReceberTerceiroRepository;
    private final PrestacaoContasTerceiroRepository prestacaoContasTerceiroRepository;
    private final UsuarioRepository usuarioRepository;

    public CaixaTerceiro abrirCaixa(AbrirCaixaTerceiroRequestDTO dto) {
        caixaTerceiroRepository.findFirstByStatusOrderByDataAberturaDesc(StatusCaixaTerceiro.ABERTO)
                .ifPresent(c -> { throw new RegraNegocioException("Ja existe um caixa de terceiros aberto (id " + c.getId() + ")"); });

        CaixaTerceiro caixa = CaixaTerceiro.builder()
                .dataAbertura(LocalDateTime.now())
                .saldoInicialEspecie(dto.saldoInicialEspecie() != null ? dto.saldoInicialEspecie() : BigDecimal.ZERO)
                .saldoInicialPix(dto.saldoInicialPix() != null ? dto.saldoInicialPix() : BigDecimal.ZERO)
                .status(StatusCaixaTerceiro.ABERTO)
                .usuarioAbertura(usuarioLogado())
                .build();
        return caixaTerceiroRepository.save(caixa);
    }

    public CaixaTerceiro fecharCaixa(Long id) {
        CaixaTerceiro caixa = buscarPorId(id);
        caixa.setStatus(StatusCaixaTerceiro.FECHADO);
        caixa.setDataFechamento(LocalDateTime.now());
        return caixaTerceiroRepository.save(caixa);
    }

    public CaixaTerceiro buscarCaixaAberto() {
        return caixaTerceiroRepository.findFirstByStatusOrderByDataAberturaDesc(StatusCaixaTerceiro.ABERTO)
                .orElseThrow(() -> new RegraNegocioException("Nao ha caixa de terceiros aberto no momento"));
    }

    public CaixaTerceiro buscarPorId(Long id) {
        return caixaTerceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Caixa de terceiros nao encontrado: " + id));
    }

    public ResumoCaixaTerceiroDTO calcularResumo(Long caixaId) {
        CaixaTerceiro caixa = buscarPorId(caixaId);
        List<VendaTerceiro> vendas = vendaTerceiroRepository.findByCaixaTerceiroIdOrderByDataHoraDesc(caixaId);
        List<DespesaTerceiro> despesas = despesaTerceiroRepository.findByCaixaTerceiroId(caixaId);
        List<PagamentoContaReceberTerceiro> recebimentos = pagamentoContaReceberTerceiroRepository.findByCaixaTerceiroId(caixaId);
        List<PrestacaoContasTerceiro> prestacoesFrota = prestacaoContasTerceiroRepository.findByCaixaTerceiroId(caixaId);

        BigDecimal totalEspecie = vendas.stream()
                .map(VendaTerceiro::getValorRecebidoEspecie).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPix = vendas.stream()
                .map(VendaTerceiro::getValorRecebidoPix).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDespesas = despesas.stream()
                .map(DespesaTerceiro::getValor).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRecebimentosEspecie = recebimentos.stream()
                .map(PagamentoContaReceberTerceiro::getValorEspecie).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRecebimentosPix = recebimentos.stream()
                .map(PagamentoContaReceberTerceiro::getValorPix).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        // note: o valor do carregamento em si ja entrou em totalDespesas (foi lancado como
        // despesa na hora de carregar) - aqui so soma o lucro que a prestacao de contas devolve
        BigDecimal totalLucroFrota = prestacoesFrota.stream()
                .map(PrestacaoContasTerceiro::getLucro).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoFinalEspecie = caixa.getSaldoInicialEspecie().add(totalEspecie).add(totalRecebimentosEspecie).add(totalLucroFrota).subtract(totalDespesas);
        BigDecimal saldoFinalPix = caixa.getSaldoInicialPix().add(totalPix).add(totalRecebimentosPix);

        return new ResumoCaixaTerceiroDTO(
                caixa.getId(), caixa.getStatus(), caixa.getDataAbertura(), caixa.getDataFechamento(),
                caixa.getSaldoInicialEspecie(), caixa.getSaldoInicialPix(),
                totalEspecie, totalPix, totalDespesas,
                totalRecebimentosEspecie, totalRecebimentosPix, totalLucroFrota,
                saldoFinalEspecie, saldoFinalPix
        );
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
