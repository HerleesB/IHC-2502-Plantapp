# ⚡ GUÍA RÁPIDA DE EJECUCIÓN

## 🚀 Inicio Rápido en 5 Pasos

### 1️⃣ Configurar Backend

```bash
cd backend

# Verificar que existe el archivo .env con tu API Key de Groq
# Si no existe, créalo:
echo "GROQ_API_KEY=tu_api_key_aqui" > .env

# Activar entorno virtual (si lo tienes)
# Windows:
.venv\Scripts\activate
# Mac/Linux:
source .venv/bin/activate

# Instalar dependencias (si es primera vez)
pip install -r app/requirements.txt

# Iniciar backend
python -m app.main
```

**Debes ver**:
```
🌱 Iniciando Jardín Inteligente v1.0.0
📁 Directorio de audio: cache/audio
🤖 Modelo de Groq: llama-3.2-90b-vision-preview
INFO:     Uvicorn running on http://0.0.0.0:8000
```

---

### 2️⃣ Configurar IP para Dispositivo Físico (Opcional)

**Solo si usarás dispositivo físico en lugar de emulador**

1. Obtén tu IP local:
   - Windows: `ipconfig` → busca "IPv4 Address"
   - Mac/Linux: `ifconfig` → busca "inet"
   
2. Abre: `app/src/main/java/com/jardin/inteligente/network/ApiConfig.kt`

3. Cambia:
```kotlin
private const val USE_EMULATOR = false  // Cambiar a false
private const val LOCAL_IP = "192.168.1.100" // Tu IP aquí
```

**Si usas emulador, NO CAMBIES NADA** (10.0.2.2 está bien)

---

### 3️⃣ Sincronizar Gradle

En Android Studio:
1. File → Sync Project with Gradle Files
2. Espera a que termine (puede tardar 1-2 minutos)

---

### 4️⃣ Ejecutar la App

1. Conecta dispositivo Android o inicia emulador
2. En Android Studio: Run → Run 'app' (Shift + F10)
3. Espera a que compile e instale

---

### 5️⃣ Probar la Funcionalidad

1. En la app, toca el tab inferior "Captura con Validación IA" o navega ahí
2. Toca "Tomar foto" o "Galería"
3. Captura/selecciona una foto de una planta
4. Toca "Validar con IA"
5. **Espera 5-10 segundos**
6. Observa el mensaje de la IA

**Foto buena** → Card verde con ✅
**Foto mala** → Card roja con ⚠️ y sugerencias

---

## 🔧 Verificación Rápida

### Verificar que el backend funciona:

**Desde navegador o Postman**:
```
http://localhost:8000/health
```

Debe responder:
```json
{
  "status": "healthy",
  "app": "Jardín Inteligente API",
  "version": "1.0.0"
}
```

### Verificar desde el emulador/dispositivo:

**Emulador**:
```
http://10.0.2.2:8000/health
```

**Dispositivo físico**:
```
http://TU_IP_LOCAL:8000/health
```

---

## ⚠️ Si algo no funciona

### Backend no inicia:
```bash
# Reinstalar dependencias
pip install -r app/requirements.txt

# Verificar que .env existe
cat .env  # Linux/Mac
type .env  # Windows
```

### App no compila:
```bash
# En Android Studio:
Build → Clean Project
Build → Rebuild Project
File → Invalidate Caches and Restart
```

### "No se puede conectar al servidor":
1. ✅ Backend está corriendo
2. ✅ Dispositivo y PC en la misma red WiFi
3. ✅ IP configurada correctamente en ApiConfig.kt
4. ✅ Firewall no bloquea puerto 8000

### "Error al validar la imagen":
1. ✅ GROQ_API_KEY configurada en backend/.env
2. ✅ API Key válida (obtener en https://console.groq.com/)
3. ✅ Conexión a internet activa

---

## 📊 Estructura del Proyecto

```
JardinInteligenApp2/
├── backend/
│   ├── app/
│   │   ├── services/
│   │   │   └── groq_service.py     ← Validación con IA
│   │   ├── routes/
│   │   │   └── diagnosis.py        ← Endpoint /capture-guidance
│   │   └── utils/
│   │       └── prompts.py          ← Prompts de IA
│   ├── .env                        ← ⚠️ API Key aquí
│   └── test_photo_validation.py   ← Script de prueba
│
└── app/
    └── src/main/java/com/jardin/inteligente/
        ├── network/
        │   ├── ApiConfig.kt        ← ⚠️ Configurar IP aquí
        │   └── ApiService.kt       ← Retrofit
        ├── repository/
        │   └── DiagnosisRepository.kt
        ├── viewmodel/
        │   └── CaptureViewModel.kt
        └── ui/screens/
            └── AccessibleCaptureScreen.kt  ← UI principal
```

---

## 🎯 Archivos Clave a Revisar

1. **Backend**:
   - `backend/.env` → GROQ_API_KEY
   - `backend/app/routes/diagnosis.py` → Endpoint

2. **Android**:
   - `app/src/main/java/com/jardin/inteligente/network/ApiConfig.kt` → IP
   - `app/build.gradle.kts` → Dependencias

3. **Documentación**:
   - `IMPLEMENTACION_COMPLETA.md` → Documentación detallada
   - `backend/FASE1_COMPLETADA.md` → Info del backend

---

## ✅ Checklist Pre-Ejecución

Antes de ejecutar, verifica:

- [ ] Backend corriendo en http://localhost:8000
- [ ] .env con GROQ_API_KEY configurado
- [ ] Android Studio sin errores de compilación
- [ ] Gradle sincronizado correctamente
- [ ] ApiConfig.kt con IP correcta (si usas dispositivo físico)
- [ ] Dispositivo/emulador conectado
- [ ] Permisos de cámara otorgados en el dispositivo

---

## 🚀 ¡Listo!

Si todo está configurado correctamente, la app debería funcionar perfectamente.

**Tiempo estimado total**: 5-10 minutos
