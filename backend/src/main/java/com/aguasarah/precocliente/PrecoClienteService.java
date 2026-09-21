package com.aguasarah.precocliente;

import com.aguasarah.cliente.Cliente;
import com.aguasarah.cliente.ClienteRepository;
import com.aguasarah.produto.Produto;
import com.aguasarah.produto.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrecoClienteService {

    private final PrecoClienteRepository precoClienteRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public List<PrecoCliente> listarPorCliente(Long clienteId) {
        return precoClienteRepository.findByClienteId(clienteId);
    }

    // substitui a tabela inteira do cliente pela lista enviada - forma mais simples
    // de editar (a tela manda so os produtos que tem preco personalizado)
    @Transactional
    public List<PrecoCliente> definirTabela(Long clienteId, List<PrecoClienteItemDTO> itens) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado: " + clienteId));

        precoClienteRepository.deleteByClienteId(clienteId);

        List<PrecoCliente> novos = itens.stream().map(item -> {
            Produto produto = produtoRepository.findById(item.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado: " + item.produtoId()));
            return PrecoCliente.builder().cliente(cliente).produto(produto).preco(item.preco()).build();
        }).toList();

        return precoClienteRepository.saveAll(novos);
    }

    // usado por Venda e Orcamento para saber qual preco cobrar - cai no preco
    // padrao do produto se o cliente nao tiver preco personalizado pra ele
    public BigDecimal precoEfetivo(Produto produto, Cliente cliente) {
        if (cliente == null) return produto.getPreco();
        return precoClienteRepository.findByClienteIdAndProdutoId(cliente.getId(), produto.getId())
                .map(PrecoCliente::getPreco)
                .orElse(produto.getPreco());
    }
}
