"""
CU-05: Rutas API para Comparador Visual de Plantas
Permite comparar fotos antes/después de tratamiento
"""
from fastapi import APIRouter, UploadFile, File, Depends, HTTPException, Form
from sqlalchemy.orm import Session
from typing import Dict, Any, Optional
import logging
import json
from datetime import datetime, timedelta

from app.models.database import get_db
from app.models.comparison_models import PlantComparison, ComparisonMetric
from app.services.groq_service import GroqService
from app.utils.image_processing import save_image
import os

logger = logging.getLogger(__name__)
router = APIRouter()


async def save_comparison_image(upload_file: UploadFile, prefix: str = "") -> str:
    """Guardar imagen de comparación"""
    image_data = await upload_file.read()
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"comparison/{prefix}{timestamp}.jpg"
    
    # Crear directorio si no existe
    os.makedirs("uploads/comparison", exist_ok=True)
    
    # Usar save_image existente
    from PIL import Image
    import io
    from pathlib import Path
    
    img = Image.open(io.BytesIO(image_data))
    path = Path(f"uploads/{filename}")
    path.parent.mkdir(exist_ok=True, parents=True)
    img.save(path)
    
    return str(path)


@router.post("/comparison/create")
async def create_comparison(
    before_image: UploadFile = File(...),
    after_image: UploadFile = File(...),
    user_id: int = Form(...),
    diagnosis_id: Optional[int] = Form(None),
    treatment_applied: Optional[str] = Form(None),
    notes: Optional[str] = Form(None),
    db: Session = Depends(get_db)
) -> Dict[str, Any]:
    """
    CU-05: Crear comparación visual entre dos fotos de planta.
    
    Args:
        before_image: Foto "antes" del tratamiento
        after_image: Foto "después" del tratamiento
        user_id: ID del usuario
        diagnosis_id: ID del diagnóstico relacionado (opcional)
        treatment_applied: Descripción del tratamiento (opcional)
        notes: Notas adicionales del usuario (opcional)
        db: Sesión de base de datos
        
    Returns:
        Análisis comparativo con métricas de mejora
    """
    try:
        # Guardar imágenes
        before_path = await save_comparison_image(before_image, prefix="before_")
        after_path = await save_comparison_image(after_image, prefix="after_")
        
        logger.info(f"Imágenes guardadas: {before_path}, {after_path}")
        
        # Analizar ambas imágenes con Groq
        service = GroqService()
        
        # Leer imágenes
        with open(before_path, "rb") as f:
            before_bytes = f.read()
        with open(after_path, "rb") as f:
            after_bytes = f.read()
        
        # Analizar foto "antes"
        before_result = await service.analyze_image_with_prompt(
            image_bytes=before_bytes,
            prompt="Analiza esta foto de planta y describe su salud en JSON: {health_score: 0-100, issues: [lista]}",
            temperature=0.3,
            max_tokens=300
        )
        
        # Analizar foto "después"
        after_result = await service.analyze_image_with_prompt(
            image_bytes=after_bytes,
            prompt="Analiza esta foto de planta y describe su salud en JSON: {health_score: 0-100, issues: [lista]}",
            temperature=0.3,
            max_tokens=300
        )
        
        # Parsear resultados
        try:
            before_content = before_result["content"].replace("```json", "").replace("```", "").strip()
            after_content = after_result["content"].replace("```json", "").replace("```", "").strip()
            before_data = json.loads(before_content)
            after_data = json.loads(after_content)
        except Exception as parse_error:
            logger.warning(f"Error parseando JSON: {parse_error}, usando valores por defecto")
            before_data = {"health_score": 50, "issues": []}
            after_data = {"health_score": 50, "issues": []}
        
        # Calcular mejora
        health_before = before_data.get("health_score", 50)
        health_after = after_data.get("health_score", 50)
        health_improvement = health_after - health_before
        
        # Determinar progresión
        if health_improvement > 10:
            progression = "improved"
        elif health_improvement < -10:
            progression = "worsened"
        else:
            progression = "stable"
        
        # Crear registro en BD
        comparison = PlantComparison(
            user_id=user_id,
            diagnosis_id=diagnosis_id,
            before_image_path=before_path,
            after_image_path=after_path,
            health_improvement=health_improvement,
            visual_changes=json.dumps({
                "health_before": health_before,
                "health_after": health_after,
                "issues_before": before_data.get("issues", []),
                "issues_after": after_data.get("issues", [])
            }),
            days_between=7,  # Calcular desde timestamps reales si disponible
            treatment_applied=treatment_applied,
            notes=notes
        )
        
        db.add(comparison)
        db.flush()
        
        # Crear métricas
        metrics = ComparisonMetric(
            comparison_id=comparison.id,
            color_health_before=health_before,
            color_health_after=health_after,
            disease_progression=progression
        )
        
        db.add(metrics)
        db.commit()
        
        logger.info(f"Comparación creada: ID={comparison.id}, mejora={health_improvement}%")
        
        return {
            "success": True,
            "comparison_id": comparison.id,
            "health_improvement": round(health_improvement, 1),
            "progression": progression,
            "summary": generate_comparison_summary(health_improvement, progression),
            "details": {
                "before": {
                    "health_score": health_before,
                    "issues": before_data.get("issues", [])[:3]
                },
                "after": {
                    "health_score": health_after,
                    "issues": after_data.get("issues", [])[:3]
                }
            }
        }
        
    except Exception as e:
        logger.error(f"Error en comparación: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Error al procesar comparación: {str(e)}")


