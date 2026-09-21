package com.aguasarah.frota;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CarregamentoRepository extends JpaRepository<Carregamento, Long> {
    List<Carregamento> findByCaminhaoIdOrderByDataCarregamentoDesc(Long caminhaoId);
    List<Carregamento> findByStatus(StatusCarregamento status);
    List<Carregamento> findByDataCarregamentoBetween(LocalDate inicio, LocalDate fim);
}
