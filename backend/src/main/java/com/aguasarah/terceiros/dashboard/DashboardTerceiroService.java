package com.aguasarah.terceiros.dashboard;

import com.aguasarah.terceiros.contapagar.ContaPagarTerceiroRepository;
import com.aguasarah.terceiros.contapagar.StatusContaPagarTerceiro;
import com.aguasarah.terceiros.contareceber.ContaReceberTerceiroRepository;
import com.aguasarah.terceiros.contareceber.StatusContaReceberTerceiro;
import com.aguasarah.terceiros.despesa.DespesaTerceiro;
import com.aguasarah.terceiros.despesa.DespesaTerceiroRepository;
import com.aguasarah.terceiros.orcamento.OrcamentoTerceiroRepository;
import com.aguasarah.terceiros.orcamento.StatusOrcamentoTerceiro;
import com.aguasarah.terceiros.venda.ItemVendaTerceiro;
import com.aguasarah.terceiros.venda.VendaTerceiro;
import com.aguasarah.terceiros.venda.VendaTerceiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// So agrega dados que ja existem nos outros sub-modulos de terceiros - nenhuma
// regra de negocio nova, mesmo espirito do DashboardService principal.
@Service
@RequiredArgsConstructor
public class DashboardTerceiroService {

    private final VendaTerceiroRepository vendaTerceiroRepository;
    private final DespesaTerceiroRepository despesaTerceiroRepository;
    private final ContaReceberTerceiroRepository contaReceberTerceiroRepository;
    private final ContaPagarTerceiroRepository contaPagarTerceiroRepository;
    private final OrcamentoTerceiroRepository orcamentoTerceiroRepository;

    public DashboardTerceiroResumoDTO gerarResumo() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMesAtual = hoje.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMesAtual.minusMonths(1);
        LocalDate fimMesAnterior = inicioMesAtual.minusDays(1);

        List<VendaTerceiro> vendasHoje = vendaTerceiroRepository.findByDataHoraBetween(hoje.atStartOfDay(), hoje.atTime(23, 59, 59));
        List<VendaTerceiro> vendasMesAtual = vendaTerceiroRepository.findByDataHoraBetween(inicioMesAtual.atStartOfDay(), hoje.atTime(23, 59, 59));
        List<VendaTerceiro> vendasMesAnterior = vendaTerceiroRepository.findByDataHoraBetween(inicioMesAnterior.atStartOfDay(), fimMesAnterior.atTime(23, 59, 59));
        List<VendaTerceiro> vendasUltimos7Dias = vendaTerceiroRepository.findByDataHoraBetween(hoje.minusDays(6).atStartOfDay(), hoje.atTime(23, 59, 59));
        List<DespesaTerceiro> despesasMesAtual = despesaTerceiroRepository.findByDataBetween(inicioMesAtual, hoje);

