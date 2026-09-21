package com.aguasarah.frota;

import com.aguasarah.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prestacoes_contas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestacaoContas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "carregamento_id", unique = true)
    private Carregamento carregamento;

    private Integer quantidadeAvaria;
    private BigDecimal valorAvaria; // quantidadeAvaria x precoVenda do carregamento

    private Integer quantidadeDevolvida; // galoes que voltaram sem vender

    // calculado: quantidadeCarregada - quantidadeAvaria - quantidadeDevolvida
    private Integer quantidadeVendida;

    private BigDecimal valorRecebidoEspecie;
    private BigDecimal valorRecebidoPix;

    // (quantidadeVendida x precoVenda) - (recebido especie + recebido pix), para conferencia
    private BigDecimal diferenca;

    // soma das despesas lancadas nesse carregamento (combustivel, pedagio...)
    private BigDecimal totalDespesas;

    // (quantidadeVendida x precoVenda) - totalDespesas - o quanto o caminhao rendeu de fato
    private BigDecimal lucro;

    private LocalDateTime dataPrestacao;

    @ManyToOne
    @JoinColumn(name = "usuario_conferencia_id")
    private Usuario usuarioConferencia;
}
