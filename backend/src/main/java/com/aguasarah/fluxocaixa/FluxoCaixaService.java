package com.aguasarah.fluxocaixa;

import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FluxoCaixaService {

    private final ContaBancariaRepository contaBancariaRepository;
    private final LancamentoFluxoCaixaRepository lancamentoRepository;
    private final UsuarioRepository usuarioRepository;

    /* ---------- contas bancarias ---------- */

    public List<ContaBancaria> listarContas(boolean somenteAtivas) {
        return somenteAtivas ? contaBancariaRepository.findByAtivaTrue() : contaBancariaRepository.findAll();
    }

    public ContaBancaria criarConta(ContaBancariaRequestDTO dto) {
        ContaBancaria conta = ContaBancaria.builder()
                .apelido(dto.apelido())
                .banco(dto.banco())
                .agencia(dto.agencia())
                .numeroConta(dto.numeroConta())
                .chavePix(dto.chavePix())
                .saldoInicial(dto.saldoInicial() != null ? dto.saldoInicial() : BigDecimal.ZERO)
                .ativa(true)
                .build();
        return contaBancariaRepository.save(conta);
    }

    public ContaBancaria buscarConta(Long id) {
        return contaBancariaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta bancária não encontrada: " + id));
    }

    public ContaBancaria atualizarConta(Long id, ContaBancariaRequestDTO dto) {
        ContaBancaria conta = buscarConta(id);
        conta.setApelido(dto.apelido());
        conta.setBanco(dto.banco());
        conta.setAgencia(dto.agencia());
        conta.setNumeroConta(dto.numeroConta());
        conta.setChavePix(dto.chavePix());
        conta.setSaldoInicial(dto.saldoInicial() != null ? dto.saldoInicial() : BigDecimal.ZERO);
        conta.setAtiva(dto.ativa());
        return contaBancariaRepository.save(conta);
    }

    public void inativarConta(Long id) {
        ContaBancaria conta = buscarConta(id);
        conta.setAtiva(false);
        contaBancariaRepository.save(conta);
    }

    public SaldoContaBancariaDTO calcularSaldo(Long contaBancariaId) {
        ContaBancaria conta = buscarConta(contaBancariaId);
        List<LancamentoFluxoCaixa> lancamentos = lancamentoRepository.findByContaBancariaIdOrderByDataDesc(contaBancariaId);

        BigDecimal totalEntradas = lancamentos.stream()
                .filter(l -> l.getTipo() == TipoLancamento.ENTRADA)
                .map(LancamentoFluxoCaixa::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSaidas = lancamentos.stream()
                .filter(l -> l.getTipo() == TipoLancamento.SAIDA)
                .map(LancamentoFluxoCaixa::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoAtual = conta.getSaldoInicial().add(totalEntradas).subtract(totalSaidas);

        return new SaldoContaBancariaDTO(conta.getId(), conta.getSaldoInicial(), totalEntradas, totalSaidas, saldoAtual);
    }

    /* ---------- lancamentos ---------- */

    public LancamentoFluxoCaixa registrarLancamentoManual(LancamentoFluxoCaixaRequestDTO dto) {
        ContaBancaria conta = buscarConta(dto.contaBancariaId());

        LancamentoFluxoCaixa lancamento = LancamentoFluxoCaixa.builder()
                .contaBancaria(conta)
                .tipo(dto.tipo())
                .descricao(dto.descricao())
                .categoria(dto.categoria())
                .valor(dto.valor())
                .data(dto.data())
                .origem(OrigemLancamento.MANUAL)
                .usuario(usuarioLogado())
                .build();
        return lancamentoRepository.save(lancamento);
    }

    public List<LancamentoFluxoCaixa> listarLancamentos(Long contaBancariaId, LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null) {
            return lancamentoRepository.findByContaBancariaIdAndDataBetweenOrderByDataDesc(contaBancariaId, inicio, fim);
        }
        return lancamentoRepository.findByContaBancariaIdOrderByDataDesc(contaBancariaId);
    }

    public void excluirLancamento(Long id) {
        LancamentoFluxoCaixa lancamento = lancamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado: " + id));
        if (lancamento.getOrigem() == OrigemLancamento.AUTOMATICO) {
            throw new RegraNegocioException("Lançamentos importados automaticamente não podem ser excluídos manualmente");
        }
        lancamentoRepository.delete(lancamento);
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
