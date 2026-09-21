package com.aguasarah.orcamento;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

    private final OrcamentoService orcamentoService;
    private final OrcamentoPdfService orcamentoPdfService;

    @GetMapping
    public List<Orcamento> listar(@RequestParam(required = false) Long clienteId) {
        return orcamentoService.listar(clienteId);
    }

    @GetMapping("/{id}")
    public Orcamento buscar(@PathVariable Long id) {
        return orcamentoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Orcamento> criar(@Valid @RequestBody OrcamentoRequestDTO dto) {
        return ResponseEntity.ok(orcamentoService.criar(dto));
    }

    @PutMapping("/{id}")
    public Orcamento atualizar(@PathVariable Long id, @Valid @RequestBody OrcamentoRequestDTO dto) {
        return orcamentoService.atualizar(id, dto);
    }

    @PatchMapping("/{id}/status")
    public Orcamento atualizarStatus(@PathVariable Long id, @Valid @RequestBody AtualizarStatusOrcamentoRequestDTO dto) {
        return orcamentoService.atualizarStatus(id, dto.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        orcamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> baixarPdf(@PathVariable Long id) {
        Orcamento orcamento = orcamentoService.buscarPorId(id);
        byte[] pdf = orcamentoPdfService.gerar(orcamento);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("orcamento-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
