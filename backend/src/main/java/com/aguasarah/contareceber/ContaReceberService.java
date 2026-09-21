package com.aguasarah.contareceber;

import com.aguasarah.caixa.Caixa;
import com.aguasarah.caixa.CaixaRepository;
import com.aguasarah.caixa.StatusCaixa;
import com.aguasarah.cliente.Cliente;
import com.aguasarah.cliente.ClienteRepository;
import com.aguasarah.common.RegraNegocioException;
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
public class ContaReceberService {

    private final ContaReceberRepository contaReceberRepository;
    private final PagamentoContaReceberRepository pagamentoRepository;
    private final ClienteRepository clienteRepository;
    private final CaixaRepository caixaRepository;
    private final UsuarioRepository usuarioRepository;

    // nasce automaticamente quando uma venda tem parte fiado - ver VendaService
    @Transactional
    public ContaReceber abrirPorVenda(Cliente cliente, com.aguasarah.venda.Venda venda, BigDecimal valorFiado) {
        ContaReceber conta = ContaReceber.builder()
                .cliente(cliente)
                .venda(venda)
                .descricao("Venda #" + venda.getId())
                .valorOriginal(valorFiado)
                .valorPago(BigDecimal.ZERO)
                .status(StatusContaReceber.ABERTA)
                .dataCriacao(LocalDate.now())
                .build();
        return contaReceberRepository.save(conta);
    }

    @Transactional
    public ContaReceber criarManual(ContaReceberRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado: " + dto.clienteId()));

        ContaReceber conta = ContaReceber.builder()
                .cliente(cliente)
                .descricao(dto.descricao())
                .valorOriginal(dto.valorOriginal())
                .valorPago(BigDecimal.ZERO)
                .status(StatusContaReceber.ABERTA)
                .dataCriacao(LocalDate.now())
                .dataVencimento(dto.dataVencimento())
                .build();
        return contaReceberRepository.save(conta);
    }

    public List<ContaReceber> listar(Long clienteId, boolean somenteEmAberto) {
        if (clienteId != null) {
            return contaReceberRepository.findByClienteIdOrderByDataCriacaoDesc(clienteId);
        }
        if (somenteEmAberto) {
            return contaReceberRepository.findByStatusInOrderByDataCriacaoDesc(
                    List.of(StatusContaReceber.ABERTA, StatusContaReceber.PARCIAL));
        }
        return contaReceberRepository.findAllByOrderByDataCriacaoDesc();
    }

    public ContaReceber buscarPorId(Long id) {
        return contaReceberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta a receber nao encontrada: " + id));
    }

    @Transactional
    public PagamentoContaReceber registrarPagamento(Long contaId, PagamentoContaReceberRequestDTO dto) {
        ContaReceber conta = buscarPorId(contaId);

        if (conta.getStatus() == StatusContaReceber.PAGA) {
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

        Caixa caixaAberto = caixaRepository.findFirstByStatusOrderByDataAberturaDesc(StatusCaixa.ABERTO)
                .orElseThrow(() -> new RegraNegocioException("Nenhum caixa aberto - abra o caixa para registrar o recebimento"));

        PagamentoContaReceber pagamento = PagamentoContaReceber.builder()
                .contaReceber(conta)
                .caixa(caixaAberto)
                .valorEspecie(valorEspecie)
                .valorPix(valorPix)
                .data(LocalDateTime.now())
                .usuario(usuarioLogado())
                .build();
        pagamento = pagamentoRepository.save(pagamento);

        BigDecimal novoValorPago = conta.getValorPago().add(valorPagamento);
        conta.setValorPago(novoValorPago);
        conta.setStatus(
                novoValorPago.compareTo(conta.getValorOriginal()) >= 0 ? StatusContaReceber.PAGA
                        : novoValorPago.compareTo(BigDecimal.ZERO) > 0 ? StatusContaReceber.PARCIAL
                        : StatusContaReceber.ABERTA
        );
        contaReceberRepository.save(conta);

        return pagamento;
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
