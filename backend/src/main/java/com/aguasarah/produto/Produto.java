package com.aguasarah.produto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotNull
    private BigDecimal preco;

    // marca se esse produto conta como "envase de agua" para o relatorio
    // de envase (saida de agua = galoes vendidos)
    @Builder.Default
    private boolean contaComoEnvase = false;

    @Builder.Default
    private boolean ativo = true;
}
