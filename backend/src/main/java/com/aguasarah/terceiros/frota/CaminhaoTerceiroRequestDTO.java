package com.aguasarah.terceiros.frota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CaminhaoTerceiroRequestDTO(
        @NotNull Long clienteId,
        @NotBlank String placa,
        String motorista,
        boolean ativo
) {}
