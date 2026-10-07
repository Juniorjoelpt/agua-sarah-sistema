package com.aguasarah.caixa;

import com.aguasarah.relatorio.RelatorioPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/caixa")
@RequiredArgsConstructor
public class CaixaController {

    private final CaixaService caixaService;
    private final RelatorioPdfService relatorioPdfService;

    @PostMapping("/abrir")
    public Caixa abrir(@RequestBody AbrirCaixaRequestDTO dto) {
        return caixaService.abrir(dto);
    }

    @PostMapping("/{id}/fechar")
    public ResumoCaixaDTO fechar(@PathVariable Long id) {
        return caixaService.fechar(id);
    }

    @GetMapping("/atual")
    public Caixa buscarAberto() {
        return caixaService.buscarAberto();
    }

    @GetMapping("/{id}")
    public Caixa buscar(@PathVariable Long id) {
        return caixaService.buscarPorId(id);
    }

    @GetMapping("/{id}/resumo")
    public ResumoCaixaDTO resumo(@PathVariable Long id) {
        return caixaService.calcularResumo(id);
    }

    // PDF do historico detalhado de um caixa (com timbrado) - liberado pro operador
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        DetalheCaixaDTO detalhe = caixaService.detalhar(id);
        byte[] conteudo = relatorioPdfService.gerarDetalheCaixa(detalhe);
        String data = detalhe.resumo().dataAbertura() != null ? detalhe.resumo().dataAbertura().toLocalDate().toString() : "caixa";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("caixa-" + id + "_" + data + ".pdf").build());
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(conteudo);
    }

    // resumo calculado de cada caixa do periodo (lista da tela de Historico de caixa)
    @GetMapping("/resumos")
    public List<ResumoCaixaDTO> resumos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return caixaService.listarResumosPorPeriodo(inicio, fim);
    }

    // historico detalhado de um caixa (vendas, despesas e recebimentos de fiado)
    @GetMapping("/{id}/detalhe")
    public DetalheCaixaDTO detalhe(@PathVariable Long id) {
        return caixaService.detalhar(id);
    }

    // historico simples (sem calculo pesado por caixa) - usado na propria tela de Caixa;
    // inicio/fim opcionais, sem eles traz todos os caixas ja abertos
    @GetMapping
    public List<Caixa> historico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return caixaService.listarHistorico(inicio, fim);
    }
}
