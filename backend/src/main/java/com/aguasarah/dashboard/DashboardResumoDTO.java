package com.aguasarah.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResumoDTO(
        int vendasHojeQuantidade,
        BigDecimal ticketMedioHoje,

        BigDecimal totalVendidoMesAtual,
        BigDecimal totalVendidoMesAnterior,
        BigDecimal variacaoPercentualMes, // null se o mes anterior nao teve vendas (sem base de comparacao)

        BigDecimal totalDespesasMesAtual,
        BigDecimal lucroBrutoMesAtual, // totalVendidoMesAtual - totalDespesasMesAtual

        List<PontoVendaDiaDTO> vendasUltimos7Dias,
        List<RankingProdutoDTO> topProdutosMes,
        List<RankingClienteDTO> topClientesMes,

        BigDecimal totalContasReceberAberto,
        BigDecimal totalContasPagarAberto,
        int orcamentosPendentesQuantidade
) {}
