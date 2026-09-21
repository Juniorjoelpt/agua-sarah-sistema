package com.aguasarah.terceiros.frota;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarregamentoTerceiroRepository extends JpaRepository<CarregamentoTerceiro, Long> {
    List<CarregamentoTerceiro> findByCaminhaoIdOrderByDataCarregamentoDesc(Long caminhaoId);
}
