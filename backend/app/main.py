"""
Aplicación principal de FastAPI - Jardín Inteligente API
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from app.config import get_settings
import logging
from pathlib import Path
import os
import hashlib

# Configurar logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# IMPORTANTE: Limpiar cache de configuración al iniciar
get_settings.cache_clear()

# Obtener configuración (después de limpiar cache)
settings = get_settings()

# Crear directorios necesarios
Path(settings.AUDIO_OUTPUT_DIR).mkdir(parents=True, exist_ok=True)
Path("uploads/plants").mkdir(parents=True, exist_ok=True)
Path("uploads/community").mkdir(parents=True, exist_ok=True)
Path("uploads/diagnosis").mkdir(parents=True, exist_ok=True)

# Crear aplicación FastAPI
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="API Backend para Jardín Inteligente - Diagnóstico de plantas con IA",
    debug=settings.DEBUG
)

# Configurar CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # En producción, especificar dominios
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Montar archivos estáticos para servir imágenes
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")


def hash_password(password: str) -> str:
    """Hash SHA256 para contraseñas"""
    return hashlib.sha256(password.encode()).hexdigest()


@app.on_event("startup")
async def startup_event():
    """Evento ejecutado al iniciar la aplicación"""
    logger.info("=" * 60)
    logger.info(f"🌱 Iniciando {settings.APP_NAME} v{settings.APP_VERSION}")
    logger.info(f"📁 Directorio de audio: {settings.AUDIO_OUTPUT_DIR}")
    logger.info(f"🤖 Modelo de Groq (Vision): {settings.GROQ_MODEL}")
    logger.info(f"📝 Modelo de Groq (Text): {settings.GROQ_TEXT_MODEL}")
    logger.info(f"⏱️  Timeout: {settings.GROQ_TIMEOUT}s")
    logger.info(f"🔧 Debug mode: {settings.DEBUG}")
    logger.info("=" * 60)
    
    # Crear usuario demo si no existe
    from app.models.database import SessionLocal, UserDB
    
    db = SessionLocal()
    try:
        demo_user = db.query(UserDB).filter(UserDB.username == "demo").first()
        if not demo_user:
            demo_user = UserDB(
                email="demo@jardininteligente.com",
                username="demo",
                full_name="Usuario Demo",
                hashed_password=hash_password("demo123"),
                level=1,
                xp=0,
                points=0,
                streak_days=0
            )
            db.add(demo_user)
            db.commit()
            logger.info("✅ Usuario demo creado: demo / demo123")
        else:
            logger.info("✅ Usuario demo ya existe")
    except Exception as e:
        logger.error(f"Error creando usuario demo: {e}")
    finally:
        db.close()


@app.on_event("shutdown")
async def shutdown_event():
    """Evento ejecutado al cerrar la aplicación"""
    logger.info("👋 Cerrando Jardín Inteligente API")


@app.get("/")
async def root():
    """Endpoint raíz con información de la API"""
    return {
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "status": "running",
        "groq_model": settings.GROQ_MODEL,
        "groq_text_model": settings.GROQ_TEXT_MODEL,
        "docs": "/docs",
        "redoc": "/redoc",
        "endpoints": {
            "auth": "/api/auth",
            "plants": "/api/plants",
            "diagnosis": "/api/diagnosis",
            "community": "/api/community",
            "gamification": "/api/gamification",
            "reminders": "/api/reminders"
        }
    }


@app.get("/health")
async def health_check():
    """Health check endpoint para verificar el estado de la API"""
    return {
        "status": "healthy",
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "groq_model": settings.GROQ_MODEL,
        "groq_text_model": settings.GROQ_TEXT_MODEL
    }


@app.get("/config")
async def get_config():
    """Endpoint para ver la configuración actual (útil para debugging)"""
    return {
        "app_name": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "debug": settings.DEBUG,
        "audio_dir": settings.AUDIO_OUTPUT_DIR,
        "groq_model": settings.GROQ_MODEL,
        "groq_text_model": settings.GROQ_TEXT_MODEL,
        "groq_timeout": settings.GROQ_TIMEOUT,
        "api_key_configured": bool(settings.GROQ_API_KEY),
        "available_vision_models": [
            "llama-3.2-90b-vision-preview (90B - más potente)",
            "llama-3.2-11b-vision-preview (11B - más rápido)"
        ]
    }


# Importar y registrar rutas
from app.routes import diagnosis, plants, community, gamification, auth, reminders
from app.models.database import init_db

# Registrar routers
app.include_router(auth.router)
app.include_router(diagnosis.router)
app.include_router(plants.router)
app.include_router(community.router)
app.include_router(gamification.router)
app.include_router(reminders.router)

# Inicializar base de datos
init_db()


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.DEBUG
    )
