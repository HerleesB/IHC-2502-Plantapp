"""Utilidades para procesamiento de imágenes (CU-01)"""
from PIL import Image
import io
import base64
from pathlib import Path
from datetime import datetime

async def validate_image_quality(image_data: bytes) -> dict:
    """CU-01: Validar calidad de imagen para captura guiada"""
    img = Image.open(io.BytesIO(image_data))
    width, height = img.size
    
    # Validaciones básicas
    quality = {
        "lighting": 0.7,  # Simulado
        "focus": 0.8,
        "distance": 0.6,
        "overall": 0.7
    }
    return quality

async def save_image(image_data: bytes, plant_id: int) -> str:
    """Guardar imagen de planta"""
    img = Image.open(io.BytesIO(image_data))
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"plant_{plant_id}_{timestamp}.jpg"
    path = Path(f"uploads/{filename}")
    path.parent.mkdir(exist_ok=True)
    img.save(path)
    return str(path)
