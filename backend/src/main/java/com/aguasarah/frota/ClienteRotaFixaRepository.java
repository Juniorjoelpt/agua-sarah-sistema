package com.aguasarah.frota;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRotaFixaRepository extends JpaRepository<ClienteRotaFixa, Long> {
    List<ClienteRotaFixa> findByCaminhaoId(Long caminhaoId);
}
