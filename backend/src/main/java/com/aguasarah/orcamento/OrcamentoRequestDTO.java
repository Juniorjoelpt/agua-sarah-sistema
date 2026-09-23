package com.aguasarah.orcamento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrcamentoRequestDTO(
        Long clienteId,               // se nulo, usa nomeClienteAvulso (validado no service)
        String nomeClienteAvulso,
        String telefoneClienteAvulso,
        LocalDate validoAte,
        String observacoes,
        @NotEmpty @Valid List<ItemOrcamentoRequestDTO> itens
) {
    public record ItemOrcamentoRequestDTO(
            @NotNull Long produtoId,
            @NotNull Integer quantidade,
            BigDecimal percentualDesconto // 0-100, opcional - desconto so deste item (validado no service)
    ) {}
}
