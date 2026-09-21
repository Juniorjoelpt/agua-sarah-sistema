package com.aguasarah.terceiros.contareceber;

import com.aguasarah.terceiros.cliente.ClienteTerceiro;
import com.aguasarah.terceiros.venda.VendaTerceiro;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// Divida de uma empresa terceira com a Agua Sarah - nasce de uma venda
// vendida parte ou totalmente fiado, ou pode ser lancada manualmente.
@Entity
@Table(name = "contas_receber_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaReceberTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private ClienteTerceiro cliente;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    private VendaTerceiro venda; // opcional - se nasceu de uma venda com parte fiado

    private String descricao;

    private BigDecimal valorOriginal;

    @Builder.Default
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusContaReceberTerceiro status = StatusContaReceberTerceiro.ABERTA;

    private LocalDate dataCriacao;
    private LocalDate dataVencimento;
}
