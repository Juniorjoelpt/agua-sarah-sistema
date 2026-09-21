package com.aguasarah.terceiros.frota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AbrirCarregamentoTerceiroRequestDTO(
        @NotNull Long caminhaoId,
        @NotNull Long produtoId,
        @NotBlank String rota,
        @NotNull @Positive Integer quantidadeCarregada,
        @NotNull @Positive BigDecimal precoVenda
) {}
