package com.aguasarah.despesa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/despesas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DespesaController {

    private final DespesaService despesaService;

    @GetMapping
    public List<Despesa> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return despesaService.listar(inicio, fim);
    }

    @PostMapping
    public ResponseEntity<Despesa> criar(@Valid @RequestBody Despesa despesa, @RequestParam(required = false) Long caixaId) {
        return ResponseEntity.ok(despesaService.criar(despesa, caixaId));
    }

    @GetMapping("/{id}")
    public Despesa buscar(@PathVariable Long id) {
        return despesaService.buscarPorId(id);
    }
}
