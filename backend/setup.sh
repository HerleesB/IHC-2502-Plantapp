#!/bin/bash

echo "🌱 Jardín Inteligente - Backend Setup"
echo "===================================="
echo ""

# Verificar Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 no está instalado"
    exit 1
fi

echo "✓ Python 3 encontrado"
echo ""

# Crear entorno virtual si no existe
if [ ! -d "venv" ]; then
    echo "📦 Creando entorno virtual..."
    python3 -m venv venv
    echo "✓ Entorno virtual creado"
else
    echo "✓ Entorno virtual ya existe"
fi

echo ""
echo "🔄 Activando entorno virtual..."
source venv/bin/activate

echo "📥 Instalando dependencias..."
pip install -q --upgrade pip
pip install -q -r requirements.txt

echo "✓ Dependencias instaladas"
echo ""

# Verificar .env
if [ ! -f ".env" ]; then
    echo "⚙️  Creando archivo .env desde .env.example..."
    cp .env .env
    echo "⚠️  IMPORTANTE: Edita .env y agrega tu GROQ_API_KEY"
    echo ""
fi

echo "✅ Setup completo!"
echo ""
echo "📝 Próximos pasos:"
echo "   1. Edita .env y agrega tu GROQ_API_KEY"
echo "   2. Ejecuta: python -m app.main"
echo "   3. Visita: http://localhost:8000/docs"
echo ""
echo "Para activar el entorno virtual manualmente:"
echo "   source venv/bin/activate"
