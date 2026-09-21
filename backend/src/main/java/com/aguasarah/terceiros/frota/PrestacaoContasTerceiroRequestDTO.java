package com.aguasarah.terceiros.frota;

import jakarta.validation.constraints.NotNull;

public record PrestacaoContasTerceiroRequestDTO(
        @NotNull Long carregamentoId,
        @NotNull Integer quantidadeAvaria,
        @NotNull Integer quantidadeDevolvida
) {}
