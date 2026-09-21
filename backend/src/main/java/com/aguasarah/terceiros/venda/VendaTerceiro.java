package com.aguasarah.terceiros.venda;

import com.aguasarah.terceiros.caixa.CaixaTerceiro;
import com.aguasarah.terceiros.cliente.ClienteTerceiro;
import com.aguasarah.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Venda de galoes prontos para uma empresa terceira (com frota propria) que
// vai revender. Isolada do Venda do sistema principal.
@Entity
@Table(name = "vendas_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendaTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caixa_terceiro_id")
    private CaixaTerceiro caixaTerceiro;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private ClienteTerceiro cliente;

    private String placaCaminhao; // opcional - qual caminhao do cliente veio buscar

    private LocalDateTime dataHora;

    private BigDecimal valorRecebidoEspecie;
    private BigDecimal valorRecebidoPix;
    private BigDecimal valorFiado; // guardado so pra exibicao - fonte de verdade e a ContaReceberTerceiro gerada
    private BigDecimal valorTotal;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Builder.Default
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemVendaTerceiro> itens = new ArrayList<>();
}
