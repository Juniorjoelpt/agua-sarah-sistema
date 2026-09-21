package com.aguasarah.terceiros.frota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DespesaCarregamentoTerceiroRequestDTO(@NotBlank String descricao, @NotNull BigDecimal valor) {}
