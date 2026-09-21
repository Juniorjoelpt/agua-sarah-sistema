package com.aguasarah.fluxocaixa;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record LancamentoFluxoCaixaRequestDTO(
        @NotNull Long contaBancariaId,
        @NotNull TipoLancamento tipo,
        String descricao,
        String categoria,
        @NotNull @Positive java.math.BigDecimal valor,
        @NotNull LocalDate data
) {}
