package com.aguasarah.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PontoVendaDiaDTO(LocalDate data, BigDecimal totalVendido, int quantidadeVendas) {}
