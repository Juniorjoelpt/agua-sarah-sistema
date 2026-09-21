package com.aguasarah.contareceber;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contas-receber")
@RequiredArgsConstructor
public class ContaReceberController {

    private final ContaReceberService contaReceberService;

    // listagem fica aberta a qualquer usuario autenticado - a tela de Clientes usa
    // isso (filtrado por clienteId) pra mostrar o saldo devedor no historico dele
    @GetMapping
    public List<ContaReceber> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(defaultValue = "false") boolean somenteEmAberto) {
        return contaReceberService.listar(clienteId, somenteEmAberto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ContaReceber buscar(@PathVariable Long id) {
        return contaReceberService.buscarPorId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ContaReceber> criar(@Valid @RequestBody ContaReceberRequestDTO dto) {
        return ResponseEntity.ok(contaReceberService.criarManual(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<PagamentoContaReceber> registrarPagamento(
            @PathVariable Long id, @RequestBody PagamentoContaReceberRequestDTO dto) {
        return ResponseEntity.ok(contaReceberService.registrarPagamento(id, dto));
    }
}
