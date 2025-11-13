@echo off
REM Script de instalación para Windows
REM Jardín Inteligente Backend

echo =========================================
echo Jardín Inteligente - Instalación Backend
echo =========================================
echo.

cd /d "%~dp0"
echo Directorio actual: %CD%
echo.

REM Verificar Python
echo Verificando Python...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Python no encontrado
    echo Por favor instala Python 3.12+
    pause
    exit /b 1
)
echo OK: Python encontrado
echo.

REM Crear entorno virtual
echo Creando entorno virtual...
if not exist .venv (
    python -m venv .venv
    echo OK: Entorno virtual creado
) else (
    echo INFO: Entorno virtual ya existe
)
echo.

REM Activar entorno virtual
echo Activando entorno virtual...
call .venv\Scripts\activate.bat

REM Actualizar pip
echo Actualizando pip...
python -m pip install --upgrade pip --quiet
echo OK: Pip actualizado
echo.

REM Instalar dependencias
echo Instalando dependencias...
pip install -r app\requirements.txt --quiet
if %errorlevel% equ 0 (
    echo OK: Dependencias instaladas
) else (
    echo ERROR: Fallo al instalar dependencias
    pause
    exit /b 1
)
echo.

REM Aplicar correcciones
echo Aplicando correcciones...
if exist app\config_fixed.py (
    copy /y app\config_fixed.py app\config.py >nul
    echo OK: config.py corregido
)
if exist app\services\groq_service_fixed.py (
    copy /y app\services\groq_service_fixed.py app\services\groq_service.py >nul
    echo OK: groq_service.py corregido
)
if exist app\routes\diagnosis_fixed.py (
    copy /y app\routes\diagnosis_fixed.py app\routes\diagnosis.py >nul
    echo OK: diagnosis.py corregido
)
echo.

REM Verificar .env
echo Verificando configuración...
if not exist .env (
    echo ADVERTENCIA: Archivo .env no encontrado
    echo Creando .env de ejemplo...
    (
        echo # Groq API Key
        echo GROQ_API_KEY=tu_clave_aqui
        echo GROQ_MODEL=llama-3.2-90b-vision-preview
        echo GROQ_TEXT_MODEL=llama-3.1-70b-versatile
        echo GROQ_TIMEOUT=30
        echo APP_NAME=Jardin Inteligente
        echo APP_VERSION=1.0.0
        echo DEBUG=True
        echo AUDIO_OUTPUT_DIR=./cache/audio
        echo ALLOWED_ORIGINS=["*"]
    ) > .env
    echo IMPORTANTE: Edita .env y agrega tu GROQ_API_KEY
) else (
    echo OK: Archivo .env encontrado
)
echo.

REM Crear directorios
echo Creando directorios...
if not exist cache\audio mkdir cache\audio
if not exist uploads mkdir uploads
echo OK: Directorios creados
echo.

REM Inicializar base de datos
echo Inicializando base de datos...
python -c "from app.models.database import init_db; init_db(); print('OK: Base de datos creada')"
echo.

REM Verificar instalación
echo Verificando instalación...
python -c "import fastapi, groq, sqlalchemy; print('OK: Todas las dependencias verificadas')"
echo.

echo =========================================
echo Instalación completada
echo =========================================
echo.
echo Proximos pasos:
echo 1. Edita .env y agrega tu GROQ_API_KEY
echo 2. Ejecuta: python -m app.main
echo 3. Abre http://localhost:8000/docs
echo.
pause
