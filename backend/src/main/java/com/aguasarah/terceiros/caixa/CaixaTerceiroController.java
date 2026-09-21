package com.aguasarah.terceiros.caixa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terceiros/caixa")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CaixaTerceiroController {

    private final CaixaTerceiroService caixaTerceiroService;

    @PostMapping("/abrir")
    public CaixaTerceiro abrir(@Valid @RequestBody AbrirCaixaTerceiroRequestDTO dto) {
        return caixaTerceiroService.abrirCaixa(dto);
    }

    @PostMapping("/{id}/fechar")
    public CaixaTerceiro fechar(@PathVariable Long id) {
        return caixaTerceiroService.fecharCaixa(id);
    }

    @GetMapping("/atual")
    public CaixaTerceiro atual() {
        return caixaTerceiroService.buscarCaixaAberto();
    }

    @GetMapping("/{id}/resumo")
    public ResumoCaixaTerceiroDTO resumo(@PathVariable Long id) {
        return caixaTerceiroService.calcularResumo(id);
    }
}
