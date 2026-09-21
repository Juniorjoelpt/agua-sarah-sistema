package com.aguasarah.terceiros.cliente;

import jakarta.validation.constraints.NotBlank;

public record ClienteTerceiroRequestDTO(
        @NotBlank String nome,
        String telefone,
        String bairro,
        String endereco,
        boolean ativo
) {}
