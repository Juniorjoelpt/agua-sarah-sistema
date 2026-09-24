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
import java.math.RoundingMode;
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

        int quantidadeAvariaCliente = dto.quantidadeAvariaCliente() != null ? dto.quantidadeAvariaCliente() : 0;
        int quantidadeAvariaProducao = dto.quantidadeAvariaProducao() != null ? dto.quantidadeAvariaProducao() : 0;
        int quantidadeBonificados = dto.quantidadeBonificados() != null ? dto.quantidadeBonificados() : 0;

        if (quantidadeAvariaCliente < 0 || quantidadeAvariaProducao < 0 || quantidadeBonificados < 0) {
            throw new RegraNegocioException("Quantidade de avaria/bonificação não pode ser negativa");
        }

        // bonificacao so faz sentido havendo avaria de producao nesta venda (pode
        // bonificar menos galoes do que os avariados, mas nao bonificar sem nenhuma avaria)
        if (quantidadeBonificados > 0 && quantidadeAvariaProducao <= 0) {
            throw new RegraNegocioException("Informe a quantidade de galões com avaria de produção para gerar bonificação");
        }

        Cliente cliente = dto.clienteId() != null ? clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado: " + dto.clienteId())) : null;

        Venda venda = Venda.builder()
                .caixa(caixaAberto)
                .cliente(cliente)
                .usuario(usuarioLogado())
                .dataHora(LocalDateTime.now())
                .quantidadeAvariaCliente(quantidadeAvariaCliente)
                .quantidadeAvariaProducao(quantidadeAvariaProducao)
                .quantidadeBonificados(quantidadeBonificados)
                .observacao(dto.observacao())
                .itens(new ArrayList<>())
                .build();

        BigDecimal valorBruto = BigDecimal.ZERO;
        BigDecimal valorDescontoItens = BigDecimal.ZERO;
        Produto produtoEnvaseParaBonificacao = null;
        BigDecimal precoEnvaseParaBonificacao = null;

        for (VendaRequestDTO.ItemVendaRequestDTO itemDto : dto.itens()) {
            Produto produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado: " + itemDto.produtoId()));

            BigDecimal percentualDesconto = itemDto.percentualDesconto() != null ? itemDto.percentualDesconto() : BigDecimal.ZERO;
            if (percentualDesconto.compareTo(BigDecimal.ZERO) < 0 || percentualDesconto.compareTo(new BigDecimal("100")) > 0) {
                throw new RegraNegocioException("O desconto do item precisa estar entre 0% e 100%");
            }

            // usa o preco personalizado do cliente pra esse produto, se existir - senao cai no preco padrao
            BigDecimal precoUnitario = precoClienteService.precoEfetivo(produto, cliente);
            BigDecimal subtotalBruto = precoUnitario.multiply(BigDecimal.valueOf(itemDto.quantidade()));
            BigDecimal valorDescontoItem = subtotalBruto.multiply(percentualDesconto)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal subtotal = subtotalBruto.subtract(valorDescontoItem);

            ItemVenda item = ItemVenda.builder()
                    .venda(venda)
                    .produto(produto)
                    .quantidade(itemDto.quantidade())
                    .precoUnitario(precoUnitario)
                    .percentualDesconto(percentualDesconto)
                    .valorDesconto(valorDescontoItem)
                    .subtotal(subtotal)
                    .build();
            venda.getItens().add(item);
            valorBruto = valorBruto.add(subtotalBruto);
            valorDescontoItens = valorDescontoItens.add(valorDescontoItem);

            if (produto.isContaComoEnvase()) {
                produtoEnvaseParaBonificacao = produto;
                precoEnvaseParaBonificacao = precoUnitario;
            }
        }

        // valor dos itens ja liquido do desconto individual - avaria/bonificacao (abaixo)
        // sao descontos adicionais, calculados sobre o preco cheio do galao (sem entrar
        // no desconto % do item, pra nao acumular dois descontos sobre o mesmo galao)
        BigDecimal valorBrutoLiquido = valorBruto.subtract(valorDescontoItens);

        BigDecimal valorAvariaCliente = BigDecimal.ZERO;
        BigDecimal valorAvariaProducao = BigDecimal.ZERO;
        BigDecimal valorBonificado = BigDecimal.ZERO;

        if (quantidadeAvariaCliente > 0 || quantidadeAvariaProducao > 0) {
            if (produtoEnvaseParaBonificacao == null) {
                throw new RegraNegocioException("Nao ha produto de envase nesta venda para calcular a avaria");
            }
            valorAvariaCliente = precoEnvaseParaBonificacao.multiply(BigDecimal.valueOf(quantidadeAvariaCliente));
            valorAvariaProducao = precoEnvaseParaBonificacao.multiply(BigDecimal.valueOf(quantidadeAvariaProducao));

            if (quantidadeBonificados > 0) {
                valorBonificado = precoEnvaseParaBonificacao.multiply(BigDecimal.valueOf(quantidadeBonificados));
            }

            if (valorAvariaCliente.add(valorAvariaProducao).add(valorBonificado).compareTo(valorBrutoLiquido) > 0) {
                throw new RegraNegocioException("O desconto de avaria e bonificação não pode ser maior que o valor total da venda");
            }
        }

        BigDecimal valorAvaria = valorAvariaCliente.add(valorAvariaProducao);

        venda.setValorBruto(valorBruto);
        venda.setValorDescontoItens(valorDescontoItens);
        venda.setValorAvariaCliente(valorAvariaCliente);
        venda.setValorAvariaProducao(valorAvariaProducao);
        venda.setValorAvaria(valorAvaria);
        venda.setValorBonificado(valorBonificado);
        // valor efetivamente cobrado do cliente: descontos de item, avaria e bonificacao
        // ja saem descontados do total, nao sao so valores informativos - reduzem de fato
        // o que entra no caixa
        BigDecimal valorTotal = valorBrutoLiquido.subtract(valorAvaria).subtract(valorBonificado).max(BigDecimal.ZERO);
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
