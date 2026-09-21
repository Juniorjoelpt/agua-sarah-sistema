package com.aguasarah.frota;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrestacaoContasRepository extends JpaRepository<PrestacaoContas, Long> {
    Optional<PrestacaoContas> findByCarregamentoId(Long carregamentoId);
    List<PrestacaoContas> findAllByOrderByDataPrestacaoDesc();
    List<PrestacaoContas> findByDataPrestacaoBetweenOrderByDataPrestacaoDesc(LocalDateTime inicio, LocalDateTime fim);
    List<PrestacaoContas> findByCarregamento_CaminhaoIdOrderByDataPrestacaoDesc(Long caminhaoId);
    List<PrestacaoContas> findByCarregamento_CaminhaoIdAndDataPrestacaoBetweenOrderByDataPrestacaoDesc(Long caminhaoId, LocalDateTime inicio, LocalDateTime fim);
    // avarias de carregamento no periodo, pela data do carregamento (mesmo criterio usado pra somar quantidade carregada)
    List<PrestacaoContas> findByCarregamento_DataCarregamentoBetween(LocalDate inicio, LocalDate fim);
}
