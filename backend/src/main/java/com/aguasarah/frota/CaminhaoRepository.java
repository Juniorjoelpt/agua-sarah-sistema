package com.aguasarah.frota;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaminhaoRepository extends JpaRepository<Caminhao, Long> {
    List<Caminhao> findByAtivoTrue();
}
