package com.aguasarah.terceiros.frota;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaminhaoTerceiroRepository extends JpaRepository<CaminhaoTerceiro, Long> {
    List<CaminhaoTerceiro> findByAtivoTrue();
}
