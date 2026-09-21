package com.aguasarah.frota;

import com.aguasarah.cliente.Cliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// So faz sentido quando Caminhao.tipoRota = FIXA: define quais clientes
// (pontos pre-definidos) aquele caminhao atende.
@Entity
@Table(name = "clientes_rota_fixa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRotaFixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caminhao_id")
    private Caminhao caminhao;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