        int vendasHojeQuantidade = vendasHoje.size();
        BigDecimal totalVendidoHoje = somaValorTotal(vendasHoje);
        BigDecimal ticketMedioHoje = vendasHojeQuantidade > 0
                ? totalVendidoHoje.divide(BigDecimal.valueOf(vendasHojeQuantidade), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalVendidoMesAtual = somaValorTotal(vendasMesAtual);
        BigDecimal totalVendidoMesAnterior = somaValorTotal(vendasMesAnterior);
        BigDecimal variacaoPercentualMes = totalVendidoMesAnterior.compareTo(BigDecimal.ZERO) > 0
                ? totalVendidoMesAtual.subtract(totalVendidoMesAnterior)
                        .divide(totalVendidoMesAnterior, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : null;

        BigDecimal totalDespesasMesAtual = despesasMesAtual.stream().map(DespesaTerceiro::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucroBrutoMesAtual = totalVendidoMesAtual.subtract(totalDespesasMesAtual);

        List<PontoVendaDiaTerceiroDTO> pontosUltimos7Dias = montarSerieUltimos7Dias(hoje, vendasUltimos7Dias);
        List<RankingProdutoTerceiroDTO> topProdutos = montarRankingProdutos(vendasMesAtual);
        List<RankingClienteTerceiroDTO> topClientes = montarRankingClientes(vendasMesAtual);

        BigDecimal totalContasReceberAberto = contaReceberTerceiroRepository
                .findByStatusInOrderByDataCriacaoDesc(List.of(StatusContaReceberTerceiro.ABERTA, StatusContaReceberTerceiro.PARCIAL))
                .stream()
                .map(c -> c.getValorOriginal().subtract(c.getValorPago()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalContasPagarAberto = contaPagarTerceiroRepository
                .findByStatusInOrderByDataVencimentoAsc(List.of(StatusContaPagarTerceiro.ABERTA, StatusContaPagarTerceiro.PARCIAL))
                .stream()
                .map(c -> c.getValorOriginal().subtract(c.getValorPago()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int orcamentosPendentesQuantidade = (int) orcamentoTerceiroRepository.findAllByOrderByDataCriacaoDesc().stream()
                .filter(o -> o.getStatus() == StatusOrcamentoTerceiro.PENDENTE)
                .count();

        return new DashboardTerceiroResumoDTO(
                vendasHojeQuantidade, ticketMedioHoje,
                totalVendidoMesAtual, totalVendidoMesAnterior, variacaoPercentualMes,
                totalDespesasMesAtual, lucroBrutoMesAtual,
                pontosUltimos7Dias, topProdutos, topClientes,
                totalContasReceberAberto, totalContasPagarAberto, orcamentosPendentesQuantidade
        );
    }

    private BigDecimal somaValorTotal(List<VendaTerceiro> vendas) {
        return vendas.stream().map(VendaTerceiro::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<PontoVendaDiaTerceiroDTO> montarSerieUltimos7Dias(LocalDate hoje, List<VendaTerceiro> vendas) {
        Map<LocalDate, List<VendaTerceiro>> porDia = vendas.stream()
                .collect(Collectors.groupingBy(v -> v.getDataHora().toLocalDate()));

        return java.util.stream.IntStream.rangeClosed(0, 6)
                .mapToObj(i -> hoje.minusDays(6 - i))
                .map(dia -> {
                    List<VendaTerceiro> doDia = porDia.getOrDefault(dia, List.of());
                    return new PontoVendaDiaTerceiroDTO(dia, somaValorTotal(doDia), doDia.size());
                })
                .toList();
    }

    private List<RankingProdutoTerceiroDTO> montarRankingProdutos(List<VendaTerceiro> vendas) {
        Map<String, List<ItemVendaTerceiro>> porProduto = vendas.stream()
                .flatMap(v -> v.getItens().stream())
                .collect(Collectors.groupingBy(i -> i.getProduto().getNome()));

        return porProduto.entrySet().stream()
                .map(e -> new RankingProdutoTerceiroDTO(
                        e.getKey(),
                        e.getValue().stream().mapToInt(ItemVendaTerceiro::getQuantidade).sum(),
                        e.getValue().stream().map(ItemVendaTerceiro::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .sorted(Comparator.comparing(RankingProdutoTerceiroDTO::valorTotal).reversed())
                .limit(5)
                .toList();
    }

    private List<RankingClienteTerceiroDTO> montarRankingClientes(List<VendaTerceiro> vendas) {
        Map<String, List<VendaTerceiro>> porCliente = vendas.stream()
                .filter(v -> v.getCliente() != null)
                .collect(Collectors.groupingBy(v -> v.getCliente().getNome()));

        return porCliente.entrySet().stream()
                .map(e -> new RankingClienteTerceiroDTO(
                        e.getKey(),
                        somaValorTotal(e.getValue()),
                        e.getValue().size()
                ))
                .sorted(Comparator.comparing(RankingClienteTerceiroDTO::valorTotal).reversed())
                .limit(5)
                .toList();
    }
}
