package com.aguasarah.fluxocaixa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fluxo-caixa")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FluxoCaixaController {

    private final FluxoCaixaService fluxoCaixaService;

    @GetMapping("/contas")
    public List<ContaBancaria> listarContas(@RequestParam(defaultValue = "true") boolean somenteAtivas) {
        return fluxoCaixaService.listarContas(somenteAtivas);
    }

    @PostMapping("/contas")
    public ResponseEntity<ContaBancaria> criarConta(@Valid @RequestBody ContaBancariaRequestDTO dto) {
        return ResponseEntity.ok(fluxoCaixaService.criarConta(dto));
    }

    @GetMapping("/contas/{id}")
    public ContaBancaria buscarConta(@PathVariable Long id) {
        return fluxoCaixaService.buscarConta(id);
    }

    @PutMapping("/contas/{id}")
    public ContaBancaria atualizarConta(@PathVariable Long id, @Valid @RequestBody ContaBancariaRequestDTO dto) {
        return fluxoCaixaService.atualizarConta(id, dto);
    }

    @DeleteMapping("/contas/{id}")
    public ResponseEntity<Void> inativarConta(@PathVariable Long id) {
        fluxoCaixaService.inativarConta(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contas/{id}/saldo")
    public SaldoContaBancariaDTO calcularSaldo(@PathVariable Long id) {
        return fluxoCaixaService.calcularSaldo(id);
    }

    @GetMapping("/contas/{id}/lancamentos")
    public List<LancamentoFluxoCaixa> listarLancamentos(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return fluxoCaixaService.listarLancamentos(id, inicio, fim);
    }

    @PostMapping("/lancamentos")
    public ResponseEntity<LancamentoFluxoCaixa> registrarLancamento(@Valid @RequestBody LancamentoFluxoCaixaRequestDTO dto) {
        return ResponseEntity.ok(fluxoCaixaService.registrarLancamentoManual(dto));
    }

    @DeleteMapping("/lancamentos/{id}")
    public ResponseEntity<Void> excluirLancamento(@PathVariable Long id) {
        fluxoCaixaService.excluirLancamento(id);
        return ResponseEntity.noContent().build();
    }
}
