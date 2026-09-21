package com.aguasarah.terceiros.contareceber;

import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.terceiros.caixa.CaixaTerceiro;
import com.aguasarah.terceiros.caixa.CaixaTerceiroRepository;
import com.aguasarah.terceiros.caixa.StatusCaixaTerceiro;
import com.aguasarah.terceiros.cliente.ClienteTerceiro;
import com.aguasarah.terceiros.cliente.ClienteTerceiroRepository;
import com.aguasarah.terceiros.venda.VendaTerceiro;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContaReceberTerceiroService {

    private final ContaReceberTerceiroRepository contaReceberTerceiroRepository;
    private final PagamentoContaReceberTerceiroRepository pagamentoRepository;
    private final ClienteTerceiroRepository clienteTerceiroRepository;
    private final CaixaTerceiroRepository caixaTerceiroRepository;
    private final UsuarioRepository usuarioRepository;

    // nasce automaticamente quando uma venda de terceiros tem parte fiado
    @Transactional
    public ContaReceberTerceiro abrirPorVenda(ClienteTerceiro cliente, VendaTerceiro venda, BigDecimal valorFiado) {
        ContaReceberTerceiro conta = ContaReceberTerceiro.builder()
                .cliente(cliente)
                .venda(venda)
                .descricao("Venda #" + venda.getId())
                .valorOriginal(valorFiado)
                .valorPago(BigDecimal.ZERO)
                .status(StatusContaReceberTerceiro.ABERTA)
                .dataCriacao(LocalDate.now())
                .build();
        return contaReceberTerceiroRepository.save(conta);
    }

    @Transactional
    public ContaReceberTerceiro criarManual(ContaReceberTerceiroRequestDTO dto) {
        ClienteTerceiro cliente = clienteTerceiroRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente terceiro nao encontrado: " + dto.clienteId()));

        ContaReceberTerceiro conta = ContaReceberTerceiro.builder()
                .cliente(cliente)
                .descricao(dto.descricao())
                .valorOriginal(dto.valorOriginal())
                .valorPago(BigDecimal.ZERO)
                .status(StatusContaReceberTerceiro.ABERTA)
                .dataCriacao(LocalDate.now())
                .dataVencimento(dto.dataVencimento())
                .build();
        return contaReceberTerceiroRepository.save(conta);
    }

    public List<ContaReceberTerceiro> listar(Long clienteId, boolean somenteEmAberto) {
        if (clienteId != null) {
            return contaReceberTerceiroRepository.findByClienteIdOrderByDataCriacaoDesc(clienteId);
        }
        if (somenteEmAberto) {
            return contaReceberTerceiroRepository.findByStatusInOrderByDataCriacaoDesc(
                    List.of(StatusContaReceberTerceiro.ABERTA, StatusContaReceberTerceiro.PARCIAL));
        }
        return contaReceberTerceiroRepository.findAllByOrderByDataCriacaoDesc();
    }

    public ContaReceberTerceiro buscarPorId(Long id) {
        return contaReceberTerceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta a receber de terceiros nao encontrada: " + id));
    }

    @Transactional
    public PagamentoContaReceberTerceiro registrarPagamento(Long contaId, PagamentoContaReceberTerceiroRequestDTO dto) {
        ContaReceberTerceiro conta = buscarPorId(contaId);

        if (conta.getStatus() == StatusContaReceberTerceiro.PAGA) {
            throw new RegraNegocioException("Esta conta ja esta totalmente paga");
        }

        BigDecimal valorEspecie = dto.valorEspecie() != null ? dto.valorEspecie() : BigDecimal.ZERO;
        BigDecimal valorPix = dto.valorPix() != null ? dto.valorPix() : BigDecimal.ZERO;
        BigDecimal valorPagamento = valorEspecie.add(valorPix);

        if (valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Informe um valor de pagamento maior que zero");
        }

        BigDecimal saldoDevedor = conta.getValorOriginal().subtract(conta.getValorPago());
        if (valorPagamento.compareTo(saldoDevedor.add(new BigDecimal("0.01"))) > 0) {
            throw new RegraNegocioException("O valor do pagamento (" + valorPagamento + ") é maior que o saldo devedor (" + saldoDevedor + ")");
        }

        CaixaTerceiro caixaAberto = caixaTerceiroRepository.findFirstByStatusOrderByDataAberturaDesc(StatusCaixaTerceiro.ABERTO)
                .orElseThrow(() -> new RegraNegocioException("Nenhum caixa de terceiros aberto - abra o caixa para registrar o recebimento"));

        PagamentoContaReceberTerceiro pagamento = PagamentoContaReceberTerceiro.builder()
                .contaReceber(conta)
                .caixaTerceiro(caixaAberto)
                .valorEspecie(valorEspecie)
                .valorPix(valorPix)
                .data(LocalDateTime.now())
                .usuario(usuarioLogado())
                .build();
        pagamento = pagamentoRepository.save(pagamento);

        BigDecimal novoValorPago = conta.getValorPago().add(valorPagamento);
        conta.setValorPago(novoValorPago);
        conta.setStatus(
                novoValorPago.compareTo(conta.getValorOriginal()) >= 0 ? StatusContaReceberTerceiro.PAGA
                        : novoValorPago.compareTo(BigDecimal.ZERO) > 0 ? StatusContaReceberTerceiro.PARCIAL
                        : StatusContaReceberTerceiro.ABERTA
        );
        contaReceberTerceiroRepository.save(conta);

        return pagamento;
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
