package com.aguasarah.precocliente;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes/{clienteId}/precos")
@RequiredArgsConstructor
public class PrecoClienteController {

    private final PrecoClienteService precoClienteService;

    @GetMapping
    public List<PrecoCliente> listar(@PathVariable Long clienteId) {
        return precoClienteService.listarPorCliente(clienteId);
    }

    @PutMapping
    public List<PrecoCliente> definirTabela(@PathVariable Long clienteId, @Valid @RequestBody List<PrecoClienteItemDTO> itens) {
        return precoClienteService.definirTabela(clienteId, itens);
    }
}
