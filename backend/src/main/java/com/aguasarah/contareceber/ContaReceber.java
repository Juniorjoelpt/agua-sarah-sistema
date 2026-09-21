package com.aguasarah.contareceber;

import com.aguasarah.cliente.Cliente;
import com.aguasarah.venda.Venda;
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

// Divida de um cliente com a empresa - nasce de uma venda vendida parte ou
// totalmente "fiado", ou pode ser lancada manualmente (divida antiga, por exemplo).
// So existe pra clientes cadastrados - nao faz sentido "fiado" pra consumidor avulso.
@Entity
@Table(name = "contas_receber")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaReceber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda; // opcional - se nasceu de uma venda com parte fiado

    private String descricao;

    private BigDecimal valorOriginal;

    @Builder.Default
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusContaReceber status = StatusContaReceber.ABERTA;

    private LocalDate dataCriacao;
    private LocalDate dataVencimento; // opcional

    @Builder.Default
    @OneToMany(mappedBy = "contaReceber", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PagamentoContaReceber> pagamentos = new ArrayList<>();
}
