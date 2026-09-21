package com.aguasarah.terceiros.venda;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terceiros/vendas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VendaTerceiroController {

    private final VendaTerceiroService vendaTerceiroService;

    @PostMapping
    public ResponseEntity<VendaTerceiro> registrar(@Valid @RequestBody VendaTerceiroRequestDTO dto) {
        return ResponseEntity.ok(vendaTerceiroService.registrarVenda(dto));
    }

    @GetMapping("/caixa/{caixaId}")
    public List<VendaTerceiro> listarPorCaixa(@PathVariable Long caixaId) {
        return vendaTerceiroService.listarPorCaixa(caixaId);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<VendaTerceiro> listarPorCliente(@PathVariable Long clienteId) {
        return vendaTerceiroService.listarPorCliente(clienteId);
    }
}
