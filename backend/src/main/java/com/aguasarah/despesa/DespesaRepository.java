package com.aguasarah.despesa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    List<Despesa> findByCaixaId(Long caixaId);
    List<Despesa> findByDataBetween(LocalDate inicio, LocalDate fim);
    List<Despesa> findByCategoria(CategoriaDespesa categoria);
    List<Despesa> findByCarregamentoId(Long carregamentoId);
    List<Despesa> findByFornecedorIdOrderByDataDesc(Long fornecedorId);
}
