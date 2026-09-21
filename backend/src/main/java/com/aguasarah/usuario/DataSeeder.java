package com.aguasarah.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Cria o usuario administrador padrao na primeira subida do backend,
// para nao depender de INSERT manual no banco.
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-login}")
    private String adminLogin;

    @Value("${app.seed.admin-senha}")
    private String adminSenha;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .login(adminLogin)
                    .senha(passwordEncoder.encode(adminSenha))
                    .perfil(Perfil.ADMIN)
                    .ativo(true)
                    .build();
            usuarioRepository.save(admin);
            System.out.println(">> Usuario admin criado: login=" + adminLogin + " senha=" + adminSenha);
        }
    }
}
