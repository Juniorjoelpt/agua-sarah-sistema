package com.aguasarah.contareceber;

import java.math.BigDecimal;

public record PagamentoContaReceberRequestDTO(BigDecimal valorEspecie, BigDecimal valorPix) {}
