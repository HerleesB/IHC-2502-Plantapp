# 🌱 Jardín Inteligente - Instrucciones de Ejecución

## 🚀 Inicio Rápido (3 pasos)

### 1️⃣ Instalar Backend (Automático)

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"
chmod +x install.sh
./install.sh
```

**El script hace automáticamente:**
- ✅ Verifica Python
- ✅ Crea entorno virtual
- ✅ Instala todas las dependencias
- ✅ Aplica correcciones al código
- ✅ Crea base de datos
- ✅ Configura directorios

### 2️⃣ Configurar API Key

```bash
# Editar archivo .env
nano .env

# Cambiar esta línea:
GROQ_API_KEY=tu_clave_aqui

# Por tu clave real de Groq:
GROQ_API_KEY=gsk_tu_clave_real_aqui
```

**Obtener clave Groq:** https://console.groq.com

### 3️⃣ Ejecutar Servidor

```bash
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"
source .venv/bin/activate
python -m app.main
```

**✅ Backend corriendo en:** http://localhost:8000

---

## 📱 Ejecutar App Android

### En Emulador

1. Abrir Android Studio
2. Abrir proyecto: `/Users/cesar/Downloads/JardinInteligenApp 2`
3. Crear emulador (si no existe):
   - Tools → Device Manager → Create Device
   - Pixel 6 Pro + Android 13 (API 33)
4. Clic en **Run** ▶️

### En Dispositivo Físico

1. **Habilitar modo desarrollador:**
   - Ajustes → Acerca del teléfono
   - Tocar "Número de compilación" 7 veces
   - Regresar → Sistema → Opciones de desarrollador
   - Activar "Depuración USB"

2. **Conectar con cable USB**

3. **En Android Studio:**
   - Seleccionar tu dispositivo en la lista
   - Clic en **Run** ▶️

4. **Configurar IP del backend:**
   
   Opción A - Desde la app (si está implementado):
   - Ir a Configuración
   - Cambiar URL a: `http://TU_IP_LOCAL:8000`
   
   Opción B - Hardcodear en código:
   ```kotlin
   // En algún archivo de configuración
   const val BASE_URL = "http://192.168.1.X:8000" // Tu IP local
   ```
   
   Para obtener tu IP:
   ```bash
   # Mac
   ifconfig | grep "inet " | grep -v 127.0.0.1
   
   # Resultado ejemplo: 192.168.1.10
   ```

---

## 📦 Instalar APK en Android

### Método 1: Compilar APK

```bash
# En Android Studio
# 1. Build → Build Bundle(s) / APK(s) → Build APK(s)
# 2. Esperar compilación
# 3. APK generado en: app/build/outputs/apk/debug/app-debug.apk
```

### Método 2: Instalar por USB

```bash
# Compilar
cd "/Users/cesar/Downloads/JardinInteligenApp 2"
./gradlew assembleDebug

# Instalar
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Método 3: Compartir APK

```bash
# 1. Ubicar archivo:
open app/build/outputs/apk/debug/

# 2. Enviar app-debug.apk por:
#    - WhatsApp
#    - Email
#    - AirDrop
#    - Google Drive

# 3. En el teléfono:
#    - Abrir APK
#    - Permitir "Instalar apps desconocidas"
#    - Instalar
```

---

## 🧪 Verificar que Todo Funciona

### Backend

```bash
# Test 1: Health check
curl http://localhost:8000/health

# Respuesta esperada:
# {"status":"healthy","app":"Jardín Inteligente API","version":"1.0.0"}

# Test 2: Documentación
open http://localhost:8000/docs
```

### App Android

1. ✅ App abre sin crashes
2. ✅ Navegación entre tabs funciona
3. ✅ Cámara solicita permisos
4. ✅ Puede tomar fotos

---

## 🔧 Comandos Útiles

### Backend

```bash
# Activar entorno
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"
source .venv/bin/activate

# Ejecutar servidor
python -m app.main

# O con recarga automática:
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Ver logs en tiempo real
tail -f *.log

# Reiniciar base de datos
rm jardin.db
python -c "from app.models.database import init_db; init_db()"

