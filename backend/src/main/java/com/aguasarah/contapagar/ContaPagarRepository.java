package com.aguasarah.contapagar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ContaPagarRepository extends JpaRepository<ContaPagar, Long> {
    List<ContaPagar> findByFornecedorIdOrderByDataVencimentoAsc(Long fornecedorId);
    List<ContaPagar> findByStatusInOrderByDataVencimentoAsc(List<StatusContaPagar> status);
    List<ContaPagar> findAllByOrderByDataVencimentoAsc();
    List<ContaPagar> findByStatusInAndDataVencimentoBeforeOrderByDataVencimentoAsc(List<StatusContaPagar> status, LocalDate data);
}
