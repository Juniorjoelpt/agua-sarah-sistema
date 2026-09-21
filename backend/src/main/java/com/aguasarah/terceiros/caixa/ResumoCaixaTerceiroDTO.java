package com.aguasarah.terceiros.caixa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResumoCaixaTerceiroDTO(
        Long caixaId,
        StatusCaixaTerceiro status,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        BigDecimal saldoInicialEspecie,
        BigDecimal saldoInicialPix,
        BigDecimal totalVendasEspecie,
        BigDecimal totalVendasPix,
        BigDecimal totalDespesas,
        BigDecimal totalRecebimentosContasReceberEspecie,
        BigDecimal totalRecebimentosContasReceberPix,
        BigDecimal totalLucroFrota, // credito das prestacoes de contas de frota feitas neste caixa
        BigDecimal saldoFinalEspecie,
        BigDecimal saldoFinalPix
) {}
