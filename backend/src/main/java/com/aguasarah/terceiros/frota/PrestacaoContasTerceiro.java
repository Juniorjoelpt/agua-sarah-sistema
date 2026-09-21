package com.aguasarah.terceiros.frota;

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
@Table(name = "prestacoes_contas_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestacaoContasTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "carregamento_id", unique = true)
    private CarregamentoTerceiro carregamento;

    // caixa de terceiros que estava aberto quando a prestacao foi feita -
    // e nele que o lucro dessa viagem e creditado
    @ManyToOne
    @JoinColumn(name = "caixa_terceiro_id")
    private CaixaTerceiro caixaTerceiro;

    private Integer quantidadeAvaria;
    private BigDecimal valorAvaria;
    private Integer quantidadeDevolvida;
    private Integer quantidadeVendida;

    private BigDecimal totalDespesas;
    private BigDecimal lucro;

    private LocalDateTime dataPrestacao;

    @ManyToOne
    @JoinColumn(name = "usuario_conferencia_id")
    private Usuario usuarioConferencia;
}
