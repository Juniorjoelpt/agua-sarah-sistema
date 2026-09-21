package com.aguasarah.estoque;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovimentacaoInsumoRequestDTO(
        @NotNull Long insumoId,
        @NotNull TipoMovimentacaoInsumo tipo,
        @NotNull @Positive Integer quantidade,
        Long fornecedorId, // opcional - quem entregou, so faz sentido em ENTRADA
        String observacao
) {}
