package com.aguasarah.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDTO(
        @NotBlank String nome,
        @NotBlank String login,
        String senha, // opcional em edicao (se vazio, mantem a senha atual)
        @NotNull Perfil perfil,
        boolean ativo
) {}
