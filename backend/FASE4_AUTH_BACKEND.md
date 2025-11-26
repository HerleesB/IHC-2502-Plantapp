# 🔐 FASE 4 - AUTENTICACIÓN JWT IMPLEMENTADA

## ✅ COMPONENTES IMPLEMENTADOS

### Backend

#### 1. Sistema de Autenticación (`app/utils/auth.py`)
- ✅ Hash de contraseñas con bcrypt
- ✅ Generación de tokens JWT
- ✅ Validación de tokens
- ✅ Dependency `get_current_user()` para endpoints protegidos
- ✅ Función `authenticate_user()`

#### 2. Servicio de Autenticación (`app/services/auth_service.py`)
- ✅ Registro de usuarios
- ✅ Login con JWT
- ✅ Creación automática de logros iniciales
- ✅ Obtención de perfil de usuario
- ✅ Validación de email y username únicos

#### 3. Rutas de Autenticación (`app/routes/auth.py`)
- ✅ `POST /api/auth/register` - Registro de nuevo usuario
- ✅ `POST /api/auth/login` - Login con JWT
- ✅ `GET /api/auth/me` - Obtener perfil del usuario autenticado
- ✅ `POST /api/auth/logout` - Cerrar sesión
- ✅ `POST /api/auth/refresh` - Refrescar token JWT

#### 4. Rutas Protegidas (`app/routes/plants_auth.py`)
- ✅ Versión con autenticación de todas las rutas de plantas
- ✅ `POST /api/plants/` - Crear planta (asignada al usuario autenticado)
- ✅ `GET /api/plants/` - Obtener plantas del usuario autenticado
- ✅ `GET /api/plants/{id}` - Obtener planta (con validación de propiedad)
- ✅ `PUT /api/plants/{id}` - Actualizar planta
- ✅ `DELETE /api/plants/{id}` - Eliminar planta
- ✅ `PUT /api/plants/{id}/water` - Regar planta
- ✅ `GET /api/plants/me/progress` - Obtener progreso del usuario
- ✅ Endpoints legacy mantenidos para compatibilidad

#### 5. Modelos de Datos
- ✅ `UserDB` ya existía en `database.py`
- ✅ Schemas Pydantic ya existían en `schemas.py`
- ✅ Relaciones con plantas, diagnósticos, posts, logros

#### 6. Migración de Base de Datos (`migrate_add_auth.py`)
- ✅ Script de migración automática
- ✅ Crea tabla `users` si no existe
- ✅ Crea usuario demo (`demo@jardin.app` / `demo123`)
- ✅ Asigna datos existentes al usuario demo
- ✅ Verifica integridad de columnas

#### 7. Configuración
- ✅ Actualizado `.env.example` con `SECRET_KEY`
- ✅ Requirements actualizados (`requirements_auth.txt`)
- ✅ Dependencias: python-jose, passlib, bcrypt

---

## 📋 PASOS DE INSTALACIÓN (BACKEND)

### 1. Instalar nuevas dependencias

```bash
cd backend
pip install python-jose[cryptography] passlib[bcrypt] bcrypt email-validator
```

O usando el archivo de requirements:

```bash
pip install -r requirements_auth.txt
```

### 2. Actualizar archivo .env

Agregar al archivo `.env`:

```env
SECRET_KEY=tu-clave-secreta-super-segura-cambiar-en-produccion
ACCESS_TOKEN_EXPIRE_MINUTES=10080
```

**IMPORTANTE**: En producción, genera una clave aleatoria:

```bash
openssl rand -hex 32
```

### 3. Ejecutar migración de base de datos

```bash
python migrate_add_auth.py
```

Esto creará:
- Tabla `users`
- Usuario demo con credenciales:
  - Email: `demo@jardin.app`
  - Username: `demo`
  - Password: `demo123`

### 4. Reiniciar el servidor

```bash
uvicorn app.main:app --reload
```

### 5. Probar endpoints de autenticación

**Registro:**
```bash
curl -X POST "http://localhost:8000/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "username": "usuario",
    "password": "password123",
    "full_name": "Nombre Completo"
  }'
```

**Login:**
```bash
curl -X POST "http://localhost:8000/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email_or_username": "demo",
    "password": "demo123"
  }'
```

**Obtener perfil (con token):**
```bash
curl -X GET "http://localhost:8000/api/auth/me" \
  -H "Authorization: Bearer TU_TOKEN_JWT_AQUI"
```

---

## 🔄 INTEGRACIÓN CON RUTAS EXISTENTES

### Opción 1: Actualizar rutas existentes

Reemplazar `app/routes/plants.py` con `app/routes/plants_auth.py`:

```bash
cd backend/app/routes
mv plants.py plants_legacy.py
mv plants_auth.py plants.py
```

### Opción 2: Mantener compatibilidad temporal

Mantener ambas versiones y usar:
- `/api/plants/` - Con autenticación JWT
- `/api/plants/user/{user_id}` - Sin autenticación (legacy)

---

## 🎯 ENDPOINTS DISPONIBLES

### Autenticación

| Método | Endpoint | Requiere Auth | Descripción |
|--------|----------|---------------|-------------|
| POST | `/api/auth/register` | No | Registrar nuevo usuario |
| POST | `/api/auth/login` | No | Iniciar sesión |
| GET | `/api/auth/me` | Sí | Obtener perfil |
| POST | `/api/auth/logout` | Sí | Cerrar sesión |
| POST | `/api/auth/refresh` | Sí | Refrescar token |

