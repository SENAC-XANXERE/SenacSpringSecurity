package br.senac.xanxere.seguranca.usuario;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Ponte entre o NOSSO modelo de usuário e o que o Spring Security entende.
 *
 * Fluxo de AUTENTICAÇÃO (quem é você?):
 *  1. A requisição chega com login/senha (aqui via HTTP Basic).
 *  2. O BasicAuthenticationFilter monta um UsernamePasswordAuthenticationToken
 *     e entrega ao AuthenticationManager.
 *  3. O DaoAuthenticationProvider chama ESTE serviço para carregar o usuário.
 *  4. O PasswordEncoder compara a senha enviada com o hash guardado.
 *  5. Se bater, o Authentication (agora "autenticado", com as authorities)
 *     é colocado no SecurityContextHolder para o resto da requisição.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = repository.buscarPorLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));

        // User.builder().roles("ADMIN") gera a authority "ROLE_ADMIN" automaticamente.
        return User.builder()
                .username(usuario.login())
                .password(usuario.senhaHash())
                .roles(usuario.papeis().stream().map(Enum::name).toArray(String[]::new))
                .build();
    }
}
