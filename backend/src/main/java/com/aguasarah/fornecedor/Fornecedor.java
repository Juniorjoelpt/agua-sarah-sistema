package com.aguasarah.fornecedor;

import com.aguasarah.estoque.Insumo;
import com.aguasarah.produto.Produto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "fornecedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    private String nomeContato; // pessoa de contato na empresa fornecedora, opcional
    private String telefone;
    private String endereco;

    @Builder.Default
    private boolean ativo = true;

    // o que esse fornecedor fornece - produtos acabados e/ou insumos de producao
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "fornecedor_produtos",
            joinColumns = @JoinColumn(name = "fornecedor_id"),
            inverseJoinColumns = @JoinColumn(name = "produto_id")
    )
    private Set<Produto> produtosFornecidos = new HashSet<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "fornecedor_insumos",
            joinColumns = @JoinColumn(name = "fornecedor_id"),
            inverseJoinColumns = @JoinColumn(name = "insumo_id")
    )
    private Set<Insumo> insumosFornecidos = new HashSet<>();
}
