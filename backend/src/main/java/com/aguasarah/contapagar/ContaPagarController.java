package com.aguasarah.contapagar;

import com.aguasarah.despesa.Despesa;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contas-pagar")
@RequiredArgsConstructor
public class ContaPagarController {

    private final ContaPagarService contaPagarService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ContaPagar> listar(
            @RequestParam(required = false) Long fornecedorId,
            @RequestParam(defaultValue = "false") boolean somenteEmAberto) {
        return contaPagarService.listar(fornecedorId, somenteEmAberto);
    }

    // fica aberto a qualquer usuario autenticado - o Painel usa isso pro alerta de vencidas
    @GetMapping("/vencidas")
    public List<ContaPagar> listarVencidas() {
        return contaPagarService.listarVencidas();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ContaPagar buscar(@PathVariable Long id) {
        return contaPagarService.buscarPorId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ContaPagar> criar(@Valid @RequestBody ContaPagarRequestDTO dto) {
        return ResponseEntity.ok(contaPagarService.criar(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<Despesa> registrarPagamento(@PathVariable Long id, @Valid @RequestBody PagamentoContaPagarRequestDTO dto) {
        return ResponseEntity.ok(contaPagarService.registrarPagamento(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        contaPagarService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
