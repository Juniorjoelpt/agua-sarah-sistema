package com.aguasarah.precocliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrecoClienteRepository extends JpaRepository<PrecoCliente, Long> {
    List<PrecoCliente> findByClienteId(Long clienteId);
    Optional<PrecoCliente> findByClienteIdAndProdutoId(Long clienteId, Long produtoId);
    void deleteByClienteId(Long clienteId);
}
