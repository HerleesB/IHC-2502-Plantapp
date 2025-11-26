#!/bin/bash
# Script de instalación y corrección automática para Jardín Inteligente Backend
# Autor: Sistema de verificación
# Fecha: 2024

echo "🌱 =========================================="
echo "🌱 Jardín Inteligente - Instalación Backend"
echo "🌱 =========================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Directorio base
BACKEND_DIR="/Users/cesar/Downloads/JardinInteligenApp 2/backend"

cd "$BACKEND_DIR" || exit 1

echo "📁 Directorio actual: $(pwd)"
echo ""

# Paso 1: Verificar Python
echo "🐍 Verificando Python..."
if command -v python3 &> /dev/null; then
    PYTHON_VERSION=$(python3 --version)
    echo -e "${GREEN}✅ Python encontrado: $PYTHON_VERSION${NC}"
else
    echo -e "${RED}❌ Python 3 no encontrado. Por favor instala Python 3.12+${NC}"
    exit 1
fi
echo ""

# Paso 2: Crear entorno virtual si no existe
echo "📦 Configurando entorno virtual..."
if [ ! -d ".venv" ]; then
    echo "Creando entorno virtual..."
    python3 -m venv .venv
    echo -e "${GREEN}✅ Entorno virtual creado${NC}"
else
    echo -e "${YELLOW}⚠️  Entorno virtual ya existe${NC}"
fi
echo ""

# Paso 3: Activar entorno virtual
echo "🔄 Activando entorno virtual..."
source .venv/bin/activate

# Paso 4: Actualizar pip
echo "⬆️  Actualizando pip..."
pip install --upgrade pip --quiet
echo -e "${GREEN}✅ Pip actualizado${NC}"
echo ""

# Paso 5: Instalar dependencias
echo "📚 Instalando dependencias..."
pip install -r app/requirements.txt --quiet
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Dependencias instaladas${NC}"
else
    echo -e "${RED}❌ Error instalando dependencias${NC}"
    exit 1
fi
echo ""

# Paso 6: Aplicar correcciones
echo "🔧 Aplicando correcciones al código..."

# Corregir config.py
echo "  - Corrigiendo app/config.py..."
if [ -f "app/config_fixed.py" ]; then
    cp app/config_fixed.py app/config.py
    echo -e "${GREEN}    ✅ config.py corregido${NC}"
fi

# Corregir groq_service.py
echo "  - Corrigiendo app/services/groq_service.py..."
if [ -f "app/services/groq_service_fixed.py" ]; then
    cp app/services/groq_service_fixed.py app/services/groq_service.py
    echo -e "${GREEN}    ✅ groq_service.py corregido${NC}"
fi

# Corregir diagnosis.py
echo "  - Corrigiendo app/routes/diagnosis.py..."
if [ -f "app/routes/diagnosis_fixed.py" ]; then
    cp app/routes/diagnosis_fixed.py app/routes/diagnosis.py
    echo -e "${GREEN}    ✅ diagnosis.py corregido${NC}"
fi

echo ""

# Paso 7: Verificar archivo .env
echo "🔐 Verificando configuración..."
if [ ! -f ".env" ]; then
    echo -e "${RED}❌ Archivo .env no encontrado${NC}"
    echo "Creando .env de ejemplo..."
    cat > .env << 'EOF'
# Groq API Key (REQUERIDO - Obtener en console.groq.com)
GROQ_API_KEY=tu_clave_aqui

# Modelos de IA
GROQ_MODEL=llama-3.2-90b-vision-preview
GROQ_TEXT_MODEL=llama-3.1-70b-versatile
GROQ_TIMEOUT=30

# Configuración de la aplicación
APP_NAME=Jardin Inteligente
APP_VERSION=1.0.0
DEBUG=True
AUDIO_OUTPUT_DIR=./cache/audio

# CORS
ALLOWED_ORIGINS=["*"]
EOF
    echo -e "${YELLOW}⚠️  Archivo .env creado. POR FAVOR EDITA Y AGREGA TU GROQ_API_KEY${NC}"
else
    # Verificar que tenga GROQ_API_KEY
    if grep -q "GROQ_API_KEY=tu_clave_aqui" .env || ! grep -q "GROQ_API_KEY=" .env; then
        echo -e "${YELLOW}⚠️  GROQ_API_KEY no configurado en .env${NC}"
        echo -e "${YELLOW}   Obtén tu clave en: https://console.groq.com${NC}"
    else
        echo -e "${GREEN}✅ Archivo .env configurado${NC}"
    fi
fi
echo ""

# Paso 8: Crear directorios necesarios
echo "📁 Creando directorios..."
mkdir -p cache/audio
mkdir -p uploads
echo -e "${GREEN}✅ Directorios creados${NC}"
echo ""

# Paso 9: Inicializar base de datos
echo "🗄️  Inicializando base de datos..."
python3 << 'PYTHON_SCRIPT'
try:
    from app.models.database import init_db
    init_db()
    print("✅ Base de datos inicializada correctamente")
except Exception as e:
    print(f"❌ Error inicializando base de datos: {e}")
    exit(1)
PYTHON_SCRIPT
echo ""

# Paso 10: Verificar instalación
echo "🧪 Verificando instalación..."
python3 << 'PYTHON_SCRIPT'
import sys
errors = []

try:
    import fastapi
    print("✅ FastAPI instalado")
except ImportError:
    errors.append("FastAPI")

try:
    import groq
    print("✅ Groq instalado")
except ImportError:
    errors.append("Groq")

try:
    import sqlalchemy
    print("✅ SQLAlchemy instalado")
except ImportError:
    errors.append("SQLAlchemy")

try:
    from app.config import get_settings
    settings = get_settings()
    print("✅ Configuración cargada")
except Exception as e:
    print(f"⚠️  Error en configuración: {e}")

if errors:
    print(f"\n❌ Módulos faltantes: {', '.join(errors)}")
    sys.exit(1)
PYTHON_SCRIPT

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 =========================================="
    echo -e "🎉 Instalación completada exitosamente"
    echo -e "🎉 ==========================================${NC}"
    echo ""
    echo "📝 Próximos pasos:"
    echo "   1. Edita .env y agrega tu GROQ_API_KEY"
    echo "   2. Ejecuta: python -m app.main"
    echo "   3. Abre http://localhost:8000/docs"
    echo ""
    echo -e "${YELLOW}💡 Tip: Para ejecutar el servidor:${NC}"
    echo "   cd $BACKEND_DIR"
    echo "   source .venv/bin/activate"
    echo "   python -m app.main"
    echo ""
else
    echo ""
    echo -e "${RED}❌ Instalación completada con errores${NC}"
    echo "   Revisa los mensajes anteriores"
fi
