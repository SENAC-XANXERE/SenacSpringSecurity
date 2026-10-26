package br.senac.xanxere.seguranca.usuario;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

/**
 * "Banco de dados" em memória com três usuários de exemplo.
 *
 * Em um projeto real este seria um JpaRepository. O que importa para o
 * Spring Security é só o método {@link #buscarPorLogin(String)}.
 */
@Repository
public class UsuarioRepository {

    private final Map<String, Usuario> usuarios;

    public UsuarioRepository(PasswordEncoder encoder) {
        this.usuarios = Map.of(
                "aluno", new Usuario("aluno", encoder.encode("aluno123"), Set.of(Papel.ALUNO)),
                "professor", new Usuario("professor", encoder.encode("prof123"), Set.of(Papel.PROFESSOR)),
                "admin", new Usuario("admin", encoder.encode("admin123"), Set.of(Papel.ADMIN, Papel.PROFESSOR))
        );
    }

    public Optional<Usuario> buscarPorLogin(String login) {
        return Optional.ofNullable(usuarios.get(login));
    }
}
