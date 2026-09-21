package com.aguasarah.orcamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    List<Orcamento> findAllByOrderByDataCriacaoDesc();
    List<Orcamento> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);
}
