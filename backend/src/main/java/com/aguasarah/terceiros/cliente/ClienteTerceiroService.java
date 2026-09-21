package com.aguasarah.terceiros.cliente;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteTerceiroService {

    private final ClienteTerceiroRepository clienteTerceiroRepository;

    public List<ClienteTerceiro> listar(boolean somenteAtivos) {
        return somenteAtivos ? clienteTerceiroRepository.findByAtivoTrue() : clienteTerceiroRepository.findAll();
    }

    public ClienteTerceiro buscarPorId(Long id) {
        return clienteTerceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente terceiro nao encontrado: " + id));
    }

    public ClienteTerceiro criar(ClienteTerceiroRequestDTO dto) {
        ClienteTerceiro cliente = ClienteTerceiro.builder()
                .nome(dto.nome())
                .telefone(dto.telefone())
                .bairro(dto.bairro())
                .endereco(dto.endereco())
                .ativo(true)
                .build();
        return clienteTerceiroRepository.save(cliente);
    }

    public ClienteTerceiro atualizar(Long id, ClienteTerceiroRequestDTO dto) {
        ClienteTerceiro cliente = buscarPorId(id);
        cliente.setNome(dto.nome());
        cliente.setTelefone(dto.telefone());
        cliente.setBairro(dto.bairro());
        cliente.setEndereco(dto.endereco());
        cliente.setAtivo(dto.ativo());
        return clienteTerceiroRepository.save(cliente);
    }

    public void inativar(Long id) {
        ClienteTerceiro cliente = buscarPorId(id);
        cliente.setAtivo(false);
        clienteTerceiroRepository.save(cliente);
    }
}
