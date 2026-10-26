package br.senac.xanxere.seguranca.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Teste unitário puro: não sobe o contexto do Spring, só instancia as classes.
 * Verifica a ponte entre o nosso Usuario e o UserDetails que o Spring Security consome.
 */
class UsuarioDetailsServiceTest {

    private PasswordEncoder encoder;
    private UsuarioDetailsService service;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        service = new UsuarioDetailsService(new UsuarioRepository(encoder));
    }

    @Test
    @DisplayName("carrega o usuário com os papéis convertidos em authorities ROLE_*")
    void carregaUsuarioComPapeis() {
        UserDetails admin = service.loadUserByUsername("admin");

        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_PROFESSOR");
    }

    @Test
    @DisplayName("a senha guardada é um hash BCrypt que confere com a senha original")
    void senhaGuardadaEhHashBcrypt() {
        UserDetails aluno = service.loadUserByUsername("aluno");

        assertThat(aluno.getPassword()).startsWith("$2").isNotEqualTo("aluno123");
        assertThat(encoder.matches("aluno123", aluno.getPassword())).isTrue();
        assertThat(encoder.matches("outra", aluno.getPassword())).isFalse();
    }

    @Test
    @DisplayName("usuário desconhecido lança UsernameNotFoundException")
    void usuarioDesconhecidoLancaExcecao() {
        assertThatThrownBy(() -> service.loadUserByUsername("ninguem"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ninguem");
    }
}
