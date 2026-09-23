package com.aguasarah.orcamento;

import com.aguasarah.cliente.Cliente;
import com.aguasarah.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Orcamento e um documento de proposta pro cliente - nao afeta caixa, estoque
// nem nenhum outro modulo. So vira algo "de verdade" quando o cliente aceita
// e uma Venda de fato e registrada separadamente.
@Entity
@Table(name = "orcamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente; // opcional - pode ser um orcamento pra alguem ainda nao cadastrado

    private String nomeClienteAvulso;   // usado quando cliente == null
    private String telefoneClienteAvulso;

    private LocalDate dataCriacao;
    private LocalDate validoAte; // opcional

    @Column(length = 1000)
    private String observacoes;

    @Builder.Default
    private BigDecimal valorBruto = BigDecimal.ZERO; // soma dos itens, antes do desconto % de cada um

    @Builder.Default
    private BigDecimal valorDescontoItens = BigDecimal.ZERO; // soma dos descontos % aplicados item a item

    private BigDecimal valorTotal; // valorBruto - valorDescontoItens

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusOrcamento status = StatusOrcamento.PENDENTE;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Builder.Default
    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemOrcamento> itens = new ArrayList<>();
}
