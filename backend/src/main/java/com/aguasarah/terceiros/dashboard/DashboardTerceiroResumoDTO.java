package com.aguasarah.terceiros.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardTerceiroResumoDTO(
        int vendasHojeQuantidade,
        BigDecimal ticketMedioHoje,

        BigDecimal totalVendidoMesAtual,
        BigDecimal totalVendidoMesAnterior,
        BigDecimal variacaoPercentualMes, // null se o mes anterior nao teve vendas

        BigDecimal totalDespesasMesAtual,
        BigDecimal lucroBrutoMesAtual,

        List<PontoVendaDiaTerceiroDTO> vendasUltimos7Dias,
        List<RankingProdutoTerceiroDTO> topProdutosMes,
        List<RankingClienteTerceiroDTO> topClientesMes,

        BigDecimal totalContasReceberAberto,
        BigDecimal totalContasPagarAberto,
        int orcamentosPendentesQuantidade
) {}
