package com.aguasarah.fluxocaixa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Uma conta bancaria da empresa, cadastrada para acompanhar o fluxo de caixa.
// Os campos pluggy* ficam prontos para quando a integracao automatica com a
// Pluggy (Open Finance) for plugada - por enquanto sempre nulos, e todo
// lancamento e feito manualmente pela tela.
@Entity
@Table(name = "contas_bancarias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String apelido; // ex: "Conta principal", "Conta Caixa Econômica"

    private String banco;
    private String agencia;
    private String numeroConta;
    private String chavePix; // so informativo, nao usado em nenhuma integracao ainda

    @Builder.Default
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @Builder.Default
    private boolean ativa = true;

    // ---- preparado para a integracao futura com a Pluggy (Open Finance) ----
    // nenhum destes e preenchido hoje - ficam aqui so para nao precisar de
    // migração de banco quando a integração automática for construída
    private String pluggyItemId;     // identifica a conexao (o "item") feita no Pluggy Connect
    private String pluggyAccountId;  // identifica esta conta especifica dentro daquele item
    @Builder.Default
    private boolean conectadaAutomaticamente = false;
}
