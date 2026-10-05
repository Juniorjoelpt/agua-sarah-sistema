package com.aguasarah.caixa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Historico detalhado de UM caixa: resumo + tudo que aconteceu nele
// (vendas, despesas e recebimentos de fiado). Somente leitura.
public record DetalheCaixaDTO(
        ResumoCaixaDTO resumo,
        String usuarioAbertura,
        String usuarioFechamento,
        BigDecimal totalVendas,
        List<VendaLinha> vendas,
        List<DespesaLinha> despesas,
        List<RecebimentoLinha> recebimentos
) {
    public record VendaLinha(
            Long id,
            LocalDateTime dataHora,
            String cliente,
            String usuario,
            BigDecimal valorBruto,
            BigDecimal valorAvaria,
            BigDecimal valorBonificado,
            BigDecimal valorDesconto,
            BigDecimal percentualDesconto,
            Integer quantidadeAvariaCliente,
            Integer quantidadeAvariaProducao,
            Integer quantidadeBonificados,
            BigDecimal valorTotal,
            BigDecimal valorRecebidoEspecie,
            BigDecimal valorRecebidoPix,
            BigDecimal valorFiado,
            String observacao,
            List<ItemLinha> itens
    ) {}

    public record ItemLinha(String produto, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {}

    public record DespesaLinha(Long id, String descricao, String categoria, BigDecimal valor, LocalDate data) {}

    public record RecebimentoLinha(Long id, LocalDateTime data, String cliente, BigDecimal valorEspecie, BigDecimal valorPix) {}
}
