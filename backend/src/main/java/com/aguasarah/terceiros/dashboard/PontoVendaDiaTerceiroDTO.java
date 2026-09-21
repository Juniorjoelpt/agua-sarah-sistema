package com.aguasarah.terceiros.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PontoVendaDiaTerceiroDTO(LocalDate data, BigDecimal totalVendido, int quantidadeVendas) {}
