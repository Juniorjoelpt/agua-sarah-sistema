package com.aguasarah.terceiros.produto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoTerceiroService {

    private final ProdutoTerceiroRepository produtoTerceiroRepository;

    public List<ProdutoTerceiro> listar(boolean somenteAtivos) {
        return somenteAtivos ? produtoTerceiroRepository.findByAtivoTrue() : produtoTerceiroRepository.findAll();
    }

    public ProdutoTerceiro buscarPorId(Long id) {
        return produtoTerceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto terceiro nao encontrado: " + id));
    }

    public ProdutoTerceiro criar(ProdutoTerceiroRequestDTO dto) {
        ProdutoTerceiro produto = ProdutoTerceiro.builder()
                .nome(dto.nome())
                .preco(dto.preco())
                .ativo(true)
                .build();
        return produtoTerceiroRepository.save(produto);
    }

    public ProdutoTerceiro atualizar(Long id, ProdutoTerceiroRequestDTO dto) {
        ProdutoTerceiro produto = buscarPorId(id);
        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setAtivo(dto.ativo());
        return produtoTerceiroRepository.save(produto);
    }

    public void inativar(Long id) {
        ProdutoTerceiro produto = buscarPorId(id);
        produto.setAtivo(false);
        produtoTerceiroRepository.save(produto);
    }
}
