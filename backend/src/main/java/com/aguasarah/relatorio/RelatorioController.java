package com.aguasarah.relatorio;

import com.aguasarah.caixa.CaixaService;
import com.aguasarah.caixa.ResumoCaixaDTO;
import com.aguasarah.despesa.Despesa;
import com.aguasarah.despesa.DespesaRepository;
import com.aguasarah.estoque.Insumo;
import com.aguasarah.estoque.InsumoRepository;
import com.aguasarah.fluxocaixa.LancamentoFluxoCaixa;
import com.aguasarah.fluxocaixa.LancamentoFluxoCaixaRepository;
import com.aguasarah.fluxocaixa.TipoLancamento;
import com.aguasarah.frota.Carregamento;
import com.aguasarah.frota.CarregamentoRepository;
import com.aguasarah.frota.PrestacaoContasRepository;
import com.aguasarah.venda.ItemVenda;
import com.aguasarah.venda.Ocorrencia;
import com.aguasarah.venda.Venda;
import com.aguasarah.venda.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Relatorios sao sempre consultas/agregacoes sobre as entidades ja existentes
// (Venda, Despesa, Insumo, Caixa...) - nada aqui e persistido como entidade propria.
@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RelatorioController {

    private final VendaRepository vendaRepository;
    private final DespesaRepository despesaRepository;
    private final InsumoRepository insumoRepository;
    private final CaixaService caixaService;
    private final CarregamentoRepository carregamentoRepository;
    private final PrestacaoContasRepository prestacaoContasRepository;
    private final LancamentoFluxoCaixaRepository lancamentoFluxoCaixaRepository;
    private final RelatorioPdfService relatorioPdfService;

    @GetMapping("/despesas")
    public List<Despesa> despesasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return despesaRepository.findByDataBetween(inicio, fim);
    }

    @GetMapping("/estoque")
    public List<Insumo> estoqueAtual() {
        return insumoRepository.findAll();
    }

    @GetMapping("/envase")
    public RelatorioEnvaseDTO envaseAgua(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return calcularEnvase(inicio, fim);
    }

    // historico de caixa por periodo - um resumo (mesmo formato do fechamento) por caixa
    // aberto no periodo; para gerar "por dia", basta chamar com inicio = fim
    @GetMapping("/caixa")
    public List<ResumoCaixaDTO> historicoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return caixaService.listarResumosPorPeriodo(inicio, fim);
    }

    /* ---------- exportacao em PDF (com timbrado) ---------- */

    @GetMapping("/caixa/pdf")
    public ResponseEntity<byte[]> caixaPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        byte[] pdf = relatorioPdfService.gerarCaixa(caixaService.listarResumosPorPeriodo(inicio, fim), inicio, fim);
        return respostaPdf(pdf, "historico-caixa.pdf");
    }

    @GetMapping("/despesas/pdf")
    public ResponseEntity<byte[]> despesasPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        byte[] pdf = relatorioPdfService.gerarDespesas(despesaRepository.findByDataBetween(inicio, fim), inicio, fim);
        return respostaPdf(pdf, "despesas.pdf");
    }

    // relatorio de fluxo de caixa - por periodo (ou um unico dia, quando inicio == fim),
    // somando entradas e saidas de TODAS as contas bancarias cadastradas
    @GetMapping("/fluxo-caixa")
    public RelatorioFluxoCaixaDTO fluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return calcularFluxoCaixa(inicio, fim);
    }

    @GetMapping("/fluxo-caixa/pdf")
    public ResponseEntity<byte[]> fluxoCaixaPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        byte[] pdf = relatorioPdfService.gerarFluxoCaixa(calcularFluxoCaixa(inicio, fim), inicio, fim);
        return respostaPdf(pdf, "fluxo-caixa.pdf");
    }

    private RelatorioFluxoCaixaDTO calcularFluxoCaixa(LocalDate inicio, LocalDate fim) {
        List<LancamentoFluxoCaixa> lancamentos = lancamentoFluxoCaixaRepository.findByDataBetweenOrderByDataDesc(inicio, fim);

        BigDecimal totalEntradas = lancamentos.stream()
                .filter(l -> l.getTipo() == TipoLancamento.ENTRADA)
                .map(LancamentoFluxoCaixa::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSaidas = lancamentos.stream()
                .filter(l -> l.getTipo() == TipoLancamento.SAIDA)
                .map(LancamentoFluxoCaixa::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioFluxoCaixaDTO(totalEntradas, totalSaidas, totalEntradas.subtract(totalSaidas), lancamentos);
    }

    @GetMapping("/estoque/pdf")
    public ResponseEntity<byte[]> estoquePdf() {
        byte[] pdf = relatorioPdfService.gerarEstoque(insumoRepository.findAll());
        return respostaPdf(pdf, "estoque.pdf");
    }

    @GetMapping("/envase/pdf")
    public ResponseEntity<byte[]> envasePdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        byte[] pdf = relatorioPdfService.gerarEnvase(calcularEnvase(inicio, fim), inicio, fim);
        return respostaPdf(pdf, "envase-agua.pdf");
    }

    private ResponseEntity<byte[]> respostaPdf(byte[] conteudo, String nomeArquivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(nomeArquivo).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(conteudo);
    }

    private RelatorioEnvaseDTO calcularEnvase(LocalDate inicio, LocalDate fim) {
        List<Venda> vendas = vendaRepository.findByDataHoraBetween(inicio.atStartOfDay(), fim.atTime(23, 59, 59));

        int totalGaloesVendidosPdv = vendas.stream()
                .flatMap(v -> v.getItens().stream())
                .filter(i -> i.getProduto().isContaComoEnvase())
                .mapToInt(ItemVenda::getQuantidade)
                .sum();

        int totalGaloesBonificados = vendas.stream()
                .filter(v -> v.getOcorrencia() == Ocorrencia.AVARIA_PRODUCAO)
                .mapToInt(v -> v.getQuantidadeBonificados() != null ? v.getQuantidadeBonificados() : 0)
                .sum();

        int totalGaloesAvariaClientePdv = vendas.stream()
                .filter(v -> v.getOcorrencia() == Ocorrencia.AVARIA_CLIENTE)
                .mapToInt(v -> v.getQuantidadeAvarias() != null ? v.getQuantidadeAvarias() : 0)
                .sum();

        // galoes que saem da fonte carregados nos caminhoes tambem contam como envase,
        // independente de terem sido vendidos, avariados ou devolvidos depois
        List<Carregamento> carregamentos = carregamentoRepository.findByDataCarregamentoBetween(inicio, fim);
        int totalGaloesCarregadosCaminhoes = carregamentos.stream()
                .mapToInt(c -> c.getQuantidadeCarregada() != null ? c.getQuantidadeCarregada() : 0)
                .sum();

        // avaria registrada na prestacao de contas dos carregamentos do periodo (so os ja prestados tem esse numero)
        int totalGaloesAvariaCaminhoes = prestacaoContasRepository.findByCarregamento_DataCarregamentoBetween(inicio, fim).stream()
                .mapToInt(p -> p.getQuantidadeAvaria() != null ? p.getQuantidadeAvaria() : 0)
                .sum();

        int totalGaloesEnvasados = totalGaloesVendidosPdv + totalGaloesCarregadosCaminhoes;
        int totalAvarias = totalGaloesBonificados + totalGaloesAvariaClientePdv + totalGaloesAvariaCaminhoes;

        return new RelatorioEnvaseDTO(
                totalGaloesEnvasados,
                totalGaloesVendidosPdv,
                totalGaloesCarregadosCaminhoes,
                totalAvarias,
                totalGaloesBonificados,
                totalGaloesAvariaClientePdv,
                totalGaloesAvariaCaminhoes
        );
    }
}
