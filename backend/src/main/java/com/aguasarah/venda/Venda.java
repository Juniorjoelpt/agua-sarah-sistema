package com.aguasarah.venda;

import com.aguasarah.caixa.Caixa;
import com.aguasarah.cliente.Cliente;
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

@Entity
@Table(name = "vendas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caixa_id")
    private Caixa caixa;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime dataHora;

    // pagamento pode ser dividido entre os dois - juntos precisam somar o valorTotal
    // (validado no VendaService). Uma venda so em PIX simplesmente tem valorRecebidoEspecie = 0.
    private BigDecimal valorRecebidoEspecie;
    private BigDecimal valorRecebidoPix;

    // guardado aqui so pra exibicao nas listagens - a fonte de verdade da divida
    // (status, pagamentos parciais) e a ContaReceber gerada a partir desta venda
    private BigDecimal valorFiado;

    @Enumerated(EnumType.STRING)
    private Ocorrencia ocorrencia;

    // quantidade de galoes com avaria - preenchida sempre que ocorrencia != NENHUMA, so para historico
    private Integer quantidadeAvarias;

    // quantidade de galoes dados de bonificacao - independente da quantidade de avarias
    // (a empresa pode optar por bonificar menos galoes do que os avariados); so se aplica
    // quando ocorrencia = AVARIA_PRODUCAO
    private Integer quantidadeBonificados;

    private BigDecimal valorBruto;      // soma dos itens, antes de qualquer desconto
    private BigDecimal valorAvaria;     // quantidadeAvarias x preco do galao - desconto por avaria (cliente ou producao)
    private BigDecimal valorBonificado; // quantidadeBonificados x preco do galao - desconto adicional, so na avaria de producao
    private BigDecimal valorTotal;      // valorBruto - valorAvaria - valorBonificado (o que o cliente efetivamente paga)

    @Column(length = 500)
    private String observacao; // texto livre, opcional - qualquer detalhe que o operador queira registrar

    @Builder.Default
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemVenda> itens = new ArrayList<>();
}
