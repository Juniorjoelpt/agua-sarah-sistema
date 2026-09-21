package com.aguasarah.contapagar;

import com.aguasarah.caixa.Caixa;
import com.aguasarah.caixa.CaixaRepository;
import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.despesa.Despesa;
import com.aguasarah.despesa.DespesaRepository;
import com.aguasarah.fornecedor.Fornecedor;
import com.aguasarah.fornecedor.FornecedorRepository;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContaPagarService {

    private final ContaPagarRepository contaPagarRepository;
    private final FornecedorRepository fornecedorRepository;
    private final DespesaRepository despesaRepository;
    private final CaixaRepository caixaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ContaPagar criar(ContaPagarRequestDTO dto) {
        Fornecedor fornecedor = dto.fornecedorId() != null
                ? fornecedorRepository.findById(dto.fornecedorId())
                        .orElseThrow(() -> new EntityNotFoundException("Fornecedor nao encontrado: " + dto.fornecedorId()))
                : null;

        ContaPagar conta = ContaPagar.builder()
                .fornecedor(fornecedor)
                .descricao(dto.descricao())
                .categoria(dto.categoria())
                .valorOriginal(dto.valorOriginal())
                .valorPago(BigDecimal.ZERO)
                .status(StatusContaPagar.ABERTA)
                .dataCriacao(LocalDate.now())
                .dataVencimento(dto.dataVencimento())
                .observacao(dto.observacao())
                .build();
        return contaPagarRepository.save(conta);
    }

    public List<ContaPagar> listar(Long fornecedorId, boolean somenteEmAberto) {
        if (fornecedorId != null) {
            return contaPagarRepository.findByFornecedorIdOrderByDataVencimentoAsc(fornecedorId);
        }
        if (somenteEmAberto) {
            return contaPagarRepository.findByStatusInOrderByDataVencimentoAsc(
                    List.of(StatusContaPagar.ABERTA, StatusContaPagar.PARCIAL));
        }
        return contaPagarRepository.findAllByOrderByDataVencimentoAsc();
    }

    // usado no Painel para o alerta de contas vencidas
    public List<ContaPagar> listarVencidas() {
        return contaPagarRepository.findByStatusInAndDataVencimentoBeforeOrderByDataVencimentoAsc(
                List.of(StatusContaPagar.ABERTA, StatusContaPagar.PARCIAL), LocalDate.now());
    }

    public ContaPagar buscarPorId(Long id) {
        return contaPagarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta a pagar nao encontrada: " + id));
    }

    @Transactional
    public Despesa registrarPagamento(Long contaId, PagamentoContaPagarRequestDTO dto) {
        ContaPagar conta = buscarPorId(contaId);

        if (conta.getStatus() == StatusContaPagar.PAGA) {
            throw new RegraNegocioException("Esta conta ja esta totalmente paga");
        }

        BigDecimal saldoDevedor = conta.getValorOriginal().subtract(conta.getValorPago());
        if (dto.valor().compareTo(saldoDevedor.add(new BigDecimal("0.01"))) > 0) {
            throw new RegraNegocioException("O valor do pagamento (" + dto.valor() + ") é maior que o saldo devedor (" + saldoDevedor + ")");
        }

        Caixa caixa = null;
        if (dto.caixaId() != null) {
            caixa = caixaRepository.findById(dto.caixaId())
                    .orElseThrow(() -> new EntityNotFoundException("Caixa nao encontrado: " + dto.caixaId()));
        }

        Despesa despesa = Despesa.builder()
                .descricao(conta.getDescricao())
                .categoria(conta.getCategoria())
                .valor(dto.valor())
                .data(LocalDate.now())
                .caixa(caixa)
                .fornecedor(conta.getFornecedor())
                .contaPagar(conta)
                .usuario(usuarioLogado())
                .build();
        despesa = despesaRepository.save(despesa);

        BigDecimal novoValorPago = conta.getValorPago().add(dto.valor());
        conta.setValorPago(novoValorPago);
        conta.setStatus(
                novoValorPago.compareTo(conta.getValorOriginal()) >= 0 ? StatusContaPagar.PAGA
                        : novoValorPago.compareTo(BigDecimal.ZERO) > 0 ? StatusContaPagar.PARCIAL
                        : StatusContaPagar.ABERTA
        );
        contaPagarRepository.save(conta);

        return despesa;
    }

    public void excluir(Long id) {
        ContaPagar conta = buscarPorId(id);
        if (conta.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
            throw new RegraNegocioException("Não é possível excluir uma conta que já teve pagamento registrado");
        }
        contaPagarRepository.delete(conta);
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
