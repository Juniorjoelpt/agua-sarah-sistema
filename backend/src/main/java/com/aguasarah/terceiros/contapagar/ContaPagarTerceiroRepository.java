package com.aguasarah.terceiros.contapagar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ContaPagarTerceiroRepository extends JpaRepository<ContaPagarTerceiro, Long> {
    List<ContaPagarTerceiro> findByStatusInOrderByDataVencimentoAsc(List<StatusContaPagarTerceiro> status);
    List<ContaPagarTerceiro> findAllByOrderByDataVencimentoAsc();
    List<ContaPagarTerceiro> findByStatusInAndDataVencimentoBeforeOrderByDataVencimentoAsc(List<StatusContaPagarTerceiro> status, LocalDate data);
}
