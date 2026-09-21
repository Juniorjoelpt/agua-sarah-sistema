package com.aguasarah.estoque;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping("/insumos")
    public List<Insumo> listarInsumos() {
        return estoqueService.listarInsumos();
    }

    @PostMapping("/insumos")
    public ResponseEntity<Insumo> criarInsumo(@Valid @RequestBody Insumo insumo) {
        return ResponseEntity.ok(estoqueService.criarInsumo(insumo));
    }

    @PostMapping("/movimentacoes")
    public ResponseEntity<MovimentacaoInsumo> registrarMovimentacao(@Valid @RequestBody MovimentacaoInsumoRequestDTO dto) {
        return ResponseEntity.ok(estoqueService.registrarMovimentacao(dto));
    }

    @GetMapping("/insumos/{id}/historico")
    public List<MovimentacaoInsumo> historico(@PathVariable Long id) {
        return estoqueService.historico(id);
    }
}
