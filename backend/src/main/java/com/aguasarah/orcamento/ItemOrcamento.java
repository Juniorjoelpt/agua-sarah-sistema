package com.aguasarah.orcamento;

import com.aguasarah.produto.Produto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_orcamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOrcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orcamento_id")
    @JsonBackReference
    private Orcamento orcamento;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private Integer quantidade;
    private BigDecimal precoUnitario; // copiado do produto no momento do orcamento

    @Builder.Default
    private BigDecimal percentualDesconto = BigDecimal.ZERO; // 0-100, desconto so deste item

    @Builder.Default
    private BigDecimal valorDesconto = BigDecimal.ZERO; // valor em R$ correspondente ao percentual acima

    private BigDecimal subtotal; // ja liquido - quantidade x precoUnitario, menos o valorDesconto
}
