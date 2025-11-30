@echo off
echo ============================================================
echo EJECUTANDO LIMPIEZA FINAL DEL BACKEND
echo ============================================================
echo.

cd /d "%~dp0"

python complete_refactor.py

echo.
echo ============================================================
echo Presiona cualquier tecla para salir...
pause > nul
