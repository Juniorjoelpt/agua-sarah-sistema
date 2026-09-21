package com.aguasarah.venda;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @PostMapping
    public ResponseEntity<Venda> registrar(@Valid @RequestBody VendaRequestDTO dto) {
        return ResponseEntity.ok(vendaService.registrar(dto));
    }

    @GetMapping("/caixa/{caixaId}")
    public List<Venda> listarPorCaixa(@PathVariable Long caixaId) {
        return vendaService.listarPorCaixa(caixaId);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Venda> listarPorCliente(@PathVariable Long clienteId) {
        return vendaService.listarPorCliente(clienteId);
    }

    @GetMapping("/{id}")
    public Venda buscar(@PathVariable Long id) {
        return vendaService.buscarPorId(id);
    }
}
