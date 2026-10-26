package br.senac.xanxere.seguranca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Configuração central do Spring Security.
 *
 * AUTENTICAÇÃO = provar quem você é (login + senha).
 * AUTORIZAÇÃO  = decidir o que você pode fazer (papéis / regras).
 *
 * Resultado esperado:
 *  - sem credenciais ou credenciais erradas -> 401 Unauthorized
 *  - credenciais certas, mas sem permissão  -> 403 Forbidden
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize nos métodos (ver NotasController)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API REST sem sessão/cookie: cada requisição traz suas credenciais.
            // Por isso o CSRF (ataque baseado em cookie de sessão) pode ser desligado.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ---------- AUTORIZAÇÃO por URL ----------
            // As regras são avaliadas na ordem: a primeira que casar com a URL vence.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/publico").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/professor/**").hasAnyRole("PROFESSOR", "ADMIN")
                .requestMatchers("/api/aluno/**").hasAnyRole("ALUNO", "PROFESSOR", "ADMIN")
                .anyRequest().authenticated()
            )

            // ---------- AUTENTICAÇÃO ----------
            // HTTP Basic: o cliente envia "Authorization: Basic base64(login:senha)".
            .httpBasic(Customizer.withDefaults())

            // Sem credenciais: responder 401 "seco", sem o popup do navegador (header WWW-Authenticate).
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    /**
     * BCrypt gera um hash diferente a cada chamada (salt aleatório) e é propositalmente lento,
     * o que dificulta ataques de força bruta. O Spring usa este bean para comparar senhas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
