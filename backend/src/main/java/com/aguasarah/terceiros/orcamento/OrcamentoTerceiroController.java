package com.aguasarah.terceiros.orcamento;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terceiros/orcamentos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrcamentoTerceiroController {

    private final OrcamentoTerceiroService orcamentoTerceiroService;
    private final OrcamentoTerceiroPdfService orcamentoTerceiroPdfService;

    @GetMapping
    public List<OrcamentoTerceiro> listar(@RequestParam(required = false) Long clienteId) {
        return orcamentoTerceiroService.listar(clienteId);
    }

    @GetMapping("/{id}")
    public OrcamentoTerceiro buscar(@PathVariable Long id) {
        return orcamentoTerceiroService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<OrcamentoTerceiro> criar(@Valid @RequestBody OrcamentoTerceiroRequestDTO dto) {
        return ResponseEntity.ok(orcamentoTerceiroService.criar(dto));
    }

    @PutMapping("/{id}")
    public OrcamentoTerceiro atualizar(@PathVariable Long id, @Valid @RequestBody OrcamentoTerceiroRequestDTO dto) {
        return orcamentoTerceiroService.atualizar(id, dto);
    }

    @PatchMapping("/{id}/status")
    public OrcamentoTerceiro atualizarStatus(@PathVariable Long id, @Valid @RequestBody AtualizarStatusOrcamentoTerceiroRequestDTO dto) {
        return orcamentoTerceiroService.atualizarStatus(id, dto.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        orcamentoTerceiroService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> baixarPdf(@PathVariable Long id) {
        OrcamentoTerceiro orcamento = orcamentoTerceiroService.buscarPorId(id);
        byte[] pdf = orcamentoTerceiroPdfService.gerar(orcamento);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("orcamento-terceiro-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
