package com.aguasarah.terceiros.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteTerceiroRepository extends JpaRepository<ClienteTerceiro, Long> {
    List<ClienteTerceiro> findByAtivoTrue();
}
