package com.aguasarah.terceiros.contapagar;

import com.aguasarah.terceiros.despesa.CategoriaDespesaTerceiro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaPagarTerceiroRequestDTO(
        @NotBlank String descricao,
        @NotNull CategoriaDespesaTerceiro categoria,
        @NotNull @Positive BigDecimal valorOriginal,
        @NotNull LocalDate dataVencimento,
        String observacao
) {}
