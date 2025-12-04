# app/config.py - VERSIÓN CORREGIDA
from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field
from typing import List

class Settings(BaseSettings):
    """
    Configuración de la aplicación usando variables de entorno
    """
    
    # API Keys - REQUERIDO (con valor por defecto para desarrollo)
    GROQ_API_KEY: str = Field(
        default="",
        description="API Key de Groq - Obtén una en https://console.groq.com/"
    )

    # Configuración de la aplicación
    APP_NAME: str = Field(default="Jardín Inteligente API")
    APP_VERSION: str = Field(default="1.0.0")
    DEBUG: bool = Field(default=True)
    AUDIO_OUTPUT_DIR: str = Field(default="./cache/audio")

    # Configuración de Groq AI
    # Modelos disponibles con VISIÓN en Groq:
    # - llama-3.2-90b-vision-preview (90B params - más potente, más lento)
    # - llama-3.2-11b-vision-preview (11B params - más rápido, suficiente)
    GROQ_MODEL: str = Field(
        default="llama-3.2-11b-vision-preview",
        description="Modelo de Groq para análisis de imágenes (debe soportar visión)"
    )
    
    # Modelo para texto solamente (sin imágenes)
    GROQ_TEXT_MODEL: str = Field(
        default="llama-3.1-70b-versatile",
        description="Modelo de Groq para texto (moderación, etc.)"
    )
    
    GROQ_TIMEOUT: int = Field(default=30, description="Timeout en segundos")

    # CORS
    ALLOWED_ORIGINS: List[str] = Field(default=["*"])

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        case_sensitive=True
    )


# Cache de la configuración para no recargar el .env en cada request
@lru_cache()
def get_settings() -> Settings:
    """
    Obtiene la configuración singleton (cached)
    """
    return Settings()


# Para debugging - imprimir configuración actual
if __name__ == "__main__":
    settings = get_settings()
    print("=" * 60)
    print("CONFIGURACIÓN ACTUAL")
    print("=" * 60)
    print(f"App Name: {settings.APP_NAME}")
    print(f"Version: {settings.APP_VERSION}")
    print(f"Debug: {settings.DEBUG}")
    print(f"Audio Dir: {settings.AUDIO_OUTPUT_DIR}")
    print(f"Groq Model (Vision): {settings.GROQ_MODEL}")
    print(f"Groq Model (Text): {settings.GROQ_TEXT_MODEL}")
    print(f"Groq Timeout: {settings.GROQ_TIMEOUT}s")
    print(f"API Key configurada: {'✓' if settings.GROQ_API_KEY else '✗'}")
    print("=" * 60)
