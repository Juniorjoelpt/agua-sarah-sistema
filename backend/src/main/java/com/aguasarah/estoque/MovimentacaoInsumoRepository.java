package com.aguasarah.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoInsumoRepository extends JpaRepository<MovimentacaoInsumo, Long> {
    List<MovimentacaoInsumo> findByInsumoIdOrderByDataDesc(Long insumoId);
    List<MovimentacaoInsumo> findByFornecedorIdOrderByDataDesc(Long fornecedorId);
}
