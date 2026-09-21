package com.aguasarah.terceiros.venda;

import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.terceiros.caixa.CaixaTerceiro;
import com.aguasarah.terceiros.caixa.CaixaTerceiroService;
import com.aguasarah.terceiros.cliente.ClienteTerceiro;
import com.aguasarah.terceiros.cliente.ClienteTerceiroRepository;
import com.aguasarah.terceiros.contareceber.ContaReceberTerceiroService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendaTerceiroService {

    private final VendaTerceiroRepository vendaTerceiroRepository;
    private final ClienteTerceiroRepository clienteTerceiroRepository;
    private final ProdutoTerceiroRepository produtoTerceiroRepository;
    private final UsuarioRepository usuarioRepository;
    private final CaixaTerceiroService caixaTerceiroService;
    private final ContaReceberTerceiroService contaReceberTerceiroService;

    @Transactional
    public VendaTerceiro registrarVenda(VendaTerceiroRequestDTO dto) {
        CaixaTerceiro caixa = caixaTerceiroService.buscarCaixaAberto();

        ClienteTerceiro cliente = clienteTerceiroRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente terceiro nao encontrado: " + dto.clienteId()));

        VendaTerceiro venda = VendaTerceiro.builder()
                .caixaTerceiro(caixa)
                .cliente(cliente)
                .placaCaminhao(dto.placaCaminhao())
                .dataHora(LocalDateTime.now())
                .usuario(usuarioLogado())
                .itens(new ArrayList<>())
                .build();

        BigDecimal valorTotal = BigDecimal.ZERO;
        for (VendaTerceiroRequestDTO.ItemVendaTerceiroRequestDTO itemDto : dto.itens()) {
            ProdutoTerceiro produto = produtoTerceiroRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto terceiro nao encontrado: " + itemDto.produtoId()));

            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemDto.quantidade()));
            venda.getItens().add(ItemVendaTerceiro.builder()
                    .venda(venda)
                    .produto(produto)
                    .quantidade(itemDto.quantidade())
                    .precoUnitario(produto.getPreco())
                    .subtotal(subtotal)
                    .build());
            valorTotal = valorTotal.add(subtotal);
        }
        venda.setValorTotal(valorTotal);

        BigDecimal recebidoEspecie = dto.valorRecebidoEspecie() != null ? dto.valorRecebidoEspecie() : BigDecimal.ZERO;
        BigDecimal recebidoPix = dto.valorRecebidoPix() != null ? dto.valorRecebidoPix() : BigDecimal.ZERO;
        BigDecimal valorFiado = dto.valorFiado() != null ? dto.valorFiado() : BigDecimal.ZERO;
        if (recebidoEspecie.add(recebidoPix).add(valorFiado).subtract(valorTotal).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new RegraNegocioException("A soma dos valores (espécie + PIX + fiado) deve ser igual ao valor total da venda: " + valorTotal);
        }
        venda.setValorRecebidoEspecie(recebidoEspecie);
        venda.setValorRecebidoPix(recebidoPix);
        venda.setValorFiado(valorFiado);

        venda = vendaTerceiroRepository.save(venda);

        if (valorFiado.compareTo(BigDecimal.ZERO) > 0) {
            contaReceberTerceiroService.abrirPorVenda(cliente, venda, valorFiado);
        }

        return venda;
    }

    public List<VendaTerceiro> listarPorCaixa(Long caixaId) {
        return vendaTerceiroRepository.findByCaixaTerceiroIdOrderByDataHoraDesc(caixaId);
    }

    public List<VendaTerceiro> listarPorCliente(Long clienteId) {
        return vendaTerceiroRepository.findByClienteIdOrderByDataHoraDesc(clienteId);
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
