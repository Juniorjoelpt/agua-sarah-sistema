package com.aguasarah.usuario;

import com.aguasarah.common.RegraNegocioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponseDTO::de).toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        return UsuarioResponseDTO.de(buscarEntidade(id));
    }

    public Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado: " + id));
    }

    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByLogin(dto.login())) {
            throw new RegraNegocioException("Ja existe um usuario com esse login");
        }
        if (dto.senha() == null || dto.senha().isBlank()) {
            throw new RegraNegocioException("Senha e obrigatoria para novo usuario");
        }
        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .login(dto.login())
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(dto.perfil())
                .ativo(dto.ativo())
                .build();
        return UsuarioResponseDTO.de(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = buscarEntidade(id);
        usuario.setNome(dto.nome());
        usuario.setLogin(dto.login());
        usuario.setPerfil(dto.perfil());
        usuario.setAtivo(dto.ativo());
        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }
        return UsuarioResponseDTO.de(usuarioRepository.save(usuario));
    }

    public void inativar(Long id) {
        Usuario usuario = buscarEntidade(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}
