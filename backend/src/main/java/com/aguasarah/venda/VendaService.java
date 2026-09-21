package com.aguasarah.venda;

import com.aguasarah.caixa.Caixa;
import com.aguasarah.caixa.CaixaService;
import com.aguasarah.caixa.StatusCaixa;
import com.aguasarah.cliente.Cliente;
import com.aguasarah.cliente.ClienteRepository;
import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.contareceber.ContaReceberService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CaixaService caixaService;
    private final ContaReceberService contaReceberService;
    private final PrecoClienteService precoClienteService;

    @Transactional
    public Venda registrar(VendaRequestDTO dto) {
        Caixa caixaAberto = caixaService.buscarAberto();
        if (caixaAberto.getStatus() != StatusCaixa.ABERTO) {
            throw new RegraNegocioException("Nao e possivel vender sem um caixa aberto");
        }

        if (dto.ocorrencia() != Ocorrencia.NENHUMA
                && (dto.quantidadeAvarias() == null || dto.quantidadeAvarias() <= 0)) {
            throw new RegraNegocioException("Informe a quantidade de galões com avaria");
        }

        if (dto.ocorrencia() == Ocorrencia.AVARIA_PRODUCAO
                && (dto.quantidadeBonificados() == null || dto.quantidadeBonificados() <= 0)) {
            throw new RegraNegocioException("Informe a quantidade de galões bonificados");
        }

        Cliente cliente = dto.clienteId() != null ? clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado: " + dto.clienteId())) : null;

        Venda venda = Venda.builder()
                .caixa(caixaAberto)
                .cliente(cliente)
                .usuario(usuarioLogado())
                .dataHora(LocalDateTime.now())
                .ocorrencia(dto.ocorrencia())
                .quantidadeAvarias(dto.ocorrencia() != Ocorrencia.NENHUMA ? dto.quantidadeAvarias() : null)
                .quantidadeBonificados(dto.ocorrencia() == Ocorrencia.AVARIA_PRODUCAO ? dto.quantidadeBonificados() : null)
                .observacao(dto.observacao())
                .itens(new ArrayList<>())
                .build();

        BigDecimal valorBruto = BigDecimal.ZERO;
        Produto produtoEnvaseParaBonificacao = null;
        BigDecimal precoEnvaseParaBonificacao = null;

        for (VendaRequestDTO.ItemVendaRequestDTO itemDto : dto.itens()) {
            Produto produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado: " + itemDto.produtoId()));

            // usa o preco personalizado do cliente pra esse produto, se existir - senao cai no preco padrao
            BigDecimal precoUnitario = precoClienteService.precoEfetivo(produto, cliente);
            BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(itemDto.quantidade()));
            ItemVenda item = ItemVenda.builder()
                    .venda(venda)
                    .produto(produto)
                    .quantidade(itemDto.quantidade())
                    .precoUnitario(precoUnitario)
                    .subtotal(subtotal)
                    .build();
            venda.getItens().add(item);
            valorBruto = valorBruto.add(subtotal);

            if (produto.isContaComoEnvase()) {
                produtoEnvaseParaBonificacao = produto;
                precoEnvaseParaBonificacao = precoUnitario;
            }
        }

        BigDecimal valorAvaria = BigDecimal.ZERO;
        BigDecimal valorBonificado = BigDecimal.ZERO;

        if (dto.ocorrencia() != Ocorrencia.NENHUMA) {
            if (produtoEnvaseParaBonificacao == null) {
                throw new RegraNegocioException("Nao ha produto de envase nesta venda para calcular a avaria");
            }
            valorAvaria = precoEnvaseParaBonificacao.multiply(BigDecimal.valueOf(dto.quantidadeAvarias()));

            if (dto.ocorrencia() == Ocorrencia.AVARIA_PRODUCAO) {
                valorBonificado = precoEnvaseParaBonificacao
                        .multiply(BigDecimal.valueOf(dto.quantidadeBonificados()));
            }

            if (valorAvaria.add(valorBonificado).compareTo(valorBruto) > 0) {
                throw new RegraNegocioException("O desconto de avaria e bonificação não pode ser maior que o valor total da venda");
            }
        }

        venda.setValorBruto(valorBruto);
        venda.setValorAvaria(valorAvaria);
        venda.setValorBonificado(valorBonificado);
        // valor efetivamente cobrado do cliente: avaria e bonificacao ja saem descontadas do total,
        // nao sao so valores informativos - reduzem de fato o que entra no caixa
        BigDecimal valorTotal = valorBruto.subtract(valorAvaria).subtract(valorBonificado);
        venda.setValorTotal(valorTotal);

        // pagamento pode ser dividido entre especie, PIX e fiado - os tres juntos precisam bater com o total
        BigDecimal recebidoEspecie = dto.valorRecebidoEspecie() != null ? dto.valorRecebidoEspecie() : BigDecimal.ZERO;
        BigDecimal recebidoPix = dto.valorRecebidoPix() != null ? dto.valorRecebidoPix() : BigDecimal.ZERO;
        BigDecimal valorFiado = dto.valorFiado() != null ? dto.valorFiado() : BigDecimal.ZERO;
        BigDecimal totalRecebido = recebidoEspecie.add(recebidoPix).add(valorFiado);
        if (totalRecebido.subtract(valorTotal).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new RegraNegocioException("A soma dos valores (espécie + PIX + fiado) deve ser igual ao valor total da venda: " + valorTotal);
        }
        if (valorFiado.compareTo(BigDecimal.ZERO) > 0 && cliente == null) {
            throw new RegraNegocioException("Venda fiado exige um cliente cadastrado - não é possível vender fiado para consumidor não identificado");
        }
        venda.setValorRecebidoEspecie(recebidoEspecie);
        venda.setValorRecebidoPix(recebidoPix);
        venda.setValorFiado(valorFiado);

        venda = vendaRepository.save(venda);

        if (valorFiado.compareTo(BigDecimal.ZERO) > 0) {
            contaReceberService.abrirPorVenda(cliente, venda, valorFiado);
        }

        return venda;
    }

    public List<Venda> listarPorCaixa(Long caixaId) {
        return vendaRepository.findByCaixaId(caixaId);
    }

    public List<Venda> listarPorCliente(Long clienteId) {
        return vendaRepository.findByClienteIdOrderByDataHoraDesc(clienteId);
    }

    public Venda buscarPorId(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venda nao encontrada: " + id));
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
