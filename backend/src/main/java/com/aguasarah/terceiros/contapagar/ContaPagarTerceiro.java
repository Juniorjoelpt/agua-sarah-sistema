package com.aguasarah.terceiros.contapagar;

import com.aguasarah.terceiros.despesa.CategoriaDespesaTerceiro;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contas_pagar_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaPagarTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private CategoriaDespesaTerceiro categoria;

    private BigDecimal valorOriginal;

    @Builder.Default
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusContaPagarTerceiro status = StatusContaPagarTerceiro.ABERTA;

    private LocalDate dataCriacao;
    private LocalDate dataVencimento;

    private String observacao;
}
