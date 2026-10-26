# Guia passo a passo: testando a API com curl, Postman ou `.http`

Tudo aqui é copiar e colar. Antes de começar, suba a API:

```bash
./mvnw spring-boot:run
```

Ela responde em `http://localhost:8080`. Usuários disponíveis:

| Login       | Senha      | Papéis           |
|-------------|------------|------------------|
| `aluno`     | `aluno123` | ALUNO            |
| `professor` | `prof123`  | PROFESSOR        |
| `admin`     | `admin123` | ADMIN, PROFESSOR |

O que observar em cada resposta:

- **200** → autenticado (quando exigido) e autorizado.
- **401 Unauthorized** → falha de **autenticação**: sem credencial, senha errada ou usuário inexistente.
- **403 Forbidden** → falha de **autorização**: login certo, mas o papel não permite.

---

## Opção 1: curl

O `-i` mostra o status e os headers. O `-u login:senha` monta o header
`Authorization: Basic base64(login:senha)` por você.

### Passo 1: rota pública (sem login)

```bash
curl -i http://localhost:8080/api/publico
```
Esperado: `200`.

### Passo 2: rota protegida sem credenciais

```bash
curl -i http://localhost:8080/api/perfil
```
Esperado: `401`. Repare que não vem o header `WWW-Authenticate`: a API responde "seco", sem pedir popup de login.

### Passo 3: senha errada e usuário inexistente

```bash
curl -i -u aluno:senha-errada http://localhost:8080/api/perfil
```
```bash
curl -i -u ninguem:qualquer http://localhost:8080/api/perfil
```
Esperado: `401` nos dois. A resposta é igual de propósito, para não revelar quais logins existem.

### Passo 4: login correto, ver o perfil

```bash
curl -i -u aluno:aluno123 http://localhost:8080/api/perfil
```
Esperado: `200` com `{"login":"aluno","papeis":["ROLE_ALUNO"]}`. Note o prefixo `ROLE_`.

### Passo 5: autorização por URL, papel a papel

Aluno:
```bash
curl -i -u aluno:aluno123 http://localhost:8080/api/aluno
```
```bash
curl -i -u aluno:aluno123 http://localhost:8080/api/professor
```
```bash
curl -i -u aluno:aluno123 http://localhost:8080/api/admin
```
Esperado: `200`, `403`, `403`.

Professor:
```bash
curl -i -u professor:prof123 http://localhost:8080/api/professor
```
```bash
curl -i -u professor:prof123 http://localhost:8080/api/admin
```
Esperado: `200`, `403`.

Admin:
```bash
curl -i -u admin:admin123 http://localhost:8080/api/admin
```
Esperado: `200`.

### Passo 6: autorização por método (`@PreAuthorize`)

```bash
curl -i -u aluno:aluno123 http://localhost:8080/api/notas/aluno
```
```bash
curl -i -u aluno:aluno123 http://localhost:8080/api/notas/outro
```
```bash
curl -i -u professor:prof123 http://localhost:8080/api/notas/aluno
```
Esperado: `200` (as próprias notas), `403` (notas de outro), `200` (professor vê qualquer um).

### Passo 7: montar o header na mão

Para ver o que o `-u` faz por baixo dos panos:

```bash
echo -n 'admin:admin123' | base64
```
```bash
curl -i -H 'Authorization: Basic YWRtaW46YWRtaW4xMjM=' http://localhost:8080/api/admin
```
Base64 **não é criptografia**: qualquer um decodifica. Por isso HTTP Basic só faz sentido com HTTPS.

### Rodar tudo de uma vez

O script abaixo executa todas as chamadas acima e confere o status de cada uma:

```bash
./scripts/testar-api.sh
```

---

## Opção 2: Postman

1. Abra o Postman e clique em **Import**.
2. Arraste o arquivo [`postman/SenacSpringSecurity.postman_collection.json`](../postman/SenacSpringSecurity.postman_collection.json).
3. A coleção já vem com a variável `baseUrl = http://localhost:8080` e com **testes automáticos**
   em cada requisição (aba *Tests*), conferindo o status esperado.
4. Abra cada requisição e clique em **Send**; o resultado dos testes aparece na aba *Test Results*.
5. Para rodar tudo: clique com o botão direito na coleção → **Run collection** → **Run**.
   Todas as 17 requisições devem passar.

Como a autenticação está configurada no Postman: cada requisição usa **Auth → Basic Auth**
com login e senha preenchidos. Veja a aba *Authorization* de qualquer requisição para trocar o usuário.

---

## Opção 3: arquivo `.http` (VS Code ou IntelliJ)

Abra [`docs/requisicoes.http`](requisicoes.http). No VS Code instale a extensão **REST Client**;
no IntelliJ o suporte é nativo. Clique em **Send Request** acima de cada bloco.
