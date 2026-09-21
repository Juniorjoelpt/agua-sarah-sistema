package com.aguasarah.venda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    List<Venda> findByCaixaId(Long caixaId);
    List<Venda> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Venda> findByClienteIdOrderByDataHoraDesc(Long clienteId);
}
