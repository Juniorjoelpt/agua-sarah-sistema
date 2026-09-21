package com.aguasarah.orcamento;

import com.aguasarah.cliente.Cliente;
import com.aguasarah.cliente.ClienteRepository;
import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.precocliente.PrecoClienteService;
import com.aguasarah.produto.Produto;
import com.aguasarah.produto.ProdutoRepository;
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
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrecoClienteService precoClienteService;

    @Transactional
    public Orcamento criar(OrcamentoRequestDTO dto) {
        validarCliente(dto);

        Orcamento orcamento = Orcamento.builder()
                .cliente(buscarClienteOuNulo(dto.clienteId()))
                .nomeClienteAvulso(dto.clienteId() == null ? dto.nomeClienteAvulso() : null)
                .telefoneClienteAvulso(dto.clienteId() == null ? dto.telefoneClienteAvulso() : null)
                .dataCriacao(LocalDate.now())
                .validoAte(dto.validoAte())
                .observacoes(dto.observacoes())
                .usuario(usuarioLogado())
                .status(StatusOrcamento.PENDENTE)
                .itens(new ArrayList<>())
                .build();

        preencherItens(orcamento, dto.itens());

        return orcamentoRepository.save(orcamento);
    }

    @Transactional
    public Orcamento atualizar(Long id, OrcamentoRequestDTO dto) {
        validarCliente(dto);
        Orcamento orcamento = buscarPorId(id);

        orcamento.setCliente(buscarClienteOuNulo(dto.clienteId()));
        orcamento.setNomeClienteAvulso(dto.clienteId() == null ? dto.nomeClienteAvulso() : null);
        orcamento.setTelefoneClienteAvulso(dto.clienteId() == null ? dto.telefoneClienteAvulso() : null);
        orcamento.setValidoAte(dto.validoAte());
        orcamento.setObservacoes(dto.observacoes());

        orcamento.getItens().clear();
        preencherItens(orcamento, dto.itens());

        return orcamentoRepository.save(orcamento);
    }

    private void preencherItens(Orcamento orcamento, List<OrcamentoRequestDTO.ItemOrcamentoRequestDTO> itensDto) {
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (OrcamentoRequestDTO.ItemOrcamentoRequestDTO itemDto : itensDto) {
            Produto produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado: " + itemDto.produtoId()));

            // usa o preco personalizado do cliente pra esse produto, se existir - so vale pra cliente cadastrado
            BigDecimal precoUnitario = precoClienteService.precoEfetivo(produto, orcamento.getCliente());
            BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(itemDto.quantidade()));
            ItemOrcamento item = ItemOrcamento.builder()
                    .orcamento(orcamento)
                    .produto(produto)
                    .quantidade(itemDto.quantidade())
                    .precoUnitario(precoUnitario)
                    .subtotal(subtotal)
                    .build();
            orcamento.getItens().add(item);
            valorTotal = valorTotal.add(subtotal);
        }
        orcamento.setValorTotal(valorTotal);
    }

    private void validarCliente(OrcamentoRequestDTO dto) {
        if (dto.clienteId() == null && (dto.nomeClienteAvulso() == null || dto.nomeClienteAvulso().isBlank())) {
            throw new RegraNegocioException("Selecione um cliente cadastrado ou informe o nome do cliente");
        }
    }

    private Cliente buscarClienteOuNulo(Long clienteId) {
        if (clienteId == null) return null;
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado: " + clienteId));
    }

    public List<Orcamento> listar(Long clienteId) {
        if (clienteId != null) {
            return orcamentoRepository.findByClienteIdOrderByDataCriacaoDesc(clienteId);
        }
        return orcamentoRepository.findAllByOrderByDataCriacaoDesc();
    }

    public Orcamento buscarPorId(Long id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orcamento nao encontrado: " + id));
    }

    public Orcamento atualizarStatus(Long id, StatusOrcamento status) {
        Orcamento orcamento = buscarPorId(id);
        orcamento.setStatus(status);
        return orcamentoRepository.save(orcamento);
    }

    public void excluir(Long id) {
        orcamentoRepository.delete(buscarPorId(id));
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
