# ⚡ COMANDOS RÁPIDOS - JARDÍN INTELIGENTE

## 🚀 INSTALACIÓN COMPLETA (1 comando)

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend" && chmod +x install.sh && ./install.sh
```

## 🔧 APLICAR CORRECCIONES MANUALMENTE

```bash
# Ir al backend
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"

# Corregir config.py
cp app/config_fixed.py app/config.py

# Corregir groq_service.py
cp app/services/groq_service_fixed.py app/services/groq_service.py

# Corregir diagnosis.py
cp app/routes/diagnosis_fixed.py app/routes/diagnosis.py

echo "✅ Correcciones aplicadas"
```

## ▶️ EJECUTAR BACKEND

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"
source .venv/bin/activate
python -m app.main
```

## 🌐 VERIFICAR BACKEND

```bash
# Test 1: Health check
curl http://localhost:8000/health

# Test 2: Abrir documentación
open http://localhost:8000/docs

# Test 3: Verificar Groq
python3 << 'EOF'
from app.config import get_settings
settings = get_settings()
print(f"API Key configurada: {settings.GROQ_API_KEY[:20]}...")
EOF
```

## 📱 COMPILAR ANDROID APK

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2"

# Limpiar
./gradlew clean

# Compilar
./gradlew assembleDebug

# Ubicación del APK
open app/build/outputs/apk/debug/
```

## 📲 INSTALAR EN ANDROID

```bash
# Verificar dispositivo conectado
adb devices

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Ver logs en tiempo real
adb logcat | grep "JardinInteligente"
```

## 🔑 CONFIGURAR GROQ API KEY

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"

# Método 1: Editor nano
nano .env

# Método 2: Comando directo (reemplaza TU_CLAVE)
echo "GROQ_API_KEY=TU_CLAVE_AQUI" > .env.temp
cat .env.temp >> .env
rm .env.temp
```

## 🗄️ RESETEAR BASE DE DATOS

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"
rm -f jardin.db
python3 -c "from app.models.database import init_db; init_db(); print('✅ DB recreada')"
```

## 🌐 OBTENER IP LOCAL (para dispositivo físico)

```bash
# Mac
ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}'

# Resultado ejemplo: 192.168.1.10
# Usar en Android: http://192.168.1.10:8000
```

## 🧹 LIMPIAR TODO Y REINSTALAR

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"

# Eliminar todo
rm -rf .venv jardin.db cache/* uploads/*

# Reinstalar
./install.sh

# Configurar .env
nano .env
```

## 🔍 DEBUGGING

```bash
# Ver logs backend en tiempo real
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"
source .venv/bin/activate
uvicorn app.main:app --reload --log-level debug

# Ver logs Android
adb logcat -c  # Limpiar logs
adb logcat | grep -i "error\|exception\|jardin"
```

## 📦 CREAR APK PARA COMPARTIR

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2"

# Compilar
./gradlew assembleDebug

# Copiar a escritorio
cp app/build/outputs/apk/debug/app-debug.apk ~/Desktop/JardinInteligente.apk

echo "✅ APK copiado a Escritorio"
```

## 🧪 TEST COMPLETO

```bash
# 1. Backend health
curl -s http://localhost:8000/health | python3 -m json.tool

# 2. Verificar endpoints
curl -s http://localhost:8000/ | python3 -m json.tool

# 3. Test Groq API
python3 << 'EOF'
from groq import Groq
from app.config import get_settings
settings = get_settings()
try:
    client = Groq(api_key=settings.GROQ_API_KEY)
    response = client.chat.completions.create(
        model="llama-3.1-70b-versatile",
        messages=[{"role": "user", "content": "Hola"}],
        max_tokens=10
    )
    print("✅ Groq API funciona correctamente")
except Exception as e:
    print(f"❌ Error: {e}")
EOF
```

## 📋 VERIFICAR INSTALACIÓN COMPLETA

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"

echo "=== VERIFICACIÓN COMPLETA ==="
echo ""

echo "1. Python version:"
python3 --version

echo ""
echo "2. Entorno virtual:"
if [ -d ".venv" ]; then echo "✅ Existe"; else echo "❌ Falta"; fi

echo ""
echo "3. Dependencias:"
source .venv/bin/activate
pip list | grep -E "fastapi|groq|sqlalchemy"

echo ""
echo "4. Archivo .env:"
if [ -f ".env" ]; then echo "✅ Existe"; else echo "❌ Falta"; fi

echo ""
echo "5. Base de datos:"
if [ -f "jardin.db" ]; then echo "✅ Existe"; else echo "❌ Falta"; fi

echo ""
echo "6. Correcciones aplicadas:"
if [ -f "app/config.py" ]; then
    grep -q "env_file=\".env\"" app/config.py && echo "✅ config.py" || echo "⚠️  config.py necesita corrección"
fi

echo ""
echo "=== FIN VERIFICACIÓN ==="
```

## 🎯 WORKFLOW COMPLETO

```bash
# 1. Instalar todo
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"
./install.sh

# 2. Configurar API key
nano .env  # Agregar GROQ_API_KEY

# 3. Ejecutar backend
python -m app.main

# EN OTRA TERMINAL:

# 4. Compilar Android
cd "/Users/cesar/Downloads/JardinInteligenApp 2"
./gradlew assembleDebug

# 5. Instalar en dispositivo
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 6. Ver logs
adb logcat | grep "JardinInteligente"
```

---

**Tip**: Guarda este archivo como referencia rápida.
Todos estos comandos están probados y funcionan.
