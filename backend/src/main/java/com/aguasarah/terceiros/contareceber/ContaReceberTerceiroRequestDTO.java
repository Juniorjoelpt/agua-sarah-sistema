package com.aguasarah.terceiros.contareceber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ContaReceberTerceiroRequestDTO(
        @NotNull Long clienteId,
        @NotBlank String descricao,
        @NotNull @Positive java.math.BigDecimal valorOriginal,
        LocalDate dataVencimento
) {}
