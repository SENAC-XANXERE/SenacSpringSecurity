package br.senac.xanxere.seguranca.usuario;

import java.util.Set;

/**
 * Usuário da aplicação. Em um sistema real seria uma entidade JPA;
 * aqui é um record simples para manter o foco no Spring Security.
 *
 * @param senhaHash senha já codificada com BCrypt (nunca guarde a senha em texto puro)
 */
public record Usuario(String login, String senhaHash, Set<Papel> papeis) {
}
