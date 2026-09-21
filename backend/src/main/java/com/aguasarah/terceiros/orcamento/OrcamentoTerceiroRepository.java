package com.aguasarah.terceiros.orcamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoTerceiroRepository extends JpaRepository<OrcamentoTerceiro, Long> {
    List<OrcamentoTerceiro> findAllByOrderByDataCriacaoDesc();
    List<OrcamentoTerceiro> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);
}