# Desactivar entorno
deactivate
```

### Android

```bash
# Compilar
./gradlew assembleDebug

# Limpiar caché
./gradlew clean

# Ver logs del dispositivo
adb logcat | grep "JardinInteligente"

# Reinstalar app
adb uninstall com.jardin.inteligente
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🐛 Solución de Problemas Comunes

### Backend no inicia

```bash
# Verificar que estás en el entorno virtual
which python
# Debe mostrar: .../backend/.venv/bin/python

# Reinstalar dependencias
pip install -r app/requirements.txt --force-reinstall
```

### Error "ModuleNotFoundError"

```bash
# Aplicar correcciones
cd "/Users/cesar/Downloads/JardinInteligenApp 2/backend"
./install.sh
```

### Android no conecta con backend

**Si usas emulador:**
- URL debe ser: `http://10.0.2.2:8000`

**Si usas dispositivo físico:**
1. Obtener IP local: `ifconfig | grep "inet " | grep -v 127.0.0.1`
2. Usar: `http://TU_IP:8000`
3. Verificar que ambos están en la misma WiFi

### Groq API falla

```bash
# Verificar key
cat .env | grep GROQ_API_KEY

# Test manual
python3 << 'EOF'
from groq import Groq
from app.config import get_settings
settings = get_settings()
client = Groq(api_key=settings.GROQ_API_KEY)
response = client.chat.completions.create(
    model="llama-3.1-70b-versatile",
    messages=[{"role": "user", "content": "Test"}],
    max_tokens=10
)
print("✅ Groq API funciona!")
EOF
```

---

## 📊 Verificación Final

### Checklist Backend ✅

- [ ] `python --version` muestra 3.12+
- [ ] Entorno virtual activado (`.venv`)
- [ ] Archivo `.env` configurado con clave Groq
- [ ] `python -m app.main` inicia sin errores
- [ ] `http://localhost:8000/docs` abre documentación
- [ ] Base de datos `jardin.db` existe
- [ ] Directorios `cache/` y `uploads/` creados

### Checklist Android ✅

- [ ] Android Studio sincroniza Gradle sin errores
- [ ] App compila sin errores
- [ ] Emulador o dispositivo conectado
- [ ] App instala correctamente
- [ ] Navegación funciona
- [ ] Permisos de cámara funcionan
- [ ] Conexión con backend establecida

---

## 🎯 Arquitectura del Sistema

```
┌─────────────────┐
│  App Android    │
│  (Kotlin +      │
│   Compose)      │
└────────┬────────┘
         │ HTTP/REST
         │
┌────────▼────────┐
│  Backend API    │
│  (FastAPI +     │
│   Python)       │
└────────┬────────┘
         │
         ├──► Groq AI (LLM + Vision)
         ├──► SQLite (Base de datos)
         └──► Sistema de archivos (Imágenes)
```

---

## 📝 Endpoints Principales

```bash
# Health check
GET http://localhost:8000/health

# Documentación interactiva
GET http://localhost:8000/docs

# Analizar planta
POST http://localhost:8000/api/diagnosis/analyze
Content-Type: multipart/form-data
{
  "plant_id": 1,
  "image": <archivo>,
  "symptoms": "hojas amarillas"
}

# Listar plantas
GET http://localhost:8000/api/plants/user/1

# Crear planta
POST http://localhost:8000/api/plants
Content-Type: application/json
{
  "name": "Monstera",
  "user_id": 1
}

# Comunidad
GET http://localhost:8000/api/community/posts

# Gamificación
GET http://localhost:8000/api/gamification/achievements/1
```

---

## 🎓 Recursos de Aprendizaje

- **FastAPI**: https://fastapi.tiangolo.com
- **Groq AI**: https://console.groq.com/docs
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **SQLAlchemy**: https://www.sqlalchemy.org

---

## 📞 Soporte

Si algo no funciona:

1. Revisa los logs del backend
2. Revisa los logs de Android (Logcat)
3. Verifica que backend y app están en la misma red
4. Asegúrate de que Groq API key es válida

---

**¡Listo para usar! 🚀🌱**
