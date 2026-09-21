package com.aguasarah.terceiros.venda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VendaTerceiroRepository extends JpaRepository<VendaTerceiro, Long> {
    List<VendaTerceiro> findByCaixaTerceiroIdOrderByDataHoraDesc(Long caixaTerceiroId);
    List<VendaTerceiro> findByClienteIdOrderByDataHoraDesc(Long clienteId);
    List<VendaTerceiro> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}
