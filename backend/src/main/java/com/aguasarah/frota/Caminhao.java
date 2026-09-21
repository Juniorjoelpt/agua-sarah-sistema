package com.aguasarah.frota;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "caminhoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caminhao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String placa;

    // relacao fixa: cada motorista sempre dirige o mesmo caminhao
    @NotNull
    @OneToOne
    @JoinColumn(name = "motorista_id", unique = true)
    private Motorista motorista;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoRota tipoRota;

    @Builder.Default
    private boolean ativo = true;
}
