package com.aguasarah.terceiros.contareceber;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terceiros/contas-receber")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ContaReceberTerceiroController {

    private final ContaReceberTerceiroService contaReceberTerceiroService;

    @GetMapping
    public List<ContaReceberTerceiro> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(defaultValue = "false") boolean somenteEmAberto) {
        return contaReceberTerceiroService.listar(clienteId, somenteEmAberto);
    }

    @GetMapping("/{id}")
    public ContaReceberTerceiro buscar(@PathVariable Long id) {
        return contaReceberTerceiroService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ContaReceberTerceiro> criar(@Valid @RequestBody ContaReceberTerceiroRequestDTO dto) {
        return ResponseEntity.ok(contaReceberTerceiroService.criarManual(dto));
    }

    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<PagamentoContaReceberTerceiro> registrarPagamento(
            @PathVariable Long id, @RequestBody PagamentoContaReceberTerceiroRequestDTO dto) {
        return ResponseEntity.ok(contaReceberTerceiroService.registrarPagamento(id, dto));
    }
}
