package com.aguasarah.terceiros.despesa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DespesaTerceiroRepository extends JpaRepository<DespesaTerceiro, Long> {
    List<DespesaTerceiro> findByCaixaTerceiroId(Long caixaTerceiroId);
    List<DespesaTerceiro> findByDataBetween(LocalDate inicio, LocalDate fim);
    List<DespesaTerceiro> findByCarregamentoId(Long carregamentoId);
}
