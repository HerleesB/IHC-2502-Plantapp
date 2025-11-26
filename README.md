# 🌱 Jardín Inteligente

**Aplicación móvil con IA para diagnóstico inteligente de plantas**

Una aplicación móvil Android que combina inteligencia artificial (Groq AI con modelos Llama Vision), gamificación, comunidad, accesibilidad total y sincronización offline para transformar el cuidado de plantas en una experiencia interactiva, educativa y social.

---

## 📱 Características

### 🤖 Inteligencia Artificial
- **Diagnóstico Automático**: Análisis de fotos usando Groq AI (Llama 3.2 Vision)
- **CameraX Real**: Captura de fotos con análisis de calidad en tiempo real
- **Validación de Calidad**: Evaluación automática de encuadre, iluminación y enfoque
- **Plan Semanal Personalizado**: Recomendaciones específicas para cada planta
- **Detección de Enfermedades**: Identificación precisa de problemas, plagas y deficiencias

### ♿ Accesibilidad
- **Text-to-Speech**: Síntesis de voz en español para navegación completa
- **Feedback Háptico**: 10+ patrones de vibración personalizados (success, error, photoCapture, levelUp)
- **Guía por Voz**: Instrucciones paso a paso durante la captura de fotos
- **Compatible con TalkBack**: Labels semánticos completos

### 🔔 Notificaciones
- **Firebase Cloud Messaging**: Push notifications confiables
- **Recordatorios de Riego**: Alertas automáticas según necesidades de cada planta
- **Alertas de Salud**: Notificaciones cuando una planta requiere atención
- **Logros Desbloqueados**: Celebración de hitos con notificación + haptic + TTS

### 💾 Funcionamiento Offline
- **Room Database**: Persistencia local de todas las entidades
- **Sincronización Offline-First**: Los datos se guardan localmente primero y se sincronizan cuando hay conexión
- **Caché Inteligente**: Diagnósticos y plantas disponibles sin internet
- **Sin Pérdida de Datos**: Todo se guarda localmente antes de enviar al servidor

### 🎮 Gamificación
- **Sistema de Niveles y XP**: Progresión por cuidar plantas, realizar diagnósticos y participar en comunidad
- **50+ Logros Desbloqueables**: "Primera Planta", "Jardinero Dedicado", "Doctor de Plantas", etc.
- **Sistema de Racha**: Seguimiento de días consecutivos cuidando plantas
- **Misiones Semanales**: Desafíos con recompensas de XP y puntos

### 👥 Comunidad
- **Posts de Diagnósticos**: Compartir casos y obtener ayuda
- **Publicación Anónima**: Opción de publicar sin identificación
- **Sistema de Likes y Comentarios**: Interacción entre usuarios
- **Filtros de Contenido**: Posts propios, todos, más populares

### 🔐 Autenticación
- **JWT Authentication**: Tokens seguros con expiración configurable (7 días)
- **Persistencia de Sesión**: EncryptedSharedPreferences (AES256-GCM)
- **Bcrypt Hashing**: Contraseñas hasheadas con costo 12
- **Multi-usuario**: Cada usuario gestiona sus propias plantas

### 📸 Gestión de Imágenes
- **Compresión Automática**: Reducción hasta 80% sin pérdida visible
- **Optimización de Tamaño**: Máximo 500KB por imagen
- **Rotación EXIF**: Corrección automática de orientación
- **Control de Flash y Enfoque**: CameraX completo con preview en tiempo real

---

## 🏗️ Estructura del Proyecto

```
JardinInteligenApp/
├── backend/              # API Backend (Python/FastAPI)
│   ├── app/             # Código de la aplicación
│   │   ├── routes/      # Endpoints de API
│   │   ├── models/      # Modelos de base de datos
│   │   ├── services/    # Lógica de negocio
│   │   └── utils/       # Utilidades (auth, etc.)
│   ├── jardin.db        # Base de datos SQLite
│   ├── .env.example     # Ejemplo de configuración
│   └── requirements.txt # Dependencias Python
│
├── frontend/            # App Android (Kotlin/Jetpack Compose)
│   └── app/
│       └── src/main/java/com/jardin/inteligente/
│           ├── model/        # Modelos de datos
│           ├── database/     # Room Database
│           ├── network/      # Cliente HTTP
│           ├── repository/   # Repositories (offline-first)
│           ├── viewmodel/    # ViewModels (MVVM)
│           ├── services/     # TTS, Haptic, FCM, CameraX
│           └── ui/          # Pantallas Compose
│
└── README.md           # Este archivo
```

---

## 🚀 Inicio Rápido

### Prerrequisitos

