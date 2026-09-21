package com.aguasarah.terceiros.orcamento;

import jakarta.validation.constraints.NotNull;

public record AtualizarStatusOrcamentoTerceiroRequestDTO(@NotNull StatusOrcamentoTerceiro status) {}
