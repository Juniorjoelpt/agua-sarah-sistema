package com.aguasarah.estoque;

import com.aguasarah.fornecedor.Fornecedor;
import com.aguasarah.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_insumo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacaoInsumo tipo;

    private Integer quantidade;
    private LocalDateTime data;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // opcional - preenchido quando a entrada veio de um fornecedor especifico;
    // e o que compoe o historico de fornecimento na tela de Fornecedores
    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    private String observacao;
}
