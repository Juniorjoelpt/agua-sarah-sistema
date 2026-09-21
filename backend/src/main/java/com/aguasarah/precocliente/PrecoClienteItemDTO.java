package com.aguasarah.precocliente;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PrecoClienteItemDTO(@NotNull Long produtoId, @NotNull @Positive BigDecimal preco) {}
