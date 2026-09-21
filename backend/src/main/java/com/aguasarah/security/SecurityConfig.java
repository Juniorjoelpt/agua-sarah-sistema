package com.aguasarah.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize nos controllers (usado no modulo de terceiros)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // sem isso, o Spring Security cai no comportamento padrao (403) para requisicao
            // sem autenticacao valida - o frontend so sabe tratar "sessao expirada" no 401
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                // modulos restritos a ADMIN - reforcado tambem via @PreAuthorize em cada controller.
                // As excecoes de leitura pontual abaixo precisam vir ANTES da regra geral do mesmo
                // modulo, senao a regra geral (mais generica) e avaliada primeiro e barra tudo.
                .requestMatchers("/api/terceiros/**").hasRole("ADMIN")
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                // Contas a Receber: a listagem fica aberta (tela de Clientes usa pra mostrar
                // o saldo devedor no historico), o resto (buscar por id, criar, pagamentos) e ADMIN
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/contas-receber").authenticated()
                .requestMatchers("/api/contas-receber/**").hasRole("ADMIN")

                // Contas a Pagar: so o alerta de vencidas fica aberto (usado no Painel)
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/contas-pagar/vencidas").authenticated()
                .requestMatchers("/api/contas-pagar/**").hasRole("ADMIN")

                // Fornecedores: a listagem fica aberta (seletor de fornecedor na tela de Estoque)
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/fornecedores").authenticated()
                .requestMatchers("/api/fornecedores/**").hasRole("ADMIN")

                .requestMatchers("/api/despesas/**").hasRole("ADMIN")
                .requestMatchers("/api/relatorios/**").hasRole("ADMIN")
                .requestMatchers("/api/fluxo-caixa/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Sessão inválida ou expirada");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
