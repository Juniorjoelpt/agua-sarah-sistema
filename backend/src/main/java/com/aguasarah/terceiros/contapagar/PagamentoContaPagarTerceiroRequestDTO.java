package com.aguasarah.terceiros.contapagar;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PagamentoContaPagarTerceiroRequestDTO(
        @NotNull @Positive BigDecimal valor,
        Long caixaTerceiroId // opcional - so preenche se saiu do caixa fisico de terceiros
) {}
