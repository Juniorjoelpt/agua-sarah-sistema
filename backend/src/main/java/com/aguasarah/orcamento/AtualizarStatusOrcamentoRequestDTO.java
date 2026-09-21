package com.aguasarah.orcamento;

import jakarta.validation.constraints.NotNull;

public record AtualizarStatusOrcamentoRequestDTO(@NotNull StatusOrcamento status) {}