### Plantas (Con Autenticación)

| Método | Endpoint | Requiere Auth | Descripción |
|--------|----------|---------------|-------------|
| POST | `/api/plants/` | Sí | Crear planta |
| GET | `/api/plants/` | Sí | Listar mis plantas |
| GET | `/api/plants/{id}` | Sí | Obtener planta |
| PUT | `/api/plants/{id}` | Sí | Actualizar planta |
| DELETE | `/api/plants/{id}` | Sí | Eliminar planta |
| PUT | `/api/plants/{id}/water` | Sí | Regar planta |
| GET | `/api/plants/me/progress` | Sí | Mi progreso |

### Legacy (Sin Autenticación)

| Método | Endpoint | Requiere Auth | Descripción |
|--------|----------|---------------|-------------|
| GET | `/api/plants/user/{user_id}` | No | Plantas por user_id |
| GET | `/api/plants/user/{user_id}/progress` | No | Progreso por user_id |

---

## 🔐 FLUJO DE AUTENTICACIÓN

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │
       ├─────► POST /api/auth/register
       │       { email, username, password }
       │
       ◄───── { access_token, user }
       │
       ├─────► POST /api/auth/login
       │       { email_or_username, password }
       │
       ◄───── { access_token, user }
       │
       │ (Guardar token en SecureStorage)
       │
       ├─────► GET /api/plants/
       │       Header: Authorization: Bearer <token>
       │
       ◄───── [ { planta1 }, { planta2 } ]
       │
       ├─────► POST /api/plants/
       │       Header: Authorization: Bearer <token>
       │       Body: { name, species }
       │
       ◄───── { id, name, user_id }
       │
       └─────► POST /api/auth/logout
               Header: Authorization: Bearer <token>
```

---

## ⚠️ CONSIDERACIONES IMPORTANTES

### Seguridad

1. **SECRET_KEY**: DEBE ser cambiada en producción
   - Nunca usar la clave del ejemplo
   - Generar con: `openssl rand -hex 32`
   - Mantener secreta y no versionar

2. **HTTPS**: En producción, usar SIEMPRE HTTPS
   - Los tokens JWT se envían en headers
   - Sin HTTPS, son vulnerables a interceptación

3. **Validación de Contraseñas**: Implementar en frontend
   - Mínimo 8 caracteres
   - Incluir mayúsculas, minúsculas, números
   - Evitar contraseñas comunes

### Base de Datos

1. **Usuario Demo**: Es solo para desarrollo
   - Eliminar en producción
   - O cambiar contraseña a una segura

2. **Migraciones**: El script migra datos existentes
   - Asigna todo al usuario demo
   - Verificar integridad después

### Performance

1. **Tokens JWT**: Expiran en 7 días por defecto
   - Ajustar según necesidades
   - Implementar refresh automático

2. **Bcrypt**: Es lento por diseño (seguridad)
   - No es problema para login/registro
   - No usar en endpoints frecuentes

---

## 🧪 TESTING

### Probar Registro

```python
import requests

response = requests.post(
    "http://localhost:8000/api/auth/register",
    json={
        "email": "test@example.com",
        "username": "testuser",
        "password": "password123",
        "full_name": "Test User"
    }
)
print(response.json())
```

### Probar Login y Endpoint Protegido

```python
import requests

# Login
login_response = requests.post(
    "http://localhost:8000/api/auth/login",
    json={
        "email_or_username": "testuser",
        "password": "password123"
    }
)
token = login_response.json()["access_token"]

# Usar token
plants_response = requests.get(
    "http://localhost:8000/api/plants/",
    headers={"Authorization": f"Bearer {token}"}
)
print(plants_response.json())
```

---

## 📊 ESTRUCTURA DE RESPUESTAS

### Registro/Login Exitoso

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "user": {
    "id": 1,
    "username": "demo",
    "email": "demo@jardin.app",
    "full_name": "Usuario Demo",
    "level": 1,
    "xp": 0,
    "points": 0,
    "streak_days": 0
  }
}
```

### Error de Autenticación

```json
{
  "detail": "Email/usuario o contraseña incorrectos"
}
```

### Error de Token Inválido

```json
{
  "detail": "Token inválido o expirado"
}
```

---

## ✅ CHECKLIST DE VERIFICACIÓN

### Backend
- [ ] Dependencias instaladas (jose, passlib, bcrypt)
- [ ] SECRET_KEY configurada en .env
- [ ] Migración ejecutada exitosamente
- [ ] Servidor reiniciado
- [ ] Endpoint de registro funcional
- [ ] Endpoint de login funcional
- [ ] Token JWT generado correctamente
- [ ] Endpoints protegidos requieren token
- [ ] Usuario demo creado

### Próximo Paso: Frontend Flutter
- [ ] Crear pantallas de Login/Registro
- [ ] Implementar AuthRepository
- [ ] Implementar AuthViewModel
- [ ] Configurar SecureStorage para tokens
- [ ] Crear interceptor HTTP para tokens
- [ ] Actualizar ViewModels existentes
- [ ] Implementar flujo de autenticación

---

## 🚀 SIGUIENTE PASO: FRONTEND FLUTTER

Una vez verificado que el backend funciona, continuar con la implementación del frontend en Flutter (Fase 4 - Parte 2).
