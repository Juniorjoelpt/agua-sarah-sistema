package com.aguasarah.fornecedor;

import com.aguasarah.despesa.Despesa;
import com.aguasarah.despesa.DespesaRepository;
import com.aguasarah.estoque.Insumo;
import com.aguasarah.estoque.InsumoRepository;
import com.aguasarah.estoque.MovimentacaoInsumo;
import com.aguasarah.estoque.MovimentacaoInsumoRepository;
import com.aguasarah.estoque.TipoMovimentacaoInsumo;
import com.aguasarah.produto.Produto;
import com.aguasarah.produto.ProdutoRepository;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;
    private final InsumoRepository insumoRepository;
    private final DespesaRepository despesaRepository;
    private final MovimentacaoInsumoRepository movimentacaoInsumoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Fornecedor> listar(boolean somenteAtivos) {
        return somenteAtivos ? fornecedorRepository.findByAtivoTrue() : fornecedorRepository.findAll();
    }

    public Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor nao encontrado: " + id));
    }

    @Transactional
    public Fornecedor criar(FornecedorRequestDTO dto) {
        Fornecedor fornecedor = Fornecedor.builder()
                .nome(dto.nome())
                .nomeContato(dto.nomeContato())
                .telefone(dto.telefone())
                .endereco(dto.endereco())
                .ativo(true)
                .produtosFornecidos(buscarProdutos(dto.produtoIds()))
                .insumosFornecidos(buscarInsumos(dto.insumoIds()))
                .build();
        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public Fornecedor atualizar(Long id, FornecedorRequestDTO dto) {
        Fornecedor fornecedor = buscarPorId(id);
        fornecedor.setNome(dto.nome());
        fornecedor.setNomeContato(dto.nomeContato());
        fornecedor.setTelefone(dto.telefone());
        fornecedor.setEndereco(dto.endereco());
        fornecedor.setAtivo(dto.ativo());
        fornecedor.setProdutosFornecidos(buscarProdutos(dto.produtoIds()));
        fornecedor.setInsumosFornecidos(buscarInsumos(dto.insumoIds()));
        return fornecedorRepository.save(fornecedor);
    }

    public void inativar(Long id) {
        Fornecedor fornecedor = buscarPorId(id);
        fornecedor.setAtivo(false);
        fornecedorRepository.save(fornecedor);
    }

    private Set<Produto> buscarProdutos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(produtoRepository.findAllById(ids));
    }

    private Set<Insumo> buscarInsumos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(insumoRepository.findAllById(ids));
    }

    @Transactional
    public Despesa registrarCompra(Long fornecedorId, CompraFornecedorRequestDTO dto) {
        Fornecedor fornecedor = buscarPorId(fornecedorId);
        Despesa despesa = Despesa.builder()
                .descricao(dto.descricao())
                .categoria(dto.categoria())
                .valor(dto.valor())
                .data(dto.data() != null ? dto.data() : LocalDate.now())
                .fornecedor(fornecedor)
                .usuario(usuarioLogado())
                .build();
        despesa = despesaRepository.save(despesa);

        // se a compra veio com um insumo + quantidade, essa mesma operacao ja da entrada
        // no estoque - nao precisa lancar a movimentacao separadamente em Estoque
        if (dto.insumoId() != null && dto.quantidadeInsumo() != null && dto.quantidadeInsumo() > 0) {
            Insumo insumo = insumoRepository.findById(dto.insumoId())
                    .orElseThrow(() -> new EntityNotFoundException("Insumo nao encontrado: " + dto.insumoId()));

            insumo.setQuantidadeAtual(insumo.getQuantidadeAtual() + dto.quantidadeInsumo());
            insumoRepository.save(insumo);

            MovimentacaoInsumo movimentacao = MovimentacaoInsumo.builder()
                    .insumo(insumo)
                    .tipo(TipoMovimentacaoInsumo.ENTRADA)
                    .quantidade(dto.quantidadeInsumo())
                    .data(LocalDateTime.now())
                    .usuario(despesa.getUsuario())
                    .fornecedor(fornecedor)
                    .observacao("Compra: " + dto.descricao())
                    .build();
            movimentacaoInsumoRepository.save(movimentacao);
        }

        return despesa;
    }

    public List<Despesa> listarCompras(Long fornecedorId) {
        return despesaRepository.findByFornecedorIdOrderByDataDesc(fornecedorId);
    }

    // historico de fornecimento: entradas de insumo no estoque vinculadas a este fornecedor
    // (diferente do historico de compras, que e financeiro - aqui e o que foi entregue de fato)
    public List<MovimentacaoInsumo> listarFornecimentos(Long fornecedorId) {
        return movimentacaoInsumoRepository.findByFornecedorIdOrderByDataDesc(fornecedorId);
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
