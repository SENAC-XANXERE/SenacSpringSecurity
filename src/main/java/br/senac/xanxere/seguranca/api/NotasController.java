package br.senac.xanxere.seguranca.api;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Autorização por MÉTODO (@PreAuthorize), para regras que dependem dos dados da requisição.
 *
 * Aqui a URL /api/notas/{aluno} só exige "estar autenticado" (anyRequest().authenticated()),
 * mas a regra de negócio é: um aluno só vê as próprias notas; professor e admin veem as de todos.
 * Isso não dá para expressar só com padrão de URL, por isso usamos SpEL no método.
 */
@RestController
@RequestMapping("/api/notas")
public class NotasController {

    private static final Map<String, Map<String, Double>> NOTAS = Map.of(
            "aluno", Map.of("Programação", 8.5, "Banco de Dados", 7.0)
    );

    @GetMapping("/{aluno}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN') or #aluno == authentication.name")
    public Map<String, Double> notasDoAluno(@PathVariable String aluno) {
        return NOTAS.getOrDefault(aluno, Map.of());
    }
}
