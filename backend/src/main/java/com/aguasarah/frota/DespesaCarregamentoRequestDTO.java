package com.aguasarah.frota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DespesaCarregamentoRequestDTO(@NotBlank String descricao, @NotNull BigDecimal valor) {}
