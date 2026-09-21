package com.aguasarah.terceiros.cliente;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terceiros/clientes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ClienteTerceiroController {

    private final ClienteTerceiroService clienteTerceiroService;

    @GetMapping
    public List<ClienteTerceiro> listar(@RequestParam(defaultValue = "true") boolean somenteAtivos) {
        return clienteTerceiroService.listar(somenteAtivos);
    }

    @GetMapping("/{id}")
    public ClienteTerceiro buscar(@PathVariable Long id) {
        return clienteTerceiroService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ClienteTerceiro> criar(@Valid @RequestBody ClienteTerceiroRequestDTO dto) {
        return ResponseEntity.ok(clienteTerceiroService.criar(dto));
    }

    @PutMapping("/{id}")
    public ClienteTerceiro atualizar(@PathVariable Long id, @Valid @RequestBody ClienteTerceiroRequestDTO dto) {
        return clienteTerceiroService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        clienteTerceiroService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
