package com.aguasarah.fluxocaixa;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ContaBancariaRequestDTO(
        @NotBlank String apelido,
        String banco,
        String agencia,
        String numeroConta,
        String chavePix,
        BigDecimal saldoInicial,
        boolean ativa
) {}
