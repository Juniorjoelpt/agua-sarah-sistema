package com.aguasarah.fornecedor;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record FornecedorRequestDTO(
        @NotBlank String nome,
        String nomeContato,
        String telefone,
        String endereco,
        boolean ativo,
        List<Long> produtoIds,
        List<Long> insumoIds
) {}
