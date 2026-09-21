package com.aguasarah.terceiros.frota;

import com.aguasarah.terceiros.cliente.ClienteTerceiro;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Caminhao de uma empresa terceira (frota do proprio cliente) - cadastrado
// aqui so pra poder ser escolhido na hora do carregamento, em vez de digitar
// a placa em texto livre toda vez.
@Entity
@Table(name = "caminhoes_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaminhaoTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private ClienteTerceiro cliente;

    @NotBlank
    private String placa;

    private String motorista;

    @Builder.Default
    private boolean ativo = true;
}
