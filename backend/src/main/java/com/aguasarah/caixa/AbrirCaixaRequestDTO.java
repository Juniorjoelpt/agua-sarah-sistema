package com.aguasarah.caixa;

import java.math.BigDecimal;

public record AbrirCaixaRequestDTO(BigDecimal saldoInicialEspecie, BigDecimal saldoInicialPix) {}
