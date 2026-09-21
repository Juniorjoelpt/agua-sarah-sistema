package com.aguasarah.terceiros.despesa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/terceiros/despesas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DespesaTerceiroController {

    private final DespesaTerceiroService despesaTerceiroService;

    @GetMapping
    public List<DespesaTerceiro> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return despesaTerceiroService.listar(inicio, fim);
    }

    @PostMapping
    public ResponseEntity<DespesaTerceiro> criar(@Valid @RequestBody DespesaTerceiroRequestDTO dto) {
        return ResponseEntity.ok(despesaTerceiroService.criar(dto));
    }

    @GetMapping("/{id}")
    public DespesaTerceiro buscar(@PathVariable Long id) {
        return despesaTerceiroService.buscarPorId(id);
    }
}
