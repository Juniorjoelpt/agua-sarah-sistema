package com.aguasarah.relatorio;

import com.aguasarah.fluxocaixa.LancamentoFluxoCaixa;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioFluxoCaixaDTO(
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldoPeriodo,
        List<LancamentoFluxoCaixa> lancamentos
) {}
