package com.aguasarah.contareceber;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long> {
    List<ContaReceber> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);
    List<ContaReceber> findByStatusOrderByDataCriacaoDesc(StatusContaReceber status);
    List<ContaReceber> findAllByOrderByDataCriacaoDesc();
    List<ContaReceber> findByStatusInOrderByDataCriacaoDesc(List<StatusContaReceber> status);
}
