package com.aguasarah.contareceber;

import com.aguasarah.caixa.Caixa;
import com.aguasarah.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Cada baixa (total ou parcial) de uma conta a receber - o dinheiro entra de
// verdade no caixa do dia, por isso sempre precisa de um caixa aberto.
@Entity
@Table(name = "pagamentos_conta_receber")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoContaReceber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conta_receber_id")
    @JsonBackReference
    private ContaReceber contaReceber;

    @ManyToOne
    @JoinColumn(name = "caixa_id")
    private Caixa caixa;

    private BigDecimal valorEspecie;
    private BigDecimal valorPix;

    private LocalDateTime data;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
