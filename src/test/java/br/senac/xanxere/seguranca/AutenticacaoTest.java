package br.senac.xanxere.seguranca;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AUTENTICAÇÃO: quem é você?
 *
 * Toda falha aqui deve resultar em 401 Unauthorized, nunca em 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AutenticacaoTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("rota pública responde 200 sem nenhuma credencial")
    void rotaPublicaNaoExigeLogin() throws Exception {
        mvc.perform(get("/api/publico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    @DisplayName("tela de testes (index.html) é pública")
    void telaDeTestesEhPublica() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    @DisplayName("rota protegida sem credenciais responde 401")
    void semCredenciaisRetorna401() throws Exception {
        mvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized())
                // HttpStatusEntryPoint não manda o header que faz o navegador abrir popup de login
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    @DisplayName("senha errada responde 401")
    void senhaErradaRetorna401() throws Exception {
        mvc.perform(get("/api/perfil").with(httpBasic("aluno", "senha-errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("usuário inexistente responde 401 (mesma resposta da senha errada, para não vazar quem existe)")
    void usuarioInexistenteRetorna401() throws Exception {
        mvc.perform(get("/api/perfil").with(httpBasic("ninguem", "qualquer")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("credenciais corretas autenticam e o perfil traz login e papéis com prefixo ROLE_")
    void credenciaisCorretasAutenticam() throws Exception {
        mvc.perform(get("/api/perfil").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("admin"))
                .andExpect(jsonPath("$.papeis").isArray())
                .andExpect(jsonPath("$.papeis[?(@ == 'ROLE_ADMIN')]").exists())
                .andExpect(jsonPath("$.papeis[?(@ == 'ROLE_PROFESSOR')]").exists());
    }
}
