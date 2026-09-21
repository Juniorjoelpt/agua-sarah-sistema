package com.aguasarah.contareceber;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PagamentoContaReceberRepository extends JpaRepository<PagamentoContaReceber, Long> {
    List<PagamentoContaReceber> findByCaixaId(Long caixaId);
    List<PagamentoContaReceber> findByDataBetween(LocalDateTime inicio, LocalDateTime fim);
}
