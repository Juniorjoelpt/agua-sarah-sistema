package com.aguasarah.precocliente;

import com.aguasarah.cliente.Cliente;
import com.aguasarah.produto.Produto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Preco personalizado de um produto especifico para um cliente especifico -
// sobrescreve o Produto.preco padrao so nas vendas/orcamentos desse cliente.
// Se nao existir um registro aqui para (cliente, produto), usa o preco padrao.
@Entity
@Table(name = "precos_cliente", uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "produto_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrecoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private BigDecimal preco;
}
