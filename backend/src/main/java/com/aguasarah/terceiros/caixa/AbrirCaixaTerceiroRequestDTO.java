package com.aguasarah.terceiros.caixa;

import java.math.BigDecimal;

public record AbrirCaixaTerceiroRequestDTO(BigDecimal saldoInicialEspecie, BigDecimal saldoInicialPix) {}
