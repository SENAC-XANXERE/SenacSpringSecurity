# SenacSpringSecurity

App didático em **Java 21 + Spring Boot 3.5** que mostra, do jeito mais enxuto possível, o fluxo de
**autenticação** e **autorização** com Spring Security.

- **Autenticação** = provar *quem você é* (login + senha).
- **Autorização** = decidir *o que você pode fazer* (papéis e regras).

Não há banco de dados nem tela de login: os usuários ficam em memória e a API responde JSON.
O objetivo é enxergar cada peça do Spring Security sem ruído.

## Como rodar

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. O log está em `DEBUG` para o pacote
`org.springframework.security`, então dá para acompanhar cada filtro decidindo o que fazer.

## Usuários de exemplo

| Login       | Senha      | Papéis            |
|-------------|------------|-------------------|
| `aluno`     | `aluno123` | ALUNO             |
| `professor` | `prof123`  | PROFESSOR         |
| `admin`     | `admin123` | ADMIN, PROFESSOR  |

As senhas são guardadas como hash **BCrypt** (veja `UsuarioRepository`). Nunca guarde senha em texto puro.

## Endpoints e quem pode acessar

| Endpoint               | Regra                                         | aluno | professor | admin |
|------------------------|-----------------------------------------------|:-----:|:---------:|:-----:|
| `GET /api/publico`     | `permitAll()`                                 |  ✅   |    ✅     |  ✅   |
| `GET /api/perfil`      | `authenticated()`                             |  ✅   |    ✅     |  ✅   |
| `GET /api/aluno`       | `hasAnyRole(ALUNO, PROFESSOR, ADMIN)`         |  ✅   |    ✅     |  ✅   |
| `GET /api/professor`   | `hasAnyRole(PROFESSOR, ADMIN)`                |  ❌   |    ✅     |  ✅   |
| `GET /api/admin`       | `hasRole(ADMIN)`                              |  ❌   |    ❌     |  ✅   |
| `GET /api/notas/{aluno}` | `@PreAuthorize`: professor/admin, ou o próprio aluno | só as próprias | ✅ | ✅ |

Sem login (ou com senha errada) qualquer rota protegida devolve **401 Unauthorized**.
Logado, mas sem o papel certo, devolve **403 Forbidden**. Essa diferença é a chave de tudo:
401 é falha de *autenticação*, 403 é falha de *autorização*.

## Testando com curl

```bash
# público, sem login
curl -i http://localhost:8080/api/publico

# sem credenciais -> 401
curl -i http://localhost:8080/api/perfil

# aluno vê o próprio perfil -> 200
curl -i -u aluno:aluno123 http://localhost:8080/api/perfil

# aluno tenta a área admin -> 403
curl -i -u aluno:aluno123 http://localhost:8080/api/admin

# admin entra -> 200
curl -i -u admin:admin123 http://localhost:8080/api/admin

# aluno vê as próprias notas -> 200; as de outro -> 403
curl -i -u aluno:aluno123 http://localhost:8080/api/notas/aluno
curl -i -u aluno:aluno123 http://localhost:8080/api/notas/outro
```

## O fluxo, passo a passo

```
requisição HTTP
   │
   ▼
SecurityFilterChain  (SecurityConfig)
   │
   ├─ BasicAuthenticationFilter ── lê o header "Authorization: Basic ..."
   │       │
   │       ▼
   │   AuthenticationManager ── DaoAuthenticationProvider
   │       │                        ├─ UsuarioDetailsService.loadUserByUsername(login)
   │       │                        └─ PasswordEncoder.matches(senhaDigitada, hashGuardado)
   │       ▼
   │   Authentication autenticado vai para o SecurityContextHolder
   │
   ├─ AuthorizationFilter ── confere as regras de authorizeHttpRequests (por URL)
   │
   ▼
Controller
   └─ @PreAuthorize (por método), quando a regra depende dos dados da requisição
```

### Autenticação (`usuario/`)

1. `UsuarioRepository` guarda os usuários (em memória, no lugar de um `JpaRepository`).
2. `UsuarioDetailsService` implementa `UserDetailsService`: é o ponto onde o Spring pergunta
   "quem é o usuário `X`?" e recebe um `UserDetails` com login, hash da senha e authorities.
3. `PasswordEncoder` (bean em `SecurityConfig`) compara a senha enviada com o hash. Com BCrypt,
   o mesmo texto gera hashes diferentes, por isso a comparação é feita por `matches`, não por `equals`.

### Autorização (`config/SecurityConfig` e `api/`)

- **Por URL**: `authorizeHttpRequests` com `requestMatchers(...)`. As regras valem na ordem em que
  são escritas; a primeira que casa com a URL decide. Por isso as mais específicas vêm antes de `anyRequest()`.
- **Por método**: `@EnableMethodSecurity` + `@PreAuthorize("...")`. Usa SpEL, então consegue olhar
  parâmetros do método (`#aluno`) e o usuário logado (`authentication.name`). Veja `NotasController`.

### Role vs. authority

`hasRole("ADMIN")` é só açúcar para `hasAuthority("ROLE_ADMIN")`. `User.builder().roles("ADMIN")`
já acrescenta o prefixo `ROLE_`. Misturar os dois sem o prefixo é um erro clássico.

### Por que CSRF está desligado?

CSRF explora o *cookie de sessão* que o navegador envia sozinho. Esta API é `STATELESS`: não cria
sessão e cada requisição traz as credenciais no header. Sem cookie de sessão, não há o que o CSRF
explorar. Em uma aplicação com formulário de login e sessão, **deixe o CSRF ligado**.

## Testes

Os testes ficam em `src/test/java` e cobrem cada linha da tabela acima:

```bash
./mvnw test
```

- `AutenticacaoTest`: sem credenciais, senha errada, usuário inexistente, credenciais corretas.
- `AutorizacaoPorUrlTest`: cada papel contra cada endpoint (200 ou 403).
- `AutorizacaoPorMetodoTest`: a regra `@PreAuthorize` de `/api/notas/{aluno}`.
- `UsuarioDetailsServiceTest`: teste unitário puro, sem subir o Spring, do carregamento do usuário.

## Próximos passos (ideias para exercícios)

- Trocar `UsuarioRepository` por uma entidade JPA + H2.
- Trocar HTTP Basic por login com formulário (`formLogin()`) e observar o CSRF e a sessão.
- Emitir um JWT no login e validá-lo num filtro próprio.
- Criar um papel `COORDENADOR` e um endpoint que só ele acessa; escrever os testes antes do código.
