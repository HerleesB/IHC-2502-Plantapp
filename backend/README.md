# 🌱 Jardín Inteligente - Backend API

Backend en Python con FastAPI y Groq AI para diagnóstico inteligente de plantas con visión por computadora.

## 📁 Estructura del Proyecto

```
backend/
├── app/                        # ✅ CÓDIGO FUENTE PRINCIPAL
│   ├── models/                 # Modelos de base de datos (SQLAlchemy)
│   ├── routes/                 # Endpoints de la API
│   ├── services/               # Lógica de negocio (Groq AI, etc.)
│   ├── utils/                  # Utilidades y helpers
│   ├── main.py                 # Punto de entrada FastAPI
│   └── config.py               # Configuración de la aplicación
│
├── scripts/                    # ✅ SCRIPTS DE UTILIDAD (organizados)
│   ├── create_demo_simple.py   # Crear usuario demo
│   ├── create_community_posts.py # Poblar comunidad
│   ├── create_env.py           # Crear archivo .env
│   ├── migrate_add_auth.py     # Migración de autenticación
│   └── reset_db.py             # Reiniciar base de datos
│
├── pruebas_unitarias/          # ✅ SCRIPTS DE PRUEBA/DESARROLLO
│   ├── analizar_base_datos.py  # Diagnóstico de BD
│   ├── verificar_comunidad.py  # Verificar tablas
│   ├── test_connection.py      # Test conexión Groq
│   └── ...                     # Otros scripts de prueba
│
├── tests/                      # Tests formales (pytest)
├── docs/                       # Documentación técnica
├── uploads/                    # Archivos subidos por usuarios
├── cache/                      # Caché de audio
│
├── requirements.txt            # Dependencias principales
├── requirements-dev.txt        # Dependencias de desarrollo
├── pytest.ini                  # Configuración de pytest
├── .env.example                # Ejemplo de variables de entorno
├── jardin.db                   # Base de datos SQLite
├── LIMPIEZA.md                 # Documentación de organización
└── README.md                   # Este archivo
```

## 🚀 Instalación Rápida

### 1. Prerrequisitos

- Python 3.10 o superior
- pip (gestor de paquetes de Python)

### 2. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd JardinInteligenteApp/backend
```

### 3. Crear entorno virtual

```bash
# Crear entorno virtual
python -m venv .venv

# Activar entorno virtual
# En Windows:
.venv\Scripts\activate

# En Linux/Mac:
source .venv/bin/activate
```

### 4. Instalar dependencias

```bash
# Dependencias principales
pip install -r requirements.txt

# Dependencias de desarrollo (opcional)
pip install -r requirements-dev.txt
```

### 5. Configurar variables de entorno

```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar .env y agregar tu GROQ_API_KEY
# Obtén una en: https://console.groq.com
```

Ejemplo de `.env`:
```env
GROQ_API_KEY=gsk_tu_clave_aqui
DATABASE_URL=sqlite:///./jardin.db
SECRET_KEY=tu-clave-secreta-super-segura-aleatoria
DEBUG=True
```

### 6. Inicializar base de datos

```bash
# Crear usuario demo (credentials: demo/demo123)
python scripts/create_demo_simple.py

# (Opcional) Poblar con datos de ejemplo
python scripts/create_community_posts.py
```

### 7. Ejecutar servidor

```bash
# Desarrollo con auto-reload
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# O simplemente:
python -m uvicorn app.main:app --reload
```

El servidor estará disponible en: `http://localhost:8000`

---

## 📚 Documentación de la API

Una vez que el servidor esté corriendo:

- **Swagger UI (interactiva)**: http://localhost:8000/docs
- **ReDoc (documentación)**: http://localhost:8000/redoc
- **Health Check**: http://localhost:8000/health

---

## 📖 Endpoints Principales

### 🔐 Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registrar nuevo usuario |
| POST | `/api/auth/login` | Iniciar sesión |
| GET | `/api/auth/me` | Obtener usuario actual |

