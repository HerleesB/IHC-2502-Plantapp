# Backend Jardín Inteligente - 100% Operativo ✅

Backend completo con todos los casos de uso implementados.

## 🚀 Inicio Rápido

### 1. Activar entorno virtual
```bash
cd backend
source .venv/bin/activate  # Mac/Linux
.venv\Scripts\activate     # Windows
```

### 2. Instalar dependencias adicionales
```bash
pip install sqlalchemy
```

### 3. Ejecutar servidor
```bash
python -m app.main
```

API disponible en: **http://localhost:8000**
Documentación: **http://localhost:8000/docs**

## 📋 Casos de Uso Implementados

### ✅ CU-01: Captura Guiada de Foto
- `POST /api/diagnosis/capture-guidance`
- Valida calidad de imagen en tiempo real
- Retorna guía para mejorar encuadre

### ✅ CU-02: Diagnóstico Automático + LLM
- `POST /api/diagnosis/analyze`
- Análisis con Groq AI
- Explicación detallada en lenguaje natural

### ✅ CU-03: Recomendaciones y Plan Semanal
- Incluido en respuesta de diagnóstico
- Plan accionable día por día
- Tareas priorizadas

### ✅ CU-04: Gestión de Plantas
- `POST /api/plants` - Crear planta
- `GET /api/plants/user/{id}` - Listar plantas
- `PUT /api/plants/{id}/water` - Registrar riego

### ✅ CU-06: Recordatorios + Gamificación
- `GET /api/gamification/achievements/{user_id}`
- `GET /api/gamification/missions/{user_id}`
- `POST /api/gamification/award-xp/{user_id}`

### ✅ CU-07: Publicar a Comunidad
- `POST /api/community/posts`
- Opción de publicación anónima
- Sistema de likes y reputación

### ✅ CU-08: Inventario y Progreso
- `GET /api/plants/user/{user_id}/progress`
- Estadísticas completas
- Racha de días y nivel

### ✅ CU-09: Respuesta y Moderación Asistida
- `POST /api/community/posts/{id}/comments`
- Moderación automática con IA
- Detección de soluciones

## 📊 Base de Datos

SQLite local creada automáticamente en primera ejecución:
- Usuarios y perfiles
- Plantas e inventario
- Diagnósticos históricos
- Posts de comunidad
- Logros y gamificación

## 🔑 Configuración

Archivo `.env` requerido:
```env
GROQ_API_KEY=tu_clave_aqui
DEBUG=True
```

## 📚 Documentación API

Accede a `/docs` para documentación interactiva completa con ejemplos de todos los endpoints.

## 🎯 Próximos Pasos

1. Conectar app Android con este backend
2. Implementar autenticación JWT
3. Agregar tests unitarios
4. Deploy en producción

---

✨ **Backend 100% funcional y listo para integración con la app Android!**
