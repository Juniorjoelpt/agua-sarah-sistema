package com.aguasarah.fluxocaixa;

import com.aguasarah.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// Uma entrada ou saida de uma conta bancaria. Hoje sempre nasce de lancamento
// manual (origem = MANUAL); quando a integracao com a Pluggy for plugada, os
// lancamentos importados do extrato virao com origem = AUTOMATICO e
// idTransacaoExterna preenchido (usado para nao importar a mesma transacao
// duas vezes numa nova sincronizacao).
@Entity
@Table(name = "lancamentos_fluxo_caixa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LancamentoFluxoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conta_bancaria_id")
    private ContaBancaria contaBancaria;

    @Enumerated(EnumType.STRING)
    private TipoLancamento tipo;

    private String descricao;
    private String categoria; // texto livre por enquanto (ex: "Venda", "Fornecedor", "Tarifa")
    private BigDecimal valor;
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrigemLancamento origem = OrigemLancamento.MANUAL;

    // preenchido so em lancamentos importados automaticamente - identificador
    // unico da transacao na Pluggy, usado para evitar duplicidade
    @Column(unique = true)
    private String idTransacaoExterna;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // nulo em lancamentos automaticos
}
