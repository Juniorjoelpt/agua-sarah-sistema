package com.aguasarah.terceiros.contapagar;

import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.terceiros.caixa.CaixaTerceiro;
import com.aguasarah.terceiros.caixa.CaixaTerceiroRepository;
import com.aguasarah.terceiros.despesa.DespesaTerceiro;
import com.aguasarah.terceiros.despesa.DespesaTerceiroRepository;
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
public class ContaPagarTerceiroService {

    private final ContaPagarTerceiroRepository contaPagarTerceiroRepository;
    private final DespesaTerceiroRepository despesaTerceiroRepository;
    private final CaixaTerceiroRepository caixaTerceiroRepository;
    private final UsuarioRepository usuarioRepository;

    public ContaPagarTerceiro criar(ContaPagarTerceiroRequestDTO dto) {
        ContaPagarTerceiro conta = ContaPagarTerceiro.builder()
                .descricao(dto.descricao())
                .categoria(dto.categoria())
                .valorOriginal(dto.valorOriginal())
                .valorPago(BigDecimal.ZERO)
                .status(StatusContaPagarTerceiro.ABERTA)
                .dataCriacao(LocalDate.now())
                .dataVencimento(dto.dataVencimento())
                .observacao(dto.observacao())
                .build();
        return contaPagarTerceiroRepository.save(conta);
    }

    public List<ContaPagarTerceiro> listar(boolean somenteEmAberto) {
        if (somenteEmAberto) {
            return contaPagarTerceiroRepository.findByStatusInOrderByDataVencimentoAsc(
                    List.of(StatusContaPagarTerceiro.ABERTA, StatusContaPagarTerceiro.PARCIAL));
        }
        return contaPagarTerceiroRepository.findAllByOrderByDataVencimentoAsc();
    }

    public List<ContaPagarTerceiro> listarVencidas() {
        return contaPagarTerceiroRepository.findByStatusInAndDataVencimentoBeforeOrderByDataVencimentoAsc(
                List.of(StatusContaPagarTerceiro.ABERTA, StatusContaPagarTerceiro.PARCIAL), LocalDate.now());
    }

    public ContaPagarTerceiro buscarPorId(Long id) {
        return contaPagarTerceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta a pagar de terceiros nao encontrada: " + id));
    }

    @Transactional
    public DespesaTerceiro registrarPagamento(Long contaId, PagamentoContaPagarTerceiroRequestDTO dto) {
        ContaPagarTerceiro conta = buscarPorId(contaId);

        if (conta.getStatus() == StatusContaPagarTerceiro.PAGA) {
            throw new RegraNegocioException("Esta conta ja esta totalmente paga");
        }

        BigDecimal saldoDevedor = conta.getValorOriginal().subtract(conta.getValorPago());
        if (dto.valor().compareTo(saldoDevedor.add(new BigDecimal("0.01"))) > 0) {
            throw new RegraNegocioException("O valor do pagamento (" + dto.valor() + ") é maior que o saldo devedor (" + saldoDevedor + ")");
        }

        CaixaTerceiro caixa = null;
        if (dto.caixaTerceiroId() != null) {
            caixa = caixaTerceiroRepository.findById(dto.caixaTerceiroId())
                    .orElseThrow(() -> new EntityNotFoundException("Caixa de terceiros nao encontrado: " + dto.caixaTerceiroId()));
        }

        DespesaTerceiro despesa = DespesaTerceiro.builder()
                .descricao(conta.getDescricao())
                .categoria(conta.getCategoria())
                .valor(dto.valor())
                .data(LocalDate.now())
                .caixaTerceiro(caixa)
                .contaPagar(conta)
                .usuario(usuarioLogado())
                .build();
        despesa = despesaTerceiroRepository.save(despesa);

        BigDecimal novoValorPago = conta.getValorPago().add(dto.valor());
        conta.setValorPago(novoValorPago);
        conta.setStatus(
                novoValorPago.compareTo(conta.getValorOriginal()) >= 0 ? StatusContaPagarTerceiro.PAGA
                        : novoValorPago.compareTo(BigDecimal.ZERO) > 0 ? StatusContaPagarTerceiro.PARCIAL
                        : StatusContaPagarTerceiro.ABERTA
        );
        contaPagarTerceiroRepository.save(conta);

        return despesa;
    }

    public void excluir(Long id) {
        ContaPagarTerceiro conta = buscarPorId(id);
        if (conta.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
            throw new RegraNegocioException("Não é possível excluir uma conta que já teve pagamento registrado");
        }
        contaPagarTerceiroRepository.delete(conta);
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
