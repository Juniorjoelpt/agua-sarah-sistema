package com.aguasarah.frota;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PrestacaoContasRequestDTO(
        @NotNull Long carregamentoId,
        @NotNull Integer quantidadeAvaria,
        @NotNull Integer quantidadeDevolvida,
        @NotNull BigDecimal valorRecebidoEspecie,
        @NotNull BigDecimal valorRecebidoPix
) {}
