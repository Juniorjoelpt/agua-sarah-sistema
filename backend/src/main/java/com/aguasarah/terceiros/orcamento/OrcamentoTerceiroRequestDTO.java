package com.aguasarah.terceiros.orcamento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record OrcamentoTerceiroRequestDTO(
        @NotNull Long clienteId,
        LocalDate validoAte,
        String observacoes,
        @NotEmpty @Valid List<ItemOrcamentoTerceiroRequestDTO> itens
) {
    public record ItemOrcamentoTerceiroRequestDTO(@NotNull Long produtoId, @NotNull Integer quantidade) {}
}
