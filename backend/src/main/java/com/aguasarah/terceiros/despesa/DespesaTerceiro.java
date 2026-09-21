package com.aguasarah.terceiros.despesa;

import com.aguasarah.terceiros.caixa.CaixaTerceiro;
import com.aguasarah.terceiros.contapagar.ContaPagarTerceiro;
import com.aguasarah.terceiros.frota.CarregamentoTerceiro;
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

// Despesa isolada do modulo de terceiros - mesma logica da Despesa do sistema
// principal (o "modulo de Despesas" completo, com tela propria, ainda vai ser
// construido; por enquanto essa entidade ja existe pra suportar o pagamento
// de uma ContaPagarTerceiro).
@Entity
@Table(name = "despesas_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DespesaTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CategoriaDespesaTerceiro categoria;

    @NotNull
    private BigDecimal valor;

    @NotNull
    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "caixa_terceiro_id")
    private CaixaTerceiro caixaTerceiro; // opcional - se saiu do caixa de terceiros do dia

    @ManyToOne
    @JoinColumn(name = "conta_pagar_id")
    private ContaPagarTerceiro contaPagar; // opcional - se essa despesa e a baixa de uma conta a pagar

    // opcional - despesa de um carregamento especifico (combustivel, pedagio...);
    // essas NAO tem caixaTerceiro setado, pra nao contar duas vezes no resumo do
    // caixa (o valor do carregamento inteiro ja debitou o caixa na hora de carregar)
    @ManyToOne
    @JoinColumn(name = "carregamento_id")
    private CarregamentoTerceiro carregamento;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
