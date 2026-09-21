package com.aguasarah.terceiros.venda;

import com.aguasarah.terceiros.produto.ProdutoTerceiro;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_venda_terceiro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemVendaTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    @JsonBackReference
    private VendaTerceiro venda;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private ProdutoTerceiro produto;

    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}
