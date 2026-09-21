package com.aguasarah.terceiros.despesa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaTerceiroRequestDTO(
        @NotBlank String descricao,
        @NotNull CategoriaDespesaTerceiro categoria,
        @NotNull @Positive BigDecimal valor,
        @NotNull LocalDate data,
        Long caixaTerceiroId // opcional - se saiu do caixa de terceiros do dia
) {}
