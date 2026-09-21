package com.aguasarah.contapagar;

import com.aguasarah.despesa.CategoriaDespesa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaPagarRequestDTO(
        Long fornecedorId, // opcional
        @NotBlank String descricao,
        @NotNull CategoriaDespesa categoria,
        @NotNull @Positive BigDecimal valorOriginal,
        @NotNull LocalDate dataVencimento,
        String observacao
) {}
