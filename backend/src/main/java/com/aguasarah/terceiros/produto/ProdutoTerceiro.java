package com.aguasarah.terceiros.produto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Produto vendido no fluxo de terceiros - cadastro isolado do Produto do
// sistema principal (mesmo que represente o mesmo galao fisico, o preco
// praticado com empresas terceiras costuma ser outro).
@Entity
@Table(name = "produtos_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotNull
    private BigDecimal preco;

    @Builder.Default
    private boolean ativo = true;
}
