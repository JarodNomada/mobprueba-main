#!/bin/bash
# Script para integrar PRs de tu repositorio
# Uso: git pr <numero>
# Ejemplo: git pr 5

REPO_URL="https://github.com/JarodNomada/mobprueba-main"
PR_NUM=$1

if [ -z "$PR_NUM" ]; then
    echo "Uso: git pr <numero_del_pr>"
    echo "Ejemplo: git pr 5"
    exit 1
fi

echo "=== Integrando PR #$PR_NUM de $REPO_URL ==="
echo ""

# Fetch del PR específico
echo "Descargando PR #$PR_NUM..."
git fetch "$REPO_URL" "pull/$PR_NUM/head:pr-$PR_NUM"

if [ $? -ne 0 ]; then
    echo "ERROR: No se pudo descargar el PR #$PR_NUM"
    echo "Verifica que el PR exista y no esté cerrado/cerrado"
    exit 1
fi

echo "PR #$PR_NUM descargado correctamente"
echo ""

# Hacer merge (con allow-unrelated-histories por si los históricos son diferentes)
echo "Integrando cambios..."
git merge "pr-$PR_NUM" --no-edit --allow-unrelated-histories

if [ $? -eq 0 ]; then
    echo ""
    echo "=== PR #$PR_NUM integrado exitosamente ==="
    echo "Limpiando branch local..."
    git branch -d "pr-$PR_NUM"
else
    echo ""
    echo "ERROR: Hay conflictos. Resuélvelos manualmente con:"
    echo "  git status"
    echo "  git add ."
    echo "  git commit -m 'Resolver conflictos'"
    exit 1
fi