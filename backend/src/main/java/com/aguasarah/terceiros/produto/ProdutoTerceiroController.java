package com.aguasarah.terceiros.produto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terceiros/produtos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProdutoTerceiroController {

    private final ProdutoTerceiroService produtoTerceiroService;

    @GetMapping
    public List<ProdutoTerceiro> listar(@RequestParam(defaultValue = "true") boolean somenteAtivos) {
        return produtoTerceiroService.listar(somenteAtivos);
    }

    @PostMapping
    public ResponseEntity<ProdutoTerceiro> criar(@Valid @RequestBody ProdutoTerceiroRequestDTO dto) {
        return ResponseEntity.ok(produtoTerceiroService.criar(dto));
    }

    @PutMapping("/{id}")
    public ProdutoTerceiro atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoTerceiroRequestDTO dto) {
        return produtoTerceiroService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        produtoTerceiroService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