@router.get("/comparison/{comparison_id}")
async def get_comparison(
    comparison_id: int,
    db: Session = Depends(get_db)
) -> Dict[str, Any]:
    """Obtener detalles de una comparación existente"""
    comparison = db.query(PlantComparison).filter(
        PlantComparison.id == comparison_id
    ).first()
    
    if not comparison:
        raise HTTPException(status_code=404, detail="Comparación no encontrada")
    
    metrics = db.query(ComparisonMetric).filter(
        ComparisonMetric.comparison_id == comparison_id
    ).first()
    
    return {
        "id": comparison.id,
        "health_improvement": comparison.health_improvement,
        "days_between": comparison.days_between,
        "treatment": comparison.treatment_applied,
        "notes": comparison.notes,
        "progression": metrics.disease_progression if metrics else "unknown",
        "images": {
            "before": comparison.before_image_path,
            "after": comparison.after_image_path
        },
        "created_at": comparison.created_at.isoformat()
    }


@router.get("/comparison/user/{user_id}")
async def get_user_comparisons(
    user_id: int,
    limit: int = 10,
    db: Session = Depends(get_db)
) -> Dict[str, Any]:
    """Obtener todas las comparaciones de un usuario"""
    comparisons = db.query(PlantComparison).filter(
        PlantComparison.user_id == user_id
    ).order_by(PlantComparison.created_at.desc()).limit(limit).all()
    
    return {
        "total": len(comparisons),
        "comparisons": [
            {
                "id": c.id,
                "health_improvement": c.health_improvement,
                "progression": "improved" if c.health_improvement > 10 else "worsened" if c.health_improvement < -10 else "stable",
                "days_between": c.days_between,
                "created_at": c.created_at.isoformat()
            }
            for c in comparisons
        ]
    }


def generate_comparison_summary(improvement: float, progression: str) -> str:
    """Genera un resumen en lenguaje natural de la comparación"""
    if progression == "improved":
        if improvement > 30:
            return f"¡Excelente mejora! Tu planta ha recuperado {improvement:.0f}% de salud. El tratamiento está funcionando muy bien. 🌟"
        elif improvement > 15:
            return f"Tu planta ha mejorado un {improvement:.0f}%. Continúa con el tratamiento actual. 🌿"
        else:
            return f"Hay una ligera mejora del {improvement:.0f}%. Mantén el cuidado constante. 🌱"
    elif progression == "worsened":
        return f"La salud ha disminuido un {abs(improvement):.0f}%. Considera ajustar el tratamiento o consultar un experto. ⚠️"
    else:
        return "La planta se mantiene estable. Continúa monitoreando su evolución. 👀"
