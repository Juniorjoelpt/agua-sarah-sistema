package com.aguasarah.terceiros.frota;

import com.aguasarah.terceiros.despesa.DespesaTerceiro;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/terceiros/frota")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FrotaTerceiroController {

    private final FrotaTerceiroService frotaTerceiroService;
    private final RelatorioFrotaTerceiroPdfService relatorioFrotaTerceiroPdfService;

    @GetMapping("/caminhoes")
    public List<CaminhaoTerceiro> listarCaminhoes() {
        return frotaTerceiroService.listarCaminhoes();
    }

    @PostMapping("/caminhoes")
    public ResponseEntity<CaminhaoTerceiro> criarCaminhao(@Valid @RequestBody CaminhaoTerceiroRequestDTO dto) {
        return ResponseEntity.ok(frotaTerceiroService.criarCaminhao(dto));
    }

    @GetMapping("/caminhoes/{id}")
    public CaminhaoTerceiro buscarCaminhao(@PathVariable Long id) {
        return frotaTerceiroService.buscarCaminhao(id);
    }

    @PutMapping("/caminhoes/{id}")
    public CaminhaoTerceiro atualizarCaminhao(@PathVariable Long id, @Valid @RequestBody CaminhaoTerceiroRequestDTO dto) {
        return frotaTerceiroService.atualizarCaminhao(id, dto);
    }

    @DeleteMapping("/caminhoes/{id}")
    public ResponseEntity<Void> inativarCaminhao(@PathVariable Long id) {
        frotaTerceiroService.inativarCaminhao(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/carregamentos")
    public ResponseEntity<CarregamentoTerceiro> abrirCarregamento(@Valid @RequestBody AbrirCarregamentoTerceiroRequestDTO dto) {
        return ResponseEntity.ok(frotaTerceiroService.abrirCarregamento(dto));
    }

    @GetMapping("/caminhoes/{id}/carregamentos")
    public List<CarregamentoTerceiro> historicoCarregamentos(@PathVariable Long id) {
        return frotaTerceiroService.historicoCarregamentos(id);
    }

    @PostMapping("/carregamentos/{id}/despesas")
    public ResponseEntity<DespesaTerceiro> registrarDespesaCarregamento(@PathVariable Long id, @Valid @RequestBody DespesaCarregamentoTerceiroRequestDTO dto) {
        return ResponseEntity.ok(frotaTerceiroService.registrarDespesaCarregamento(id, dto));
    }

    @GetMapping("/carregamentos/{id}/despesas")
    public List<DespesaTerceiro> listarDespesasCarregamento(@PathVariable Long id) {
        return frotaTerceiroService.listarDespesasCarregamento(id);
    }

    @PostMapping("/prestacoes-contas")
    public ResponseEntity<PrestacaoContasTerceiro> registrarPrestacaoContas(@Valid @RequestBody PrestacaoContasTerceiroRequestDTO dto) {
        return ResponseEntity.ok(frotaTerceiroService.registrarPrestacaoContas(dto));
    }

    @GetMapping("/carregamentos/{id}/prestacao-contas")
    public PrestacaoContasTerceiro buscarPrestacaoPorCarregamento(@PathVariable Long id) {
        return frotaTerceiroService.buscarPrestacaoPorCarregamento(id);
    }

    // relatorio: prestacoes de contas da frota de terceiros, por periodo e/ou caminhao
    @GetMapping("/prestacoes-contas")
    public List<PrestacaoContasTerceiro> listarPrestacoes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long caminhaoId) {
        return frotaTerceiroService.listarPrestacoes(inicio, fim, caminhaoId);
    }

    @GetMapping("/prestacoes-contas/pdf")
    public ResponseEntity<byte[]> baixarPrestacoesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long caminhaoId) {
        List<PrestacaoContasTerceiro> prestacoes = frotaTerceiroService.listarPrestacoes(inicio, fim, caminhaoId);
        String placa = caminhaoId != null ? frotaTerceiroService.buscarCaminhao(caminhaoId).getPlaca() : null;
        byte[] pdf = relatorioFrotaTerceiroPdfService.gerar(prestacoes, inicio, fim, placa);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("prestacoes-frota-terceiros.pdf").build());
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