### 🔬 Diagnóstico
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/diagnosis/analyze` | Analizar foto de planta |
| GET | `/api/diagnosis/{id}` | Obtener diagnóstico |
| GET | `/api/diagnosis/history` | Historial de diagnósticos |

### 🌿 Plantas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/plants` | Listar plantas del usuario |
| POST | `/api/plants` | Crear nueva planta |
| GET | `/api/plants/{id}` | Detalle de planta |
| PUT | `/api/plants/{id}` | Actualizar planta |
| DELETE | `/api/plants/{id}` | Eliminar planta |

### 👥 Comunidad
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/community/posts` | Listar posts |
| POST | `/api/community/posts` | Crear post |
| POST | `/api/community/posts/{id}/like` | Toggle like |
| POST | `/api/community/posts/{id}/comments` | Comentar |

### 🏆 Gamificación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/gamification/profile` | Perfil del usuario |
| GET | `/api/gamification/achievements` | Logros |
| GET | `/api/gamification/leaderboard` | Tabla de posiciones |

---

## 🛠️ Scripts Útiles

### Scripts de utilidad (`scripts/`)
```bash
# Crear usuario demo (demo/demo123)
python scripts/create_demo_simple.py

# Crear posts de ejemplo
python scripts/create_community_posts.py

# Migración de autenticación
python scripts/migrate_add_auth.py

# Reiniciar base de datos
python scripts/reset_db.py

# Crear archivo .env
python scripts/create_env.py
```

### Scripts de diagnóstico (`pruebas_unitarias/`)
```bash
# Analizar estructura de BD
python pruebas_unitarias/analizar_base_datos.py

# Verificar comunidad
python pruebas_unitarias/verificar_comunidad.py

# Test conexión con Groq
python pruebas_unitarias/test_connection.py
```

---

## 🧪 Testing

```bash
# Ejecutar todos los tests
pytest

# Con verbose
pytest -v

# Con cobertura
pytest --cov=app --cov-report=html
```

---

## 🔧 Tecnologías Principales

| Tecnología | Propósito |
|-----------|-----------|
| **FastAPI** | Framework web moderno y rápido |
| **SQLAlchemy** | ORM para base de datos |
| **Groq AI** | API de IA para visión y análisis |
| **Pydantic** | Validación de datos |
| **Uvicorn** | Servidor ASGI |
| **Python-Jose** | JWT para autenticación |

---

## 🌍 Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `GROQ_API_KEY` | API key de Groq AI | **Requerido** |
| `DATABASE_URL` | URL de la base de datos | `sqlite:///./jardin.db` |
| `SECRET_KEY` | Clave secreta para JWT | **Requerido** |
| `DEBUG` | Modo debug | `True` |
| `GROQ_MODEL` | Modelo de visión | `llama-3.2-11b-vision-preview` |
| `GROQ_TEXT_MODEL` | Modelo de texto | `llama-3.1-70b-versatile` |

---

## 🐛 Debugging

```bash
# Modo verbose
uvicorn app.main:app --reload --log-level debug

# Ver configuración actual
curl http://localhost:8000/config

# Verificar salud del servidor
curl http://localhost:8000/health
```

---

## 📝 Organización del Código

Este proyecto ha sido organizado siguiendo las mejores prácticas:

- **`app/`**: Código fuente principal de la aplicación
- **`scripts/`**: Scripts de utilidad para setup y datos
- **`pruebas_unitarias/`**: Scripts de desarrollo y pruebas
- **`tests/`**: Tests formales (pytest)
- **`docs/`**: Documentación técnica adicional

Para más detalles sobre la organización, ver `LIMPIEZA.md`.

---

## ⚠️ Notas para Producción

Antes de desplegar a producción:
- Cambiar `DEBUG=False`
- Usar PostgreSQL/MySQL en lugar de SQLite
- Configurar `SECRET_KEY` segura y aleatoria
- Configurar CORS apropiadamente
- Usar HTTPS
- Implementar rate limiting

---

## 📄 Licencia

Ver archivo LICENSE en el directorio raíz del proyecto.

---

## 📞 Soporte

Para problemas o preguntas, abre un issue en el repositorio del proyecto.
