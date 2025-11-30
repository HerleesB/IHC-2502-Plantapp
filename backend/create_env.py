#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script para crear archivo .env con codificación correcta
"""

env_content = """GROQ_API_KEY=tu_api_key_aqui
GROQ_MODEL=llama-3.2-11b-vision-preview
GROQ_TEXT_MODEL=llama-3.1-70b-versatile
GROQ_TIMEOUT=30
APP_NAME=Jardin Inteligente
APP_VERSION=1.0.0
DEBUG=True
AUDIO_OUTPUT_DIR=./cache/audio
ALLOWED_ORIGINS=["*"]
"""

# Escribir archivo con codificación UTF-8
with open('.env', 'w', encoding='utf-8') as f:
    f.write(env_content)

print("✅ Archivo .env creado correctamente")
print("⚠️  Ahora edita el archivo y cambia 'tu_api_key_aqui' por tu API Key real")
print("\nPuedes editarlo con:")
print("  notepad .env")
