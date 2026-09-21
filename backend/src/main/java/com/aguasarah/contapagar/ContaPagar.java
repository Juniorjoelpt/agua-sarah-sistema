package com.aguasarah.contapagar;

import com.aguasarah.despesa.CategoriaDespesa;
import com.aguasarah.fornecedor.Fornecedor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// Uma obrigacao de pagamento com data de vencimento - nasce sempre lancada
// manualmente (conta de luz, boleto de fornecedor, aluguel...). So vira
// dinheiro saindo de verdade quando um pagamento e registrado (gera Despesa).
@Entity
@Table(name = "contas_pagar")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaPagar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor; // opcional - nem toda conta a pagar tem um fornecedor cadastrado

    private String descricao;

    @Enumerated(EnumType.STRING)
    private CategoriaDespesa categoria;

    private BigDecimal valorOriginal;

    @Builder.Default
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusContaPagar status = StatusContaPagar.ABERTA;

    private LocalDate dataCriacao;
    private LocalDate dataVencimento;

    private String observacao;
}
