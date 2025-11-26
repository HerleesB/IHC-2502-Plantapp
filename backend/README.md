# Jardin Inteligente - Backend API

Backend en Python con FastAPI y Groq AI para diagnostico inteligente de plantas.

## Instalacion Rapida

### Paso 1: Crear entorno virtual

```bash
cd backend
python3 -m venv venv
source venv/bin/activate
```

### Paso 2: Instalar dependencias

```bash
pip install -r requirements.txt
```

### Paso 3: Configurar variables de entorno

```bash
cp .env .env
# Editar .env y agregar tu GROQ_API_KEY
```

### Paso 4: Ejecutar

```bash
python -m app.main
```

API disponible en: http://localhost:8000
Documentacion: http://localhost:8000/docs

## Estado del Proyecto

FASE 1-2 COMPLETADA:
- Configuracion con Pydantic Settings
- Cliente de Groq AI
- Prompts especializados
- Procesamiento de imagenes

Proxima fase: Validacion de encuadre
