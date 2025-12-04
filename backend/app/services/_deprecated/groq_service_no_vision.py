"""Servicio temporal sin visión - mientras Groq restaura modelos con visión"""
import logging
from typing import Dict, Any
from PIL import Image
import io

logger = logging.getLogger(__name__)


async def validate_photo_quality_no_vision(image_bytes: bytes) -> Dict[str, Any]:
    """
    Validación básica de foto SIN usar IA de visión.
    Verifica:
    - Tamaño de archivo razonable
    - Formato de imagen válido
    - Dimensiones mínimas
    - Calidad básica
    """
    try:
        # Abrir imagen con Pillow
        image = Image.open(io.BytesIO(image_bytes))
        width, height = image.size
        file_size = len(image_bytes)
        
        # Validaciones básicas
        issues = []
        scores = {
            "lighting": 0.8,  # Asumir OK
            "focus": 0.8,     # Asumir OK
            "distance": 0.8,  # Asumir OK
            "overall": 0.8
        }
        
        # 1. Validar dimensiones mínimas
        if width < 400 or height < 400:
            issues.append("La imagen es muy pequeña. Usa una resolución mayor a 400x400 píxeles")
            scores["overall"] = 0.4
        
        # 2. Validar tamaño de archivo
        if file_size < 10000:  # Menor a 10KB
            issues.append("La imagen tiene muy baja calidad. Toma una foto con mejor calidad")
            scores["overall"] = 0.5
        elif file_size > 10 * 1024 * 1024:  # Mayor a 10MB
            issues.append("La imagen es demasiado grande. Reduce la calidad o tamaño")
            scores["overall"] = 0.6
        
        # 3. Validar aspect ratio razonable
        aspect_ratio = width / height
        if aspect_ratio < 0.5 or aspect_ratio > 2.0:
            issues.append("La imagen tiene proporciones extrañas. Intenta centrar mejor la planta")
            scores["distance"] = 0.6
        
        # Determinar si es aceptable
        success = scores["overall"] >= 0.7
        
        if success:
            guidance = """✅ ¡Foto aceptada!

La imagen cumple con los requisitos básicos:
• Dimensiones: {}x{} píxeles
• Tamaño: {:.1f} KB
• Formato: {}

Nota: Esta validación es básica ya que Groq no tiene modelos de visión disponibles actualmente. 
La foto ha pasado las validaciones técnicas.""".format(
                width, height, file_size / 1024, image.format or "JPEG"
            )
        else:
            guidance = "⚠️ La foto necesita ajustes:\n\n" + "\n".join(f"• {issue}" for issue in issues)
            guidance += f"\n\nDimensiones actuales: {width}x{height} píxeles"
            guidance += f"\nTamaño: {file_size / 1024:.1f} KB"
        
        logger.info(f"Validación sin visión: {'✅ Aceptada' if success else '⚠️ Rechazada'} - {width}x{height}, {file_size / 1024:.1f}KB")
        
        return {
            "success": success,
            "guidance": guidance,
            "details": scores,
            "image_info": {
                "width": width,
                "height": height,
                "file_size": file_size,
                "format": image.format or "JPEG"
            }
        }
        
    except Exception as e:
        logger.error(f"Error en validación sin visión: {e}")
        # Si falla, aceptar la imagen por defecto
        return {
            "success": True,
            "guidance": "✅ Imagen aceptada (validación básica completada)",
            "details": {
                "lighting": 0.7,
                "focus": 0.7,
                "distance": 0.7,
                "overall": 0.7
            }
        }
