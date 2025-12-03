# 🌱 Jardín Inteligente - Backend API

Backend en Python con FastAPI y Groq AI para diagnóstico inteligente de plantas con visión por computadora.

## 📁 Estructura del Proyecto

```
backend/
├── app/                    # Código fuente principal
│   ├── models/            # Modelos de base de datos (SQLAlchemy)
│   ├── routes/            # Endpoints de la API
│   ├── services/          # Lógica de negocio
│   ├── utils/             # Utilidades y helpers
│   ├── main.py            # Punto de entrada FastAPI
│   └── config.py          # Configuración de la aplicación
├── scripts/               # Scripts de utilidad
│   ├── create_demo_simple.py          # Crear usuario demo
│   ├── create_community_posts.py      # Poblar comunidad
│   ├── create_community_posts_simple.py
│   ├── migrate_add_auth.py            # Migración de autenticación
│   └── install.sh                     # Scripts de instalación
├── tests/                 # Tests unitarios y de integración
├── docs/                  # Documentación técnica
│   ├── FASE1_COMPLETADA.md
│   ├── FASE4_AUTH_BACKEND.md
│   └── MODELOS_GROQ.md
├── uploads/               # Archivos subidos por usuarios
├── requirements.txt       # Dependencias principales
├── requirements-dev.txt   # Dependencias de desarrollo
├── pytest.ini             # Configuración de pytest
├── .env.example          # Ejemplo de variables de entorno
├── .gitignore
└── README.md
```

## 🚀 Instalación Rápida

### 1. Prerrequisitos

- Python 3.10 o superior
- pip (gestor de paquetes de Python)

### 2. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd IHC-2502-Plantapp/backend
```

### 3. Crear entorno virtual

```bash
# Crear entorno virtual
python -m venv venv

# Activar entorno virtual
# En Windows:
venv\Scripts\activate

# En Linux/Mac:
source venv/bin/activate
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

# Editar .env y agregar tus credenciales
# Necesitas una GROQ_API_KEY de https://console.groq.com
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
# Crear usuario demo
python scripts/create_demo_simple.py

# (Opcional) Poblar con datos de ejemplo
python scripts/create_community_posts.py
```

### 7. Ejecutar servidor

```bash
# Opción 1: Con uvicorn (recomendado para desarrollo)
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Opción 2: Usando Python directamente
python -m app.main

# Opción 3: Con el script de la app
cd app && python main.py
```

El servidor estará disponible en: `http://localhost:8000`

## 📚 Documentación de la API

Una vez que el servidor esté corriendo, puedes acceder a:

- **Swagger UI (interactiva)**: http://localhost:8000/docs
- **ReDoc (documentación)**: http://localhost:8000/redoc
- **Health Check**: http://localhost:8000/health
- **Configuración actual**: http://localhost:8000/config

## 🧪 Testing

```bash
# Ejecutar todos los tests
pytest

# Ejecutar tests con verbose
pytest -v

# Ejecutar tests con cobertura
pytest --cov=app --cov-report=html

# Ver reporte de cobertura
open htmlcov/index.html  # En Mac/Linux
start htmlcov/index.html  # En Windows
```

## 🛠️ Scripts Útiles

### Gestión de usuarios

```bash
# Crear usuario demo (credentials: demo/demo123)
python scripts/create_demo_simple.py

# Migrar base de datos para autenticación
python scripts/migrate_add_auth.py
```

### Datos de prueba

```bash
# Crear posts de ejemplo en la comunidad
python scripts/create_community_posts.py

# Versión simplificada
python scripts/create_community_posts_simple.py

# Crear archivo .env desde cero
python scripts/create_env.py
```

## 🔧 Tecnologías Principales

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **FastAPI** | 0.104.1 | Framework web moderno y rápido |
| **SQLAlchemy** | 2.0.23 | ORM para base de datos |
| **Groq AI** | 0.4.0 | API de IA para visión y análisis |
| **Pydantic** | 2.5.0 | Validación de datos |
| **Uvicorn** | 0.24.0 | Servidor ASGI |
| **Python-Jose** | 3.3.0 | JWT para autenticación |
| **Pillow** | 10.1.0 | Procesamiento de imágenes |
| **gTTS** | 2.4.0 | Text-to-Speech |

