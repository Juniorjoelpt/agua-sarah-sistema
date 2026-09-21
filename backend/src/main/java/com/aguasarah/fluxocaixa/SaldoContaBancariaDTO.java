package com.aguasarah.fluxocaixa;

import java.math.BigDecimal;

public record SaldoContaBancariaDTO(
        Long contaBancariaId,
        BigDecimal saldoInicial,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldoAtual
) {}
