package com.aguasarah.terceiros.orcamento;

import com.aguasarah.terceiros.cliente.ClienteTerceiro;
import com.aguasarah.terceiros.cliente.ClienteTerceiroRepository;
import com.aguasarah.terceiros.produto.ProdutoTerceiro;
import com.aguasarah.terceiros.produto.ProdutoTerceiroRepository;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrcamentoTerceiroService {

    private final OrcamentoTerceiroRepository orcamentoTerceiroRepository;
    private final ClienteTerceiroRepository clienteTerceiroRepository;
    private final ProdutoTerceiroRepository produtoTerceiroRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public OrcamentoTerceiro criar(OrcamentoTerceiroRequestDTO dto) {
        ClienteTerceiro cliente = clienteTerceiroRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente terceiro nao encontrado: " + dto.clienteId()));

        OrcamentoTerceiro orcamento = OrcamentoTerceiro.builder()
                .cliente(cliente)
                .dataCriacao(LocalDate.now())
                .validoAte(dto.validoAte())
                .observacoes(dto.observacoes())
                .usuario(usuarioLogado())
                .status(StatusOrcamentoTerceiro.PENDENTE)
                .itens(new ArrayList<>())
                .build();

        preencherItens(orcamento, dto.itens());

        return orcamentoTerceiroRepository.save(orcamento);
    }

    @Transactional
    public OrcamentoTerceiro atualizar(Long id, OrcamentoTerceiroRequestDTO dto) {
        ClienteTerceiro cliente = clienteTerceiroRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente terceiro nao encontrado: " + dto.clienteId()));

        OrcamentoTerceiro orcamento = buscarPorId(id);
        orcamento.setCliente(cliente);
        orcamento.setValidoAte(dto.validoAte());
        orcamento.setObservacoes(dto.observacoes());

        orcamento.getItens().clear();
        preencherItens(orcamento, dto.itens());

        return orcamentoTerceiroRepository.save(orcamento);
    }

    private void preencherItens(OrcamentoTerceiro orcamento, List<OrcamentoTerceiroRequestDTO.ItemOrcamentoTerceiroRequestDTO> itensDto) {
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (OrcamentoTerceiroRequestDTO.ItemOrcamentoTerceiroRequestDTO itemDto : itensDto) {
            ProdutoTerceiro produto = produtoTerceiroRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto terceiro nao encontrado: " + itemDto.produtoId()));

            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemDto.quantidade()));
            ItemOrcamentoTerceiro item = ItemOrcamentoTerceiro.builder()
                    .orcamento(orcamento)
                    .produto(produto)
                    .quantidade(itemDto.quantidade())
                    .precoUnitario(produto.getPreco())
                    .subtotal(subtotal)
                    .build();
            orcamento.getItens().add(item);
            valorTotal = valorTotal.add(subtotal);
        }
        orcamento.setValorTotal(valorTotal);
    }

    public List<OrcamentoTerceiro> listar(Long clienteId) {
        if (clienteId != null) {
            return orcamentoTerceiroRepository.findByClienteIdOrderByDataCriacaoDesc(clienteId);
        }
        return orcamentoTerceiroRepository.findAllByOrderByDataCriacaoDesc();
    }

    public OrcamentoTerceiro buscarPorId(Long id) {
        return orcamentoTerceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orcamento de terceiros nao encontrado: " + id));
    }

    public OrcamentoTerceiro atualizarStatus(Long id, StatusOrcamentoTerceiro status) {
        OrcamentoTerceiro orcamento = buscarPorId(id);
        orcamento.setStatus(status);
        return orcamentoTerceiroRepository.save(orcamento);
    }

    public void excluir(Long id) {
        orcamentoTerceiroRepository.delete(buscarPorId(id));
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
