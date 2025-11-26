# app/config.py - VERSIÓN CORREGIDA
from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field
from typing import List

class Settings(BaseSettings):
    # Requeridos
    GROQ_API_KEY: str = Field(..., env="GROQ_API_KEY")

    # Opcionales con defaults
    APP_NAME: str = "Jardín Inteligente API"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = True
    AUDIO_OUTPUT_DIR: str = "cache/audio"
    GROQ_MODEL: str = "meta-llama/llama-4-maverick-17b-128e-instruct"
    GROQ_TEXT_MODEL: str = "openai/gpt-oss-120b"
    GROQ_TIMEOUT: int = 30

    # CORS
    ALLOWED_ORIGINS: List[str] = ["*"]

    model_config = SettingsConfigDict(
        env_file=".env",  # ← CORREGIDO: era "backend/.env"
        env_file_encoding="utf-8",
        extra="ignore"
    )

@lru_cache()
def get_settings() -> Settings:
    return Settings()
