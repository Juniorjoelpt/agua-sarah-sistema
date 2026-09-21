package com.aguasarah.terceiros.contareceber;

import com.aguasarah.terceiros.caixa.CaixaTerceiro;
import com.aguasarah.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos_conta_receber_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoContaReceberTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conta_receber_id")
    private ContaReceberTerceiro contaReceber;

    @ManyToOne
    @JoinColumn(name = "caixa_terceiro_id")
    private CaixaTerceiro caixaTerceiro;

    private BigDecimal valorEspecie;
    private BigDecimal valorPix;

    private LocalDateTime data;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
