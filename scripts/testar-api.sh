#!/usr/bin/env bash
# Executa todas as chamadas do guia e confere o status HTTP de cada uma.
# Uso: ./scripts/testar-api.sh [baseUrl]   (padrão: http://localhost:8080)
set -u

BASE="${1:-http://localhost:8080}"
VERDE='\033[0;32m'; VERMELHO='\033[0;31m'; SEM_COR='\033[0m'
falhas=0

# chamada <esperado> <descrição> <url> [login:senha]
chamada() {
    local esperado="$1" descricao="$2" url="$3" cred="${4:-}"
    local status
    if [ -n "$cred" ]; then
        status=$(curl -s -o /dev/null -w '%{http_code}' -u "$cred" "$BASE$url")
    else
        status=$(curl -s -o /dev/null -w '%{http_code}' "$BASE$url")
    fi
    if [ "$status" = "$esperado" ]; then
        printf "${VERDE}OK ${SEM_COR}  %s  %-22s %-16s -> %s\n" "$esperado" "$url" "${cred%%:*}" "$descricao"
    else
        printf "${VERMELHO}FALHOU${SEM_COR} esperado %s, veio %s  %s (%s)\n" "$esperado" "$status" "$url" "${cred:-sem login}"
        falhas=$((falhas + 1))
    fi
}

if ! curl -s -o /dev/null "$BASE/api/publico"; then
    echo "API não respondeu em $BASE. Suba com ./mvnw spring-boot:run"
    exit 1
fi

echo "== Autenticação =="
chamada 200 "rota pública sem login"        /api/publico
chamada 401 "sem credenciais"               /api/perfil
chamada 401 "senha errada"                  /api/perfil   aluno:senha-errada
chamada 401 "usuário inexistente"           /api/perfil   ninguem:qualquer
chamada 200 "credenciais corretas"          /api/perfil   aluno:aluno123

echo "== Autorização por URL =="
chamada 200 "aluno na área do aluno"        /api/aluno     aluno:aluno123
chamada 403 "aluno na área do professor"    /api/professor aluno:aluno123
chamada 403 "aluno na área admin"           /api/admin     aluno:aluno123
chamada 200 "professor na área do aluno"    /api/aluno     professor:prof123
chamada 200 "professor na área do professor" /api/professor professor:prof123
chamada 403 "professor na área admin"       /api/admin     professor:prof123
chamada 200 "admin na área do aluno"        /api/aluno     admin:admin123
chamada 200 "admin na área do professor"    /api/professor admin:admin123
chamada 200 "admin na área admin"           /api/admin     admin:admin123

echo "== Autorização por método (@PreAuthorize) =="
chamada 200 "aluno vê as próprias notas"    /api/notas/aluno aluno:aluno123
chamada 403 "aluno vê notas de outro"       /api/notas/outro aluno:aluno123
chamada 200 "professor vê notas de qualquer um" /api/notas/aluno professor:prof123
chamada 401 "notas sem login"               /api/notas/aluno

echo
if [ "$falhas" -eq 0 ]; then
    printf "${VERDE}Todas as chamadas responderam como esperado.${SEM_COR}\n"
else
    printf "${VERMELHO}%d chamada(s) com resultado inesperado.${SEM_COR}\n" "$falhas"
    exit 1
fi
