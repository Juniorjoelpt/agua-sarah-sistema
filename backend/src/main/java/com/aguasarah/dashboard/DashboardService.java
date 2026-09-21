package com.aguasarah.dashboard;

import com.aguasarah.contapagar.ContaPagarRepository;
import com.aguasarah.contapagar.StatusContaPagar;
import com.aguasarah.contareceber.ContaReceberRepository;
import com.aguasarah.contareceber.StatusContaReceber;
import com.aguasarah.despesa.Despesa;
import com.aguasarah.despesa.DespesaRepository;
import com.aguasarah.orcamento.Orcamento;
import com.aguasarah.orcamento.OrcamentoRepository;
import com.aguasarah.orcamento.StatusOrcamento;
import com.aguasarah.venda.ItemVenda;
import com.aguasarah.venda.Venda;
import com.aguasarah.venda.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// So agrega dados que ja existem em outros modulos (Venda, Despesa, ContaReceber,
// ContaPagar, Orcamento) - nao introduz nenhuma regra de negocio nova.
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VendaRepository vendaRepository;
    private final DespesaRepository despesaRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final ContaPagarRepository contaPagarRepository;
    private final OrcamentoRepository orcamentoRepository;

    public DashboardResumoDTO gerarResumo() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMesAtual = hoje.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMesAtual.minusMonths(1);
        LocalDate fimMesAnterior = inicioMesAtual.minusDays(1);

        List<Venda> vendasHoje = vendaRepository.findByDataHoraBetween(hoje.atStartOfDay(), hoje.atTime(23, 59, 59));
        List<Venda> vendasMesAtual = vendaRepository.findByDataHoraBetween(inicioMesAtual.atStartOfDay(), hoje.atTime(23, 59, 59));
        List<Venda> vendasMesAnterior = vendaRepository.findByDataHoraBetween(inicioMesAnterior.atStartOfDay(), fimMesAnterior.atTime(23, 59, 59));
        List<Venda> vendasUltimos7Dias = vendaRepository.findByDataHoraBetween(hoje.minusDays(6).atStartOfDay(), hoje.atTime(23, 59, 59));
        List<Despesa> despesasMesAtual = despesaRepository.findByDataBetween(inicioMesAtual, hoje);

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

        BigDecimal totalDespesasMesAtual = despesasMesAtual.stream().map(Despesa::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucroBrutoMesAtual = totalVendidoMesAtual.subtract(totalDespesasMesAtual);

        List<PontoVendaDiaDTO> pontosUltimos7Dias = montarSerieUltimos7Dias(hoje, vendasUltimos7Dias);
        List<RankingProdutoDTO> topProdutos = montarRankingProdutos(vendasMesAtual);
        List<RankingClienteDTO> topClientes = montarRankingClientes(vendasMesAtual);

        BigDecimal totalContasReceberAberto = contaReceberRepository
                .findByStatusInOrderByDataCriacaoDesc(List.of(StatusContaReceber.ABERTA, StatusContaReceber.PARCIAL))
                .stream()
                .map(c -> c.getValorOriginal().subtract(c.getValorPago()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalContasPagarAberto = contaPagarRepository
                .findByStatusInOrderByDataVencimentoAsc(List.of(StatusContaPagar.ABERTA, StatusContaPagar.PARCIAL))
                .stream()
                .map(c -> c.getValorOriginal().subtract(c.getValorPago()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int orcamentosPendentesQuantidade = (int) orcamentoRepository.findAllByOrderByDataCriacaoDesc().stream()
                .filter(o -> o.getStatus() == StatusOrcamento.PENDENTE)
                .count();

        return new DashboardResumoDTO(
                vendasHojeQuantidade, ticketMedioHoje,
                totalVendidoMesAtual, totalVendidoMesAnterior, variacaoPercentualMes,
                totalDespesasMesAtual, lucroBrutoMesAtual,
                pontosUltimos7Dias, topProdutos, topClientes,
                totalContasReceberAberto, totalContasPagarAberto, orcamentosPendentesQuantidade
        );
    }

    private BigDecimal somaValorTotal(List<Venda> vendas) {
        return vendas.stream().map(Venda::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<PontoVendaDiaDTO> montarSerieUltimos7Dias(LocalDate hoje, List<Venda> vendas) {
        Map<LocalDate, List<Venda>> porDia = vendas.stream()
                .collect(Collectors.groupingBy(v -> v.getDataHora().toLocalDate()));

        return java.util.stream.IntStream.rangeClosed(0, 6)
                .mapToObj(i -> hoje.minusDays(6 - i))
                .map(dia -> {
                    List<Venda> doDia = porDia.getOrDefault(dia, List.of());
                    return new PontoVendaDiaDTO(dia, somaValorTotal(doDia), doDia.size());
                })
                .toList();
    }

    private List<RankingProdutoDTO> montarRankingProdutos(List<Venda> vendas) {
        Map<String, List<ItemVenda>> porProduto = vendas.stream()
                .flatMap(v -> v.getItens().stream())
                .collect(Collectors.groupingBy(i -> i.getProduto().getNome()));

        return porProduto.entrySet().stream()
                .map(e -> new RankingProdutoDTO(
                        e.getKey(),
                        e.getValue().stream().mapToInt(ItemVenda::getQuantidade).sum(),
                        e.getValue().stream().map(ItemVenda::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .sorted(Comparator.comparing(RankingProdutoDTO::valorTotal).reversed())
                .limit(5)
                .toList();
    }

    private List<RankingClienteDTO> montarRankingClientes(List<Venda> vendas) {
        Map<String, List<Venda>> porCliente = vendas.stream()
                .filter(v -> v.getCliente() != null)
                .collect(Collectors.groupingBy(v -> v.getCliente().getNome()));

        return porCliente.entrySet().stream()
                .map(e -> new RankingClienteDTO(
                        e.getKey(),
                        somaValorTotal(e.getValue()),
                        e.getValue().size()
                ))
                .sorted(Comparator.comparing(RankingClienteDTO::valorTotal).reversed())
                .limit(5)
                .toList();
    }
}
