package com.aguasarah.despesa;

import com.aguasarah.caixa.Caixa;
import com.aguasarah.contapagar.ContaPagar;
import com.aguasarah.fornecedor.Fornecedor;
import com.aguasarah.frota.Carregamento;
import com.aguasarah.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "despesas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CategoriaDespesa categoria;

    @NotNull
    private BigDecimal valor;

    @NotNull
    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "caixa_id")
    private Caixa caixa; // opcional - se a despesa saiu do caixa do dia

    // opcional - se a despesa e de um carregamento/viagem especifico (combustivel, pedagio...),
    // entra no calculo do lucro daquele carregamento na prestacao de contas
    @ManyToOne
    @JoinColumn(name = "carregamento_id")
    private Carregamento carregamento;

    // opcional - se a despesa e uma compra feita de um fornecedor especifico,
    // entra no historico de compras daquele fornecedor
    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    // opcional - se essa despesa e a baixa (total ou parcial) de uma conta a pagar
    @ManyToOne
    @JoinColumn(name = "conta_pagar_id")
    private ContaPagar contaPagar;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
