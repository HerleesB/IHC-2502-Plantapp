# 🌱 Jardín Inteligente

**Aplicación móvil con IA para diagnóstico inteligente de plantas**

Una aplicación móvil Android que combina inteligencia artificial (Groq AI con modelos Llama Vision), gamificación y comunidad para transformar el cuidado de plantas en una experiencia interactiva, educativa y social. Utiliza inteligencia artificial (Groq AI con modelos Llama Vision) para analizar fotos de plantas y proporcionar diagnósticos precisos sobre su salud, enfermedades y necesidades de cuidado.

---

## 📱 Características

- **📸 Captura Guiada**: Sistema de captura de fotos con retroalimentación en tiempo real
- **🤖 Validación con IA**: Análisis automático de calidad de imagen (encuadre, iluminación, enfoque)
- **🔍 Diagnóstico Inteligente**: Identificación de enfermedades y problemas en plantas
- **💬 Feedback por Voz**: Guía por voz en español durante la captura
- **📳 Retroalimentación Háptica**: Vibraciones para confirmar acciones
- **♿ Accesibilidad**: Diseñado para ser accesible a todos los usuarios
- **📊 Análisis Detallado**: Recomendaciones personalizadas de cuidado

---

## 🏗️ Estructura del Proyecto

```
JardinInteligenApp/
├── backend/              # API Backend (Python/FastAPI)
│   ├── app/             # Código de la aplicación
│   ├── .env.example     # Ejemplo de configuración
│   └── README.md        # Documentación del backend
│
├── frontend/            # App Android (Kotlin/Jetpack Compose)
│   ├── app/            # Código de la aplicación
│   └── README.md       # Documentación del frontend
│
├── .gitignore          # Archivos ignorados por Git
└── README.md           # Este archivo
```

---

## 🚀 Inicio Rápido

### Prerrequisitos

**Backend:**
- Python 3.9+
- Cuenta en [Groq](https://console.groq.com/) (API Key gratuita)

**Frontend:**
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Dispositivo Android 8.0+ (API 26+) o emulador

### 1. Configurar Backend

```bash
# Navegar al directorio backend
cd backend

# Crear entorno virtual
python -m venv .venv

# Activar entorno virtual
# Windows:
.venv\Scripts\activate
# Linux/Mac:
source .venv/bin/activate

# Instalar dependencias
pip install -r requirements.txt

# Configurar variables de entorno
cp .env.example .env
# Editar .env y agregar tu GROQ_API_KEY

# Iniciar servidor
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

El backend estará disponible en: `http://localhost:8000`

Documentación interactiva: `http://localhost:8000/docs`

### 2. Configurar Frontend

```bash
# Abrir proyecto en Android Studio
# File > Open > seleccionar carpeta 'frontend'

# Configurar la IP del backend
# Editar: frontend/app/src/main/java/com/jardin/inteligente/network/ApiConfig.kt
# Cambiar USE_EMULATOR y LOCAL_IP según tu configuración

# Ejecutar la app
# Presionar el botón Run (▶) o Shift+F10
```

### 3. Obtener tu IP Local (para dispositivos físicos)

**Windows:**
```bash
ipconfig
# Buscar "Dirección IPv4" en tu adaptador WiFi
```

**Linux/Mac:**
```bash
ifconfig
# Buscar "inet" en tu interfaz de red
```

---

## 🔧 Configuración Detallada

### Variables de Entorno del Backend

Copia `.env.example` a `.env` y configura:

```bash
# API Key de Groq (obtener en https://console.groq.com/)
GROQ_API_KEY=tu_api_key_aqui

# Modelo para análisis de imágenes
GROQ_MODEL=llama-3.2-11b-vision-preview

# Modelo para texto
GROQ_TEXT_MODEL=llama-3.1-70b-versatile
```

### Configuración de Red del Frontend

Edita `frontend/app/src/main/java/com/jardin/inteligente/network/ApiConfig.kt`:

```kotlin
// Para emulador Android
private const val USE_EMULATOR = true

// Para dispositivo físico (cambiar con tu IP)
private const val USE_EMULATOR = false
private const val LOCAL_IP = "192.168.1.105"
```

**IMPORTANTE**: Tu PC y dispositivo Android deben estar en la misma red WiFi.

---

## 📖 Documentación

- **Backend**: Ver `backend/README.md`
- **Frontend**: Ver `frontend/README.md`
- **Guía de Conexión**: Ver `frontend/SOLUCION_CONEXION.md`
- **Modelos de IA**: Ver `backend/MODELOS_GROQ.md`

---

## 🛠️ Tecnologías

### Backend
- **Framework**: FastAPI
- **IA**: Groq API (Llama 3.2 Vision)
- **Base de Datos**: SQLite / PostgreSQL
- **Procesamiento de Imágenes**: Pillow
- **Validación**: Pydantic

### Frontend
- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Arquitectura**: MVVM
- **Red**: Retrofit + OkHttp
- **Imágenes**: Coil
- **Permisos**: Accompanist Permissions

---

## 🎯 Características Principales

### 1. Captura Guiada con IA
- Análisis en tiempo real de la calidad de la foto
- Retroalimentación instantánea sobre encuadre, iluminación y enfoque
- Guía por voz para usuarios con discapacidad visual

### 2. Validación Inteligente
La IA evalúa:
- ✅ Centrado de la planta
- ✅ Visibilidad completa
- ✅ Enfoque correcto
- ✅ Iluminación adecuada
- ✅ Distancia apropiada

### 3. Diagnóstico Completo
- Identificación de especies
- Detección de enfermedades
- Análisis de plagas
- Recomendaciones de tratamiento
- Plan de cuidado semanal

---

## 🔐 Seguridad

- ✅ API Keys en variables de entorno (nunca en el código)
- ✅ `.env` en `.gitignore`
- ✅ Signing keys de Android excluidas del repositorio
- ✅ CORS configurado para producción
- ✅ Validación de entrada en el backend

---

## 📝 Licencia

Este proyecto es de código abierto bajo la licencia MIT.

---

## 👥 Autores

- Kevin Condor Chavez
- Cesar Sanchez Malaspina
- Herlees Barrientos Porras


---

## 🗺️ Roadmap

- [x] Captura de fotos con validación IA
- [x] Diagnóstico básico de plantas
- [ ] Historial de diagnósticos
- [ ] Compartir en comunidad
- [ ] Sistema de gamificación
- [ ] Recordatorios de cuidado
- [ ] Modo offline
- [ ] Versión iOS

---

## 📊 Estado del Proyecto

**Versión Actual**: 1.0.0

**Estado**: ✅ En desarrollo activo
