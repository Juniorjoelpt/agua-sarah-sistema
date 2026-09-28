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

    // preco cheio do item (quantidade x precoUnitario) - o desconto da venda (se
    // houver) e um percentual UNICO aplicado sobre o total da venda ja liquido de
    // avaria/bonificacao (ver Venda.percentualDesconto), nao mais por item
    private BigDecimal subtotal;
}
