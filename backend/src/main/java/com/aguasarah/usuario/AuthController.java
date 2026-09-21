package com.aguasarah.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aguasarah.security.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public record LoginRequest(String login, String senha) {}
    public record LoginResponse(String token, UsuarioResponseDTO usuario) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.senha())
        );

        Usuario usuario = usuarioRepository.findByLogin(request.login())
                .orElseThrow(() -> new IllegalStateException("Usuario nao encontrado apos autenticacao"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(usuario.getLogin())
                .password(usuario.getSenha())
                .authorities("ROLE_" + usuario.getPerfil().name())
                .build();

        String token = jwtService.gerarToken(userDetails, Map.of(
                "perfil", usuario.getPerfil().name(),
                "nome", usuario.getNome()
        ));

        return new LoginResponse(token, UsuarioResponseDTO.de(usuario));
    }
}
