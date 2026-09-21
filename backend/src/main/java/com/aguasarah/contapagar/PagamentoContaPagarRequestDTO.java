package com.aguasarah.contapagar;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PagamentoContaPagarRequestDTO(
        @NotNull @Positive BigDecimal valor,
        Long caixaId // opcional - so preenche se o pagamento saiu do caixa fisico do dia
) {}
