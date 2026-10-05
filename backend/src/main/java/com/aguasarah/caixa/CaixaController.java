package com.aguasarah.caixa;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/caixa")
@RequiredArgsConstructor
public class CaixaController {

    private final CaixaService caixaService;

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
