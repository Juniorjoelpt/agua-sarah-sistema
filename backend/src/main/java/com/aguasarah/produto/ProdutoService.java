package com.aguasarah.produto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public List<Produto> listar(boolean somenteAtivos) {
        return somenteAtivos ? produtoRepository.findByAtivoTrue() : produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado: " + id));
    }

    public Produto criar(Produto produto) {
        produto.setId(null);
        produto.setAtivo(true);
        return produtoRepository.save(produto);
    }

    public Produto atualizar(Long id, Produto dados) {
        Produto produto = buscarPorId(id);
        produto.setNome(dados.getNome());
        produto.setPreco(dados.getPreco());
        produto.setContaComoEnvase(dados.isContaComoEnvase());
        produto.setAtivo(dados.isAtivo());
        return produtoRepository.save(produto);
    }

    public void inativar(Long id) {
        Produto produto = buscarPorId(id);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }
}
