package com.aguasarah.cliente;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> listar(boolean somenteAtivos) {
        return somenteAtivos ? clienteRepository.findByAtivoTrue() : clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado: " + id));
    }

    public Cliente criar(Cliente cliente) {
        cliente.setId(null);
        cliente.setAtivo(true);
        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(Long id, Cliente dados) {
        Cliente cliente = buscarPorId(id);
        cliente.setNome(dados.getNome());
        cliente.setTipo(dados.getTipo());
        cliente.setTelefone(dados.getTelefone());
        cliente.setBairro(dados.getBairro());
        cliente.setEndereco(dados.getEndereco());
        cliente.setAtivo(dados.isAtivo());
        return clienteRepository.save(cliente);
    }

    public void inativar(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }
}
