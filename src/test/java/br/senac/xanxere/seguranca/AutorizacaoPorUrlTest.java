package br.senac.xanxere.seguranca;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AUTORIZAÇÃO por URL: o que cada papel pode acessar?
 *
 * Aqui o usuário SEMPRE está autenticado; a única pergunta é 200 ou 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AutorizacaoPorUrlTest {

    @Autowired
    private MockMvc mvc;

    /**
     * Matriz completa: (usuário, endpoint) -> status esperado. Espelha a tabela do README.
     */
    static Stream<Arguments> matriz() {
        return Stream.of(
                Arguments.of("aluno", "aluno123", "/api/aluno", HttpStatus.OK),
                Arguments.of("aluno", "aluno123", "/api/professor", HttpStatus.FORBIDDEN),
                Arguments.of("aluno", "aluno123", "/api/admin", HttpStatus.FORBIDDEN),

                Arguments.of("professor", "prof123", "/api/aluno", HttpStatus.OK),
                Arguments.of("professor", "prof123", "/api/professor", HttpStatus.OK),
                Arguments.of("professor", "prof123", "/api/admin", HttpStatus.FORBIDDEN),

                Arguments.of("admin", "admin123", "/api/aluno", HttpStatus.OK),
                Arguments.of("admin", "admin123", "/api/professor", HttpStatus.OK),
                Arguments.of("admin", "admin123", "/api/admin", HttpStatus.OK)
        );
    }

    @ParameterizedTest(name = "{0} em {2} -> {3}")
    @MethodSource("matriz")
    void papelContraEndpoint(String login, String senha, String url, HttpStatus esperado) throws Exception {
        mvc.perform(get(url).with(httpBasic(login, senha)))
                .andExpect(status().is(esperado.value()));
    }

    /**
     * Alternativa ao httpBasic: @WithMockUser cria um usuário fictício já autenticado,
     * sem passar pelo UserDetailsService. Útil para testar SÓ a autorização.
     */
    @Nested
    @DisplayName("com @WithMockUser")
    class ComUsuarioSimulado {

        @Test
        @WithMockUser(roles = "ALUNO")
        void alunoSimuladoNaoEntraNaAreaAdmin() throws Exception {
            mvc.perform(get("/api/admin")).andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void adminSimuladoEntraNaAreaAdmin() throws Exception {
            mvc.perform(get("/api/admin")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("authorities sem o prefixo ROLE_ não valem como papel")
        @WithMockUser(authorities = "ADMIN")
        void authoritySemPrefixoNaoEhPapel() throws Exception {
            mvc.perform(get("/api/admin")).andExpect(status().isForbidden());
        }
    }
}
