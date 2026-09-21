package com.aguasarah.terceiros.caixa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaixaTerceiroRepository extends JpaRepository<CaixaTerceiro, Long> {
    Optional<CaixaTerceiro> findFirstByStatusOrderByDataAberturaDesc(StatusCaixaTerceiro status);
}
