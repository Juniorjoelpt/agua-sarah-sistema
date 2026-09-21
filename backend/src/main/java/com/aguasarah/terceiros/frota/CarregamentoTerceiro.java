package com.aguasarah.terceiros.frota;

import com.aguasarah.terceiros.caixa.CaixaTerceiro;
import com.aguasarah.terceiros.produto.ProdutoTerceiro;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// Carregamento de um caminhao de terceiro - e contabilizado como se o
// cliente tivesse comprado da Agua Sarah na hora: o valor sai do caixa de
// terceiros imediatamente (vira uma DespesaTerceiro), e so volta (como
// lucro) quando a prestacao de contas dessa viagem for feita.
@Entity
@Table(name = "carregamentos_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarregamentoTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caminhao_id")
    private CaminhaoTerceiro caminhao;

    // produto carregado - o caminhao pode levar galao ou garrafa, cada um com
    // seu proprio preco cadastrado em Produtos de Terceiros
    @ManyToOne
    @JoinColumn(name = "produto_id")
    private ProdutoTerceiro produto;

    // caixa de terceiros que estava aberto quando o carregamento foi lancado -
    // e dele que o valor do carregamento e debitado
    @ManyToOne
    @JoinColumn(name = "caixa_terceiro_id")
    private CaixaTerceiro caixaTerceiro;

    private LocalDate dataCarregamento;
    private String rota;
    private Integer quantidadeCarregada;
    private BigDecimal precoVenda;
    private BigDecimal valorCarregamento;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusCarregamentoTerceiro status = StatusCarregamentoTerceiro.PENDENTE;
}
