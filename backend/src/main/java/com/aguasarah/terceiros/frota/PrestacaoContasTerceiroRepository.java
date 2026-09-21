package com.aguasarah.terceiros.frota;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrestacaoContasTerceiroRepository extends JpaRepository<PrestacaoContasTerceiro, Long> {
    Optional<PrestacaoContasTerceiro> findByCarregamentoId(Long carregamentoId);
    List<PrestacaoContasTerceiro> findByCaixaTerceiroId(Long caixaTerceiroId);
    List<PrestacaoContasTerceiro> findAllByOrderByDataPrestacaoDesc();
    List<PrestacaoContasTerceiro> findByDataPrestacaoBetweenOrderByDataPrestacaoDesc(LocalDateTime inicio, LocalDateTime fim);
    List<PrestacaoContasTerceiro> findByCarregamento_CaminhaoIdOrderByDataPrestacaoDesc(Long caminhaoId);
    List<PrestacaoContasTerceiro> findByCarregamento_CaminhaoIdAndDataPrestacaoBetweenOrderByDataPrestacaoDesc(Long caminhaoId, LocalDateTime inicio, LocalDateTime fim);
}
