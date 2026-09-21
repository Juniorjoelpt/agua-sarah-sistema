package com.aguasarah.usuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    @Column(unique = true)
    private String login;

    @NotBlank
    @JsonIgnore // impede que o hash da senha vaze em qualquer resposta que inclua um Usuario (ex: Venda.usuario, Caixa.usuarioAbertura)
    private String senha; // hash bcrypt

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    @Builder.Default
    private boolean ativo = true;
}
