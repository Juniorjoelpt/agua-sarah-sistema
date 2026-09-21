package com.aguasarah.frota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CaminhaoRequestDTO(
        @NotBlank String placa,
        @NotBlank String nomeMotorista,
        String telefoneMotorista,
        @NotNull TipoRota tipoRota,
        List<Long> clienteIdsRotaFixa // usado so quando tipoRota = FIXA
) {}
