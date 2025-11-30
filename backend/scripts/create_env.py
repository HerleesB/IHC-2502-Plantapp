"""
Script para crear variable de entorno
Ejecutar: python scripts/create_env.py
"""
import sys
from pathlib import Path

# Ajustar sys.path para importar desde directorio padre
sys.path.insert(0, str(Path(__file__).parent.parent))

def create_env_file():
    """Crea archivo .env con configuración básica"""
    env_content = """# API Keys
GROQ_API_KEY=tu_clave_api_aqui

# Database
DATABASE_URL=sqlite:///./jardin.db

# JWT Secret
SECRET_KEY=tu-clave-secreta-super-segura-aleatoria

# App Config
APP_NAME=Jardín Inteligente
APP_VERSION=1.0.0
DEBUG=True

# Models
GROQ_MODEL=llama-3.2-90b-vision-preview
GROQ_TEXT_MODEL=llama-3.3-70b-versatile
GROQ_TIMEOUT=30

# Audio
AUDIO_OUTPUT_DIR=audio_cache

# CORS
ALLOWED_ORIGINS=["http://localhost:3000","http://localhost:19006"]
"""
    
    env_path = Path(__file__).parent.parent / ".env"
    
    if env_path.exists():
        print(f"⚠️  El archivo .env ya existe en: {env_path}")
        respuesta = input("¿Deseas sobrescribirlo? (s/n): ")
        if respuesta.lower() != 's':
            print("Cancelado")
            return
    
    with open(env_path, 'w', encoding='utf-8') as f:
        f.write(env_content)
    
    print(f"✅ Archivo .env creado exitosamente en: {env_path}")
    print("\n⚠️  IMPORTANTE: Edita el archivo .env y agrega tu GROQ_API_KEY")

if __name__ == "__main__":
    create_env_file()
