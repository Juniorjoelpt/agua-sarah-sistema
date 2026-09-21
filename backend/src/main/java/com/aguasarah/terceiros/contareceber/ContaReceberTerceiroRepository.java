package com.aguasarah.terceiros.contareceber;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaReceberTerceiroRepository extends JpaRepository<ContaReceberTerceiro, Long> {
    List<ContaReceberTerceiro> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);
    List<ContaReceberTerceiro> findAllByOrderByDataCriacaoDesc();
    List<ContaReceberTerceiro> findByStatusInOrderByDataCriacaoDesc(List<StatusContaReceberTerceiro> status);
}
