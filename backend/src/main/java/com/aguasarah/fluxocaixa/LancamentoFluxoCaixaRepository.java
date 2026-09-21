package com.aguasarah.fluxocaixa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LancamentoFluxoCaixaRepository extends JpaRepository<LancamentoFluxoCaixa, Long> {
    List<LancamentoFluxoCaixa> findByContaBancariaIdOrderByDataDesc(Long contaBancariaId);
    List<LancamentoFluxoCaixa> findByContaBancariaIdAndDataBetweenOrderByDataDesc(Long contaBancariaId, LocalDate inicio, LocalDate fim);
    List<LancamentoFluxoCaixa> findByDataBetweenOrderByDataDesc(LocalDate inicio, LocalDate fim);
    Optional<LancamentoFluxoCaixa> findByIdTransacaoExterna(String idTransacaoExterna);
}
