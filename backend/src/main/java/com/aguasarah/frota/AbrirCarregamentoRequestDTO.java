package com.aguasarah.frota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AbrirCarregamentoRequestDTO(
        @NotNull Long caminhaoId,
        @NotBlank String rota,
        @NotNull @Positive Integer quantidadeCarregada,
        // preco por galao combinado com o caminhao - nao e o preco de envase do PDV,
        // o caminhao compra como um cliente final "no balcao"
        @NotNull @Positive BigDecimal precoVenda
) {}
