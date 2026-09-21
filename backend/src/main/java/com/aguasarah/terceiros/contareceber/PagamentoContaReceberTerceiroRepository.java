package com.aguasarah.terceiros.contareceber;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoContaReceberTerceiroRepository extends JpaRepository<PagamentoContaReceberTerceiro, Long> {
    List<PagamentoContaReceberTerceiro> findByCaixaTerceiroId(Long caixaTerceiroId);
}
