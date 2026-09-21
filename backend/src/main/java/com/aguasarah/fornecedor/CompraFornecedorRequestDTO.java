package com.aguasarah.fornecedor;

import com.aguasarah.despesa.CategoriaDespesa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraFornecedorRequestDTO(
        @NotBlank String descricao,
        @NotNull CategoriaDespesa categoria,
        @NotNull BigDecimal valor,
        LocalDate data, // opcional - se nao vier, usa a data de hoje
        // opcionais - se preenchidos, essa compra tambem da entrada no estoque desse insumo
        Long insumoId,
        Integer quantidadeInsumo
) {}
