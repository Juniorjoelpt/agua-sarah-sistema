package com.aguasarah.terceiros.contapagar;

import com.aguasarah.terceiros.despesa.DespesaTerceiro;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terceiros/contas-pagar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ContaPagarTerceiroController {

    private final ContaPagarTerceiroService contaPagarTerceiroService;

    @GetMapping
    public List<ContaPagarTerceiro> listar(@RequestParam(defaultValue = "false") boolean somenteEmAberto) {
        return contaPagarTerceiroService.listar(somenteEmAberto);
    }

    @GetMapping("/vencidas")
    public List<ContaPagarTerceiro> listarVencidas() {
        return contaPagarTerceiroService.listarVencidas();
    }

    @GetMapping("/{id}")
    public ContaPagarTerceiro buscar(@PathVariable Long id) {
        return contaPagarTerceiroService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ContaPagarTerceiro> criar(@Valid @RequestBody ContaPagarTerceiroRequestDTO dto) {
        return ResponseEntity.ok(contaPagarTerceiroService.criar(dto));
    }

    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<DespesaTerceiro> registrarPagamento(@PathVariable Long id, @Valid @RequestBody PagamentoContaPagarTerceiroRequestDTO dto) {
        return ResponseEntity.ok(contaPagarTerceiroService.registrarPagamento(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        contaPagarTerceiroService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
