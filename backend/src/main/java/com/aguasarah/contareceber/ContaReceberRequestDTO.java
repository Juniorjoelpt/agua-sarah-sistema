package com.aguasarah.contareceber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

// usado para lancar uma divida manualmente (nao originada de uma venda) -
// ex: divida antiga do cliente que precisa entrar no controle
public record ContaReceberRequestDTO(
        @NotNull Long clienteId,
        @NotBlank String descricao,
        @NotNull @Positive java.math.BigDecimal valorOriginal,
        LocalDate dataVencimento
) {}
