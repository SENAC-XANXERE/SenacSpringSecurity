package br.senac.xanxere.seguranca.api;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints protegidos por URL (regras em SecurityConfig).
 * Os controllers não sabem nada de segurança: o filtro já barrou quem não podia chegar aqui.
 */
@RestController
@RequestMapping("/api")
public class AreaController {

    @GetMapping("/publico")
    public Map<String, String> publico() {
        return Map.of("mensagem", "Qualquer pessoa pode ver isto, sem login.");
    }

    /**
     * O Spring injeta o Authentication da requisição atual (o mesmo que está no SecurityContextHolder).
     */
    @GetMapping("/perfil")
    public Map<String, Object> perfil(Authentication authentication) {
        List<String> papeis = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Map.of("login", authentication.getName(), "papeis", papeis);
    }

    @GetMapping("/aluno")
    public Map<String, String> aluno() {
        return Map.of("mensagem", "Área do aluno: ALUNO, PROFESSOR ou ADMIN.");
    }

    @GetMapping("/professor")
    public Map<String, String> professor() {
        return Map.of("mensagem", "Área do professor: PROFESSOR ou ADMIN.");
    }

    @GetMapping("/admin")
    public Map<String, String> admin() {
        return Map.of("mensagem", "Área administrativa: somente ADMIN.");
    }
}
