package com.aguasarah.terceiros.venda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record VendaTerceiroRequestDTO(
        @NotNull Long clienteId,
        String placaCaminhao,
        // os tres juntos precisam somar o valorTotal calculado a partir dos itens
        BigDecimal valorRecebidoEspecie,
        BigDecimal valorRecebidoPix,
        BigDecimal valorFiado, // vira uma ContaReceberTerceiro
        @NotEmpty @Valid List<ItemVendaTerceiroRequestDTO> itens
) {
    public record ItemVendaTerceiroRequestDTO(@NotNull Long produtoId, @NotNull Integer quantidade) {}
}
