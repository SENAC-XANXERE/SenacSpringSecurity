package br.senac.xanxere.seguranca.usuario;

/**
 * Papéis (roles) do sistema.
 *
 * No Spring Security um papel é apenas uma "authority" com o prefixo ROLE_.
 * Ou seja: hasRole("ADMIN") é o mesmo que hasAuthority("ROLE_ADMIN").
 */
public enum Papel {
    ALUNO,
    PROFESSOR,
    ADMIN
}
