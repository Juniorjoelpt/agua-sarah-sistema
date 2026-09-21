package com.aguasarah.usuario;

public record UsuarioResponseDTO(Long id, String nome, String login, Perfil perfil, boolean ativo) {
    public static UsuarioResponseDTO de(Usuario u) {
        return new UsuarioResponseDTO(u.getId(), u.getNome(), u.getLogin(), u.getPerfil(), u.isAtivo());
    }
}
