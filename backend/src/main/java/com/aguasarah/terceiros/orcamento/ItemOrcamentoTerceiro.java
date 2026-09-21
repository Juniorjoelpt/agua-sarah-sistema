package com.aguasarah.terceiros.orcamento;

import com.aguasarah.terceiros.produto.ProdutoTerceiro;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_orcamento_terceiro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOrcamentoTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orcamento_id")
    @JsonBackReference
    private OrcamentoTerceiro orcamento;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private ProdutoTerceiro produto;

    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}
