package com.aguasarah.terceiros.produto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoTerceiroRepository extends JpaRepository<ProdutoTerceiro, Long> {
    List<ProdutoTerceiro> findByAtivoTrue();
}
