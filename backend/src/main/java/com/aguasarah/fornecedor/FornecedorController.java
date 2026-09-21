package com.aguasarah.fornecedor;

import com.aguasarah.despesa.Despesa;
import com.aguasarah.estoque.MovimentacaoInsumo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    // listagem fica aberta a qualquer usuario autenticado - o seletor de fornecedor
    // na tela de Estoque (entrada de insumo) usa isso
    @GetMapping
    public List<Fornecedor> listar(@RequestParam(defaultValue = "true") boolean somenteAtivos) {
        return fornecedorService.listar(somenteAtivos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Fornecedor buscar(@PathVariable Long id) {
        return fornecedorService.buscarPorId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Fornecedor> criar(@Valid @RequestBody FornecedorRequestDTO dto) {
        return ResponseEntity.ok(fornecedorService.criar(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Fornecedor atualizar(@PathVariable Long id, @Valid @RequestBody FornecedorRequestDTO dto) {
        return fornecedorService.atualizar(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        fornecedorService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/compras")
    public ResponseEntity<Despesa> registrarCompra(@PathVariable Long id, @Valid @RequestBody CompraFornecedorRequestDTO dto) {
        return ResponseEntity.ok(fornecedorService.registrarCompra(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/compras")
    public List<Despesa> listarCompras(@PathVariable Long id) {
        return fornecedorService.listarCompras(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/fornecimentos")
    public List<MovimentacaoInsumo> listarFornecimentos(@PathVariable Long id) {
        return fornecedorService.listarFornecimentos(id);
    }
}
