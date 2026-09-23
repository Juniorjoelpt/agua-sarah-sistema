package com.aguasarah.venda;

import com.aguasarah.produto.Produto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_venda")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    @JsonBackReference
    private Venda venda;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private Integer quantidade;
    private BigDecimal precoUnitario; // copiado do produto no momento da venda

    @Builder.Default
    private BigDecimal percentualDesconto = BigDecimal.ZERO; // 0-100, desconto so deste item

    @Builder.Default
    private BigDecimal valorDesconto = BigDecimal.ZERO; // valor em R$ correspondente ao percentual acima

    private BigDecimal subtotal; // ja liquido - quantidade x precoUnitario, menos o valorDesconto
}
