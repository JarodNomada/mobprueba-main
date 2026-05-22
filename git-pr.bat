@echo off
setlocal enabledelayedexpansion

set REPO_URL=https://github.com/JarodNomada/mobprueba-main
set PR_NUM=%~1

if "%PR_NUM%"=="" (
    echo Uso: git pr ^<numero^>
    echo Ejemplo: git pr 3
    exit /b 1
)

echo === Integrando PR #%PR_NUM% ===
echo.

git fetch "%REPO_URL%" "pull/%PR_NUM%/head:pr-%PR_NUM%"

if errorlevel 1 (
    echo ERROR: No se pudo descargar el PR #%PR_NUM%
    exit /b 1
)

echo PR #%PR_NUM% descargado
echo.

git merge "pr-%PR_NUM%" --no-edit --allow-unrelated-histories

if errorlevel 1 (
    echo.
    echo ERROR: Hay conflictos
    exit /b 1
)

echo === PR #%PR_NUM% integrado ===