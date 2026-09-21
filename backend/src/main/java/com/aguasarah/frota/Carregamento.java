package com.aguasarah.frota;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// Carregamento de agua de um caminhao para uma rota - vale para caminhoes de
// rota FIXA ou VARIAVEL (os dois tipos carregam agua e precisam prestar contas).
@Entity
@Table(name = "carregamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carregamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caminhao_id")
    private Caminhao caminhao;

    private LocalDate dataCarregamento;

    private String rota; // texto livre, ex: "Zona Norte", "Bairro Centro"

    private Integer quantidadeCarregada;

    // preco de venda por galao PARA O CAMINHAO, informado no cadastro do carregamento -
    // e um valor proprio, diferente do preco de envase praticado no PDV (o caminhao
    // funciona como um comprador final, comprando "no balcao" a um preco combinado)
    private BigDecimal precoVenda;

    // quantidadeCarregada x precoVenda - valor total da venda dos galoes para o caminhao
    private BigDecimal valorCarregamento;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusCarregamento status = StatusCarregamento.PENDENTE;
}
