package com.aguasarah.terceiros.caixa;

import com.aguasarah.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Caixa proprio do modulo de terceiros - totalmente isolado do Caixa do
// sistema principal (mesmo padrao ja usado antes: nenhuma FK cruzada).
@Entity
@Table(name = "caixas_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaixaTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;

    private BigDecimal saldoInicialEspecie;
    private BigDecimal saldoInicialPix;

    @Enumerated(EnumType.STRING)
    private StatusCaixaTerceiro status;

    @ManyToOne
    @JoinColumn(name = "usuario_abertura_id")
    private Usuario usuarioAbertura;
}
