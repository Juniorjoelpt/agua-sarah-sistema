package com.aguasarah.terceiros.orcamento;

import com.aguasarah.terceiros.cliente.ClienteTerceiro;
import com.aguasarah.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Orcamento isolado do modulo de terceiros - proposta pra uma empresa
// terceira cadastrada (aqui sempre exige cliente, nao existe "avulso" como
// no orcamento principal, ja que sao sempre parceiros conhecidos com frota).
@Entity
@Table(name = "orcamentos_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrcamentoTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private ClienteTerceiro cliente;

    private LocalDate dataCriacao;
    private LocalDate validoAte;

    @Column(length = 1000)
    private String observacoes;

    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusOrcamentoTerceiro status = StatusOrcamentoTerceiro.PENDENTE;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Builder.Default
    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemOrcamentoTerceiro> itens = new ArrayList<>();
}
