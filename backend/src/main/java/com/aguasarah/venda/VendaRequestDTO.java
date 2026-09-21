package com.aguasarah.venda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record VendaRequestDTO(
        Long clienteId,
        // os tres juntos precisam somar o valorTotal da venda (validado no service);
        // uma venda so em PIX manda os outros dois como 0 (ou null)
        BigDecimal valorRecebidoEspecie,
        BigDecimal valorRecebidoPix,
        BigDecimal valorFiado, // vira uma ContaReceber - exige cliente cadastrado (validado no service)
        @NotNull Ocorrencia ocorrencia,
        Integer quantidadeAvarias, // obrigatorio sempre que ocorrencia != NENHUMA (validado no service)
        Integer quantidadeBonificados, // obrigatorio so quando ocorrencia = AVARIA_PRODUCAO - independente da quantidadeAvarias
        String observacao, // texto livre, opcional
        @NotEmpty @Valid List<ItemVendaRequestDTO> itens
) {
    public record ItemVendaRequestDTO(@NotNull Long produtoId, @NotNull Integer quantidade) {}
}
