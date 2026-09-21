package com.aguasarah.estoque;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "insumos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome; // Lacre, Rotulo, Selo, Tampa, Luva, Mascara, Touca, Bloco de anotacao

    private String unidadeMedida; // un, caixa, pacote

    @Builder.Default
    private Integer quantidadeAtual = 0;

    @Builder.Default
    private Integer quantidadeMinima = 0;
}
