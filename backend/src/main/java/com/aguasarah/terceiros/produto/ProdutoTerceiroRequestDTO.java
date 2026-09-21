package com.aguasarah.terceiros.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoTerceiroRequestDTO(@NotBlank String nome, @NotNull BigDecimal preco, boolean ativo) {}
