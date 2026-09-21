package com.aguasarah.caixa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaixaRepository extends JpaRepository<Caixa, Long> {
    Optional<Caixa> findFirstByStatusOrderByDataAberturaDesc(StatusCaixa status);
    List<Caixa> findByStatusOrderByDataAberturaDesc(StatusCaixa status);
    List<Caixa> findAllByOrderByDataAberturaDesc();
    List<Caixa> findByDataAberturaBetweenOrderByDataAberturaDesc(LocalDateTime inicio, LocalDateTime fim);
}
