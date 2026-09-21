package com.aguasarah.caixa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Resumo calculado do caixa (nao gravado - sempre derivado de Venda + Despesa).
// Usado na tela de fechamento, no historico simples da tela de Caixa e no
// relatorio de historico de caixa por periodo/dia.
public record ResumoCaixaDTO(
        Long caixaId,
        StatusCaixa status,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        BigDecimal saldoInicialEspecie,
        BigDecimal saldoInicialPix,
        BigDecimal totalVendasEspecie,
        BigDecimal totalVendasPix,
        BigDecimal totalDespesas,
        Integer totalGaloesBonificados,
        BigDecimal totalRecebimentosContasReceberEspecie,
        BigDecimal totalRecebimentosContasReceberPix,
        BigDecimal saldoFinalEspecie,
        BigDecimal saldoFinalPix
) {}