## 📖 Endpoints Principales

### Autenticación
- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión
- `GET /api/auth/me` - Obtener usuario actual

### Diagnóstico
- `POST /api/diagnosis/analyze` - Analizar foto de planta
- `GET /api/diagnosis/{id}` - Obtener diagnóstico
- `GET /api/diagnosis/history` - Historial de diagnósticos

### Plantas
- `GET /api/plants` - Listar plantas del usuario
- `POST /api/plants` - Crear nueva planta
- `GET /api/plants/{id}` - Detalle de planta
- `PUT /api/plants/{id}` - Actualizar planta
- `DELETE /api/plants/{id}` - Eliminar planta

### Comunidad
- `GET /api/community/posts` - Listar posts
- `POST /api/community/posts` - Crear post
- `POST /api/community/posts/{id}/like` - Dar like
- `POST /api/community/posts/{id}/comments` - Comentar

### Gamificación
- `GET /api/gamification/profile` - Perfil del usuario
- `GET /api/gamification/achievements` - Logros
- `GET /api/gamification/leaderboard` - Tabla de posiciones

## 🔐 Autenticación

La API utiliza JWT (JSON Web Tokens) para autenticación. Para endpoints protegidos:

```bash
# 1. Obtener token
curl -X POST "http://localhost:8000/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}'

# 2. Usar token en requests
curl -X GET "http://localhost:8000/api/plants" \
  -H "Authorization: Bearer {tu_token_aqui}"
```

## 🌍 Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `GROQ_API_KEY` | API key de Groq AI | **Requerido** |
| `DATABASE_URL` | URL de la base de datos | `sqlite:///./jardin.db` |
| `SECRET_KEY` | Clave secreta para JWT | **Requerido** |
| `DEBUG` | Modo debug | `True` |
| `GROQ_MODEL` | Modelo de visión | `llama-3.2-90b-vision-preview` |
| `GROQ_TEXT_MODEL` | Modelo de texto | `llama-3.3-70b-versatile` |
| `GROQ_TIMEOUT` | Timeout en segundos | `30` |
| `ALLOWED_ORIGINS` | CORS origins | `["http://localhost:3000"]` |

## 📝 Modelos de IA Disponibles

El backend utiliza dos modelos de Groq AI:

### Visión (Análisis de imágenes)
- **llama-3.2-90b-vision-preview** (90B) - Más potente y preciso
- **llama-3.2-11b-vision-preview** (11B) - Más rápido

### Texto (Generación de respuestas)
- **llama-3.3-70b-versatile** - Recomendado
- **mixtral-8x7b-32768** - Alternativa rápida

Ver `docs/MODELOS_GROQ.md` para más detalles.

## 🐛 Debugging

```bash
# Modo verbose
uvicorn app.main:app --reload --log-level debug

# Ver configuración actual
curl http://localhost:8000/config

# Verificar salud del servidor
curl http://localhost:8000/health
```

## 📦 Estructura de Base de Datos

- **users** - Usuarios de la aplicación
- **plants** - Plantas registradas por usuarios
- **diagnoses** - Diagnósticos realizados
- **community_posts** - Posts de la comunidad
- **comments** - Comentarios en posts
- **achievements** - Logros desbloqueados

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Ver archivo LICENSE en el directorio raíz del proyecto.

## 👥 Equipo

Proyecto desarrollado como parte del curso de Interacción Humano-Computadora.

## 🔗 Enlaces Útiles

- [Documentación de FastAPI](https://fastapi.tiangolo.com/)
- [Groq AI Console](https://console.groq.com/)
- [SQLAlchemy Docs](https://docs.sqlalchemy.org/)
- [Pydantic Docs](https://docs.pydantic.dev/)

## ⚠️ Notas Importantes

- **Desarrollo**: La configuración actual está optimizada para desarrollo local
- **Producción**: Antes de desplegar a producción:
  - Cambiar `DEBUG=False`
  - Usar una base de datos PostgreSQL/MySQL
  - Configurar SECRET_KEY segura y aleatoria
  - Configurar CORS apropiadamente
  - Usar HTTPS
  - Implementar rate limiting

## 📞 Soporte

Para problemas o preguntas, abre un issue en el repositorio del proyecto.
