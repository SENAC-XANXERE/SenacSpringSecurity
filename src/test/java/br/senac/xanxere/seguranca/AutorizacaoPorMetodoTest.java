package br.senac.xanxere.seguranca;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AUTORIZAÇÃO por MÉTODO: a regra @PreAuthorize de NotasController.
 *
 * "hasAnyRole('PROFESSOR', 'ADMIN') or #aluno == authentication.name"
 */
@SpringBootTest
@AutoConfigureMockMvc
class AutorizacaoPorMetodoTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("aluno vê as próprias notas")
    void alunoVeAsPropriasNotas() throws Exception {
        mvc.perform(get("/api/notas/aluno").with(httpBasic("aluno", "aluno123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Programação").value(8.5));
    }

    @Test
    @DisplayName("aluno não vê as notas de outro aluno (403)")
    void alunoNaoVeNotasDeOutro() throws Exception {
        mvc.perform(get("/api/notas/outro").with(httpBasic("aluno", "aluno123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("professor vê as notas de qualquer aluno")
    void professorVeNotasDeQualquerAluno() throws Exception {
        mvc.perform(get("/api/notas/aluno").with(httpBasic("professor", "prof123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("admin vê as notas de qualquer aluno")
    void adminVeNotasDeQualquerAluno() throws Exception {
        mvc.perform(get("/api/notas/aluno").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("sem login continua sendo 401, mesmo com regra de método")
    void semLoginRetorna401() throws Exception {
        mvc.perform(get("/api/notas/aluno"))
                .andExpect(status().isUnauthorized());
    }
}
