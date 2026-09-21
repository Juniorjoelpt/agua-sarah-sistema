package com.aguasarah.frota;

import com.aguasarah.cliente.Cliente;
import com.aguasarah.despesa.Despesa;
import com.aguasarah.relatorio.RelatorioPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/frota")
@RequiredArgsConstructor
public class FrotaController {

    private final FrotaService frotaService;
    private final RelatorioPdfService relatorioPdfService;

    @GetMapping("/caminhoes")
    public List<Caminhao> listarCaminhoes() {
        return frotaService.listarCaminhoes();
    }

    @PostMapping("/caminhoes")
    public ResponseEntity<Caminhao> criarCaminhao(@Valid @RequestBody CaminhaoRequestDTO dto) {
        return ResponseEntity.ok(frotaService.criarCaminhao(dto));
    }

    @GetMapping("/caminhoes/{id}")
    public Caminhao buscarCaminhao(@PathVariable Long id) {
        return frotaService.buscarCaminhao(id);
    }

    @PutMapping("/caminhoes/{id}")
    public Caminhao atualizarCaminhao(@PathVariable Long id, @Valid @RequestBody CaminhaoRequestDTO dto) {
        return frotaService.atualizarCaminhao(id, dto);
    }

    @DeleteMapping("/caminhoes/{id}")
    public ResponseEntity<Void> inativarCaminhao(@PathVariable Long id) {
        frotaService.inativarCaminhao(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/caminhoes/{id}/clientes-rota-fixa")
    public List<Cliente> clientesDaRotaFixa(@PathVariable Long id) {
        return frotaService.clientesDaRotaFixa(id);
    }

    // carregamento vale para qualquer caminhao (fixo ou variavel)
    @PostMapping("/carregamentos")
    public ResponseEntity<Carregamento> abrirCarregamento(@Valid @RequestBody AbrirCarregamentoRequestDTO dto) {
        return ResponseEntity.ok(frotaService.abrirCarregamento(dto));
    }

    @GetMapping("/caminhoes/{id}/carregamentos")
    public List<Carregamento> historicoCarregamentos(@PathVariable Long id) {
        return frotaService.historicoCarregamentos(id);
    }

    @PostMapping("/carregamentos/{id}/despesas")
    public ResponseEntity<Despesa> registrarDespesaCarregamento(@PathVariable Long id, @Valid @RequestBody DespesaCarregamentoRequestDTO dto) {
        return ResponseEntity.ok(frotaService.registrarDespesaCarregamento(id, dto));
    }

    @GetMapping("/carregamentos/{id}/despesas")
    public List<Despesa> listarDespesasCarregamento(@PathVariable Long id) {
        return frotaService.listarDespesasCarregamento(id);
    }

    @PostMapping("/prestacoes-contas")
    public ResponseEntity<PrestacaoContas> registrarPrestacaoContas(@Valid @RequestBody PrestacaoContasRequestDTO dto) {
        return ResponseEntity.ok(frotaService.registrarPrestacaoContas(dto));
    }

    @GetMapping("/carregamentos/{id}/prestacao-contas")
    public PrestacaoContas buscarPrestacaoPorCarregamento(@PathVariable Long id) {
        return frotaService.buscarPrestacaoPorCarregamento(id);
    }

    // historico consolidado, por data e opcionalmente por caminhao (todos os parametros opcionais)
    @GetMapping("/prestacoes-contas")
    public List<PrestacaoContas> listarPrestacoes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long caminhaoId) {
        return frotaService.listarPrestacoes(inicio, fim, caminhaoId);
    }

    @GetMapping("/prestacoes-contas/pdf")
    public ResponseEntity<byte[]> baixarPrestacoesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long caminhaoId) {
        List<PrestacaoContas> prestacoes = frotaService.listarPrestacoes(inicio, fim, caminhaoId);
        String placa = caminhaoId != null ? frotaService.buscarCaminhao(caminhaoId).getPlaca() : null;
        byte[] pdf = relatorioPdfService.gerarPrestacoesFrota(prestacoes, inicio, fim, placa);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("prestacoes-frota.pdf").build());
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
