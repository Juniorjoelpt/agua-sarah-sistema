package com.aguasarah.terceiros.cliente;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Empresa terceira (com frota propria) que compra galoes prontos da Agua
// Sarah pra revenda - cadastro isolado do Cliente do sistema principal.
@Entity
@Table(name = "clientes_terceiros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    private String telefone;
    private String bairro;
    private String endereco;

    @Builder.Default
    private boolean ativo = true;
}
