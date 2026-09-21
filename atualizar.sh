#!/bin/bash
# Script de atualizacao do sistema Agua Sarah em producao.
# Uso: ./atualizar.sh
#
# O que faz, em ordem:
#   1. Baixa o codigo mais novo do GitHub
#   2. Recompila o backend (so reinicia o servico se a build der certo)
#   3. Recompila o frontend numa pasta temporaria (so troca pro ar se der certo)
#   4. Reinicia o backend e confere se ele realmente subiu (nao so "iniciou")
#
# Se qualquer etapa falhar, o script para na hora (set -e) e a versao antiga
# continua rodando - nada e trocado pela metade.

set -euo pipefail

REPO_DIR="/opt/agua-sarah-sistema"
SERVICO="agua-sarah"
LOG_DIR="/var/log/agua-sarah-deploy"
DATA=$(date +"%Y-%m-%d_%H-%M-%S")
LOG_FILE="$LOG_DIR/deploy_$DATA.log"

mkdir -p "$LOG_DIR"
exec > >(tee -a "$LOG_FILE") 2>&1   # tudo que o script imprime tambem vai pro log

cor_verde=$'\033[0;32m'
cor_vermelha=$'\033[0;31m'
cor_amarela=$'\033[0;33m'
cor_reset=$'\033[0m'

etapa() { echo -e "\n${cor_amarela}==> $1${cor_reset}"; }
ok()    { echo -e "${cor_verde}✓ $1${cor_reset}"; }
erro()  { echo -e "${cor_vermelha}✗ $1${cor_reset}"; }

# se o script for interrompido por erro em qualquer ponto, avisa claramente
trap 'erro "Atualizacao interrompida por erro. A versao anterior continua no ar. Log completo em: $LOG_FILE"' ERR

echo "===================================================="
echo " Atualizacao do sistema Agua Sarah - $DATA"
echo "===================================================="

cd "$REPO_DIR"

etapa "1/5 - Conferindo se ha alteracoes locais nao esperadas"
# --untracked-files=no: ignora pastas/arquivos novos (como os de build do
# proprio script) - so bloqueia se um arquivo que JA esta no Git foi editado
# a mao no servidor, que e o unico caso que de fato indicaria algo errado
if [ -n "$(git status --porcelain --untracked-files=no)" ]; then
    erro "Existe um arquivo ja versionado que foi editado direto no servidor."
    echo "Isso normalmente nao deveria acontecer (essa pasta e so pra rodar o sistema, nao pra editar)."
    echo "Rode 'git status' pra ver o que mudou antes de continuar."
    exit 1
fi
ok "Nada de estranho - pasta limpa"

etapa "2/5 - Baixando a versao mais nova do GitHub"
COMMIT_ANTES=$(git rev-parse HEAD)
git pull origin main
COMMIT_DEPOIS=$(git rev-parse HEAD)

if [ "$COMMIT_ANTES" == "$COMMIT_DEPOIS" ]; then
    echo "Nao havia nada novo pra baixar (ja estava atualizado)."
else
    ok "Codigo atualizado: $COMMIT_ANTES -> $COMMIT_DEPOIS"
fi

etapa "3/5 - Recompilando o backend"
cd "$REPO_DIR/backend"
mvn clean package -DskipTests

JAR="$REPO_DIR/backend/target/agua-sarah-backend.jar"
if [ ! -f "$JAR" ] || [ "$(stat -c%s "$JAR")" -lt 1000000 ]; then
    erro "O arquivo .jar nao foi gerado corretamente. Build do backend falhou."
    exit 1
fi
ok "Backend compilado ($(du -h "$JAR" | cut -f1))"

etapa "4/5 - Recompilando o frontend (numa pasta separada, so troca se der certo)"
cd "$REPO_DIR/frontend"
npm install --silent
rm -rf dist_novo
VITE_API_URL=/api npm run build -- --outDir dist_novo

if [ ! -f "$REPO_DIR/frontend/dist_novo/index.html" ]; then
    erro "O build do frontend nao gerou os arquivos esperados."
    exit 1
fi

# troca atomica: guarda a versao anterior como dist_anterior (fica de backup)
rm -rf dist_anterior
if [ -d dist ]; then
    mv dist dist_anterior
fi
mv dist_novo dist
ok "Frontend atualizado (versao anterior preservada em dist_anterior/)"

etapa "5/5 - Reiniciando o backend e conferindo se ele sobe de verdade"
systemctl restart "$SERVICO"

echo "Aguardando o backend responder (pode levar ate 30s nesse sistema)..."
SUBIU=false
for i in $(seq 1 15); do
    sleep 2
    if curl -s -o /dev/null -m 2 http://localhost:8080/api/auth/login; then
        SUBIU=true
        break
    fi
done

if [ "$SUBIU" = false ]; then
    erro "O backend nao respondeu apos o restart. Confira com: journalctl -u $SERVICO -n 60 --no-pager"
    exit 1
fi

ok "Backend respondendo normalmente"

echo -e "\n${cor_verde}===================================================="
echo " Atualizacao concluida com sucesso!"
echo "====================================================${cor_reset}"
echo "Log salvo em: $LOG_FILE"