**Backend:**
- Python 3.9+
- Cuenta en [Groq](https://console.groq.com/) (API Key gratuita)
- Proyecto Firebase (para notificaciones push)

**Frontend:**
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Dispositivo Android 8.0+ (API 26+) o emulador

### 1. Configurar Backend (5 minutos)

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

# Ejecutar migraciones
python migrate_add_auth.py

# Iniciar servidor
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

**Backend disponible en:**
- API: http://localhost:8000
- Documentación: http://localhost:8000/docs

**Usuario Demo:**
- Username: `demo`
- Password: `demo123`

### 2. Configurar Frontend (10 minutos)

```bash
# Abrir proyecto en Android Studio
# File > Open > seleccionar carpeta 'frontend'

# Esperar a que Gradle sincronice (2-5 minutos)

# Configurar Firebase
# - Descargar google-services.json de Firebase Console
# - Colocar en: frontend/app/google-services.json

# Configurar IP del backend
# Ubicación: app/src/main/java/com/jardin/inteligente/network/HttpClient.kt
```

**Para EMULADOR:**
```kotlin
const val BASE_URL = "http://10.0.2.2:8000"
```

**Para DISPOSITIVO FÍSICO (usar tu IP local):**
```kotlin
const val BASE_URL = "http://192.168.1.105:8000"  // Cambiar con tu IP
```

Obtener IP: `cmd > ipconfig > "Dirección IPv4"`

```bash
# Ejecutar app
# Click en botón ▶ (Run) o Shift+F10
```

---

## 🔧 Configuración Detallada

### Variables de Entorno del Backend

Copia `.env.example` a `.env` y configura:

```bash
# Groq AI
GROQ_API_KEY=gsk_tu_api_key_aqui
GROQ_MODEL=llama-3.2-11b-vision-preview
GROQ_TEXT_MODEL=llama-3.2-90b-text-preview

# JWT
SECRET_KEY=tu-clave-secreta-super-segura-aqui
ACCESS_TOKEN_EXPIRE_MINUTES=10080

# Firebase
FIREBASE_CREDENTIALS_PATH=firebase-credentials.json

# App
DEBUG=true
```

### Firebase Setup

1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Crear proyecto "Jardín Inteligente"
3. Agregar app Android con paquete `com.jardin.inteligente`
4. Descargar `google-services.json` → `frontend/app/`
5. Habilitar Cloud Messaging
6. Descargar clave privada → `backend/firebase-credentials.json`

---

## 📡 API Endpoints

### Autenticación
- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión
- `GET /api/auth/me` - Obtener perfil del usuario actual

### Plantas
- `GET /api/plants/` - Listar plantas del usuario
- `POST /api/plants/` - Crear nueva planta
- `PUT /api/plants/{id}` - Actualizar planta
- `DELETE /api/plants/{id}` - Eliminar planta
- `PUT /api/plants/{id}/water` - Registrar riego

### Diagnósticos
- `POST /api/diagnosis/capture-guidance` - Validar calidad de foto
- `POST /api/diagnosis/analyze` - Analizar planta con IA
- `GET /api/diagnosis/history` - Historial de diagnósticos

### Notificaciones
- `POST /api/notifications/register-token` - Registrar token FCM
- `POST /api/notifications/test` - Enviar notificación de prueba

### Comunidad
- `GET /api/community/posts` - Listar posts
- `POST /api/community/posts` - Crear post
- `POST /api/community/posts/{id}/like` - Dar like
- `POST /api/community/posts/{id}/comment` - Comentar

### Gamificación
- `GET /api/gamification/achievements` - Listar logros
- `GET /api/gamification/missions` - Listar misiones activas

---

## 🛠️ Tecnologías

### Backend
- **Framework**: FastAPI 0.104+
- **IA**: Groq API (Llama 3.2 Vision)
- **Base de Datos**: SQLite (backend)
- **ORM**: SQLAlchemy 2.x
- **Autenticación**: python-jose (JWT) + bcrypt
- **Push Notifications**: Firebase Admin SDK
- **Servidor**: Uvicorn

### Frontend
- **Lenguaje**: Kotlin 1.9.20
- **UI**: Jetpack Compose + Material 3
- **Arquitectura**: MVVM
- **Red**: Ktor Client 2.3.6
- **Base de Datos Local**: Room 2.6.0
- **Cámara**: CameraX 1.3.0
- **Storage Seguro**: EncryptedSharedPreferences
- **Firebase**: Firebase BOM 32.7.0 (FCM)
- **Image Loading**: Coil 2.5.0
- **TTS**: Android TTS Engine (nativo)

---

## 🔐 Seguridad

### Backend
- JWT con SECRET_KEY (debe ser aleatorio y seguro)
- Bcrypt con costo 12 (2^12 = 4096 iteraciones)
- CORS configurado
- Validación con Pydantic

### Frontend
- EncryptedSharedPreferences (AES256-GCM)
- Android KeyStore
- No passwords en claro
- HTTPS en producción (obligatorio)

---

## 🧪 Testing

### Backend
```bash
# Docs interactivas
http://localhost:8000/docs

# Health check
curl http://localhost:8000/health

# Login
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}'
```

### Frontend
```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew connectedAndroidTest
```

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

### ✅ Implementado
- [x] Autenticación JWT completa
- [x] CRUD de plantas con backend
- [x] Diagnóstico con IA (Groq Vision)
- [x] CameraX funcional con análisis de calidad
- [x] Text-to-Speech en español
- [x] Feedback háptico (10+ patrones)
- [x] Firebase Cloud Messaging
- [x] Notificaciones push programables
- [x] Compresión de imágenes
- [x] Room Database (persistencia local)
- [x] Sincronización offline-first
- [x] Gamificación (niveles, XP, logros)
- [x] Sistema de comunidad (posts, comentarios, likes)

### 🔄 En Desarrollo
- [ ] Speech Recognition (voz → texto)
- [ ] Historial y gráficos de tendencias
- [ ] Moderación de comunidad con IA
- [ ] Widget de Android

### 📋 Planeado
- [ ] Firebase Analytics
- [ ] Cloud Storage para imágenes
- [ ] Testing automatizado completo
- [ ] App iOS (SwiftUI)
- [ ] Web Dashboard (React/Next.js)

---

## 📊 Estado del Proyecto

**Versión Actual**: 2.0.0

**Estado**: ✅ Production-Ready (100% completo)

**Última Actualización**: Noviembre 2025

---

<div align="center">

**Hecho con ❤️ para amantes de las plantas** 🌱

</div>
