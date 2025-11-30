"""Rutas para diagnóstico de plantas (CU-01, CU-02, CU-03, CU-08, CU-12)"""
from fastapi import APIRouter, Depends, HTTPException, File, UploadFile, Form, Request
from sqlalchemy.orm import Session
from typing import Optional
from pydantic import BaseModel
from app.models.database import get_db, DiagnosisDB, PlantDB, DiagnosisFeedbackDB
from app.models.schemas import DiagnosisResponse, CaptureGuidance
from app.services.groq_service import get_plant_diagnosis, validate_photo_quality
from app.utils.image_processing import save_image
import json
from datetime import datetime
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/diagnosis", tags=["Diagnosis"])


class DiagnosisFeedbackRequest(BaseModel):
    """Modelo para feedback de diagnóstico (CU-12)"""
    is_correct: bool
    correct_diagnosis: Optional[str] = None
    feedback_text: Optional[str] = None


def get_full_image_url(image_path: Optional[str], request: Request) -> Optional[str]:
    """Convierte una ruta relativa a una URL completa accesible"""
    if not image_path:
        return None
    
    # Si ya es una URL completa, devolverla tal cual
    if image_path.startswith("http://") or image_path.startswith("https://"):
        return image_path
    
    # Construir URL base del servidor
    base_url = str(request.base_url).rstrip("/")
    
    # Normalizar la ruta (quitar "./" si existe)
    clean_path = image_path.replace("\\", "/")
    if clean_path.startswith("./"):
        clean_path = clean_path[2:]
    
    return f"{base_url}/{clean_path}"


@router.post("/analyze", response_model=DiagnosisResponse)
async def analyze_plant(
    request: Request,
    plant_id: int = Form(0),
    image: UploadFile = File(...),
    symptoms: Optional[str] = Form(None),
    user_id: int = Form(1),
    db: Session = Depends(get_db)
):
    """CU-02: Diagnóstico automático + explicación LLM"""
    # Si plant_id es 0, es un diagnóstico sin planta asociada (modo invitado o rápido)
    if plant_id > 0:
        plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
        if not plant:
            raise HTTPException(404, "Planta no encontrada")
    
    # Guardar imagen
    image_data = await image.read()
    image_path = await save_image(image_data, plant_id if plant_id > 0 else user_id)
    
    logger.info(f"Imagen guardada en: {image_path}")
    
    # Obtener diagnóstico de Groq
    diagnosis_data = await get_plant_diagnosis(image_path, symptoms)
    
    # Guardar en DB
    diagnosis = DiagnosisDB(
        plant_id=plant_id if plant_id > 0 else None,
        user_id=user_id,
        image_url=image_path,
        diagnosis_text=diagnosis_data["diagnosis"],
        confidence=diagnosis_data["confidence"],
        disease_name=diagnosis_data.get("disease_name"),
        severity=diagnosis_data["severity"],
        recommendations=json.dumps(diagnosis_data["recommendations"])
    )
    db.add(diagnosis)
    db.commit()
    db.refresh(diagnosis)
    
    logger.info(f"Diagnóstico guardado con ID: {diagnosis.id}, imagen: {diagnosis.image_url}")
    
    # Actualizar health_score de la planta si existe
    if plant_id > 0:
        plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
        if plant:
            # Calcular nuevo health_score basado en severidad
            severity_scores = {"low": 80, "medium": 50, "high": 25, "critical": 10}
            new_score = severity_scores.get(diagnosis_data["severity"].lower(), 70)
            plant.health_score = int((plant.health_score + new_score) / 2)  # Promedio
            db.commit()
    
    # CU-03: Plan semanal ya viene incluido en el diagnóstico
    weekly_plan = diagnosis_data.get("weekly_plan", [])
    
    return DiagnosisResponse(
        diagnosis_id=diagnosis.id,
        diagnosis_text=diagnosis.diagnosis_text,
        disease_name=diagnosis.disease_name,
        confidence=diagnosis.confidence,
        severity=diagnosis.severity,
        recommendations=diagnosis_data["recommendations"],
        weekly_plan=weekly_plan
    )


@router.post("/capture-guidance", response_model=CaptureGuidance)
async def get_capture_guidance(image: UploadFile = File(...)):
    """
    CU-01: Captura guiada de foto - Validación de encuadre con IA
    
    Analiza una imagen capturada y proporciona retroalimentación sobre:
    - Calidad de iluminación
    - Enfoque de la imagen
    - Distancia apropiada
    - Centrado de la planta
    - Visibilidad completa
    """
    try:
        # Validar formato de imagen
        if not image.content_type.startswith('image/'):
            raise HTTPException(400, "El archivo debe ser una imagen")
        
        # Leer imagen
        image_data = await image.read()
        
        # Validar tamaño (max 10MB)
        if len(image_data) > 10 * 1024 * 1024:
            raise HTTPException(400, "La imagen es demasiado grande. Máximo 10MB")
        
        logger.info(f"Validando imagen capturada ({len(image_data)} bytes)")
        
        # Validar con IA (Groq)
        validation_result = await validate_photo_quality(image_data)
        
        # Preparar respuesta
        success = validation_result.get("success", False)
        guidance = validation_result.get("guidance", "No se pudo analizar la imagen")
        
        # Mensaje adicional basado en el resultado
        if success:
            message = "✅ ¡Excelente! Tu foto está lista para el diagnóstico"
        else:
            message = "⚠️ La foto necesita algunos ajustes"
        
        logger.info(f"Validación {'exitosa' if success else 'fallida'}: {guidance}")
        
        return CaptureGuidance(
            step="validation",
            message=message,
            success=success,
            guidance=guidance,
            audio_url=None
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error en validación de captura: {e}", exc_info=True)
        raise HTTPException(500, f"Error al validar la imagen: {str(e)}")


@router.get("/{diagnosis_id}")
async def get_diagnosis(diagnosis_id: int, request: Request, db: Session = Depends(get_db)):
    """Obtener diagnóstico por ID"""
    diagnosis = db.query(DiagnosisDB).filter(DiagnosisDB.id == diagnosis_id).first()
    if not diagnosis:
        raise HTTPException(404, "Diagnóstico no encontrado")
    
    plant = db.query(PlantDB).filter(PlantDB.id == diagnosis.plant_id).first() if diagnosis.plant_id else None
    
    return {
        "id": diagnosis.id,
        "plant_id": diagnosis.plant_id,
        "plant_name": plant.name if plant else "Sin planta asociada",
        "user_id": diagnosis.user_id,
        "diagnosis_text": diagnosis.diagnosis_text,
        "disease_name": diagnosis.disease_name,
        "confidence": diagnosis.confidence,
        "severity": diagnosis.severity,
        "image_url": get_full_image_url(diagnosis.image_url, request),
        "recommendations": json.loads(diagnosis.recommendations) if diagnosis.recommendations else [],
        "created_at": diagnosis.created_at.isoformat()
    }


@router.get("/history/{user_id}")
async def get_diagnosis_history(user_id: int, request: Request, limit: int = 20, db: Session = Depends(get_db)):
    """CU-08: Obtener historial de diagnósticos del usuario."""
    diagnoses = db.query(DiagnosisDB).filter(
        DiagnosisDB.user_id == user_id
    ).order_by(
        DiagnosisDB.created_at.desc()
    ).limit(limit).all()
    
    # Formatear respuesta con información de la planta
    result = []
    for diag in diagnoses:
        plant = db.query(PlantDB).filter(PlantDB.id == diag.plant_id).first() if diag.plant_id else None
        result.append({
            "id": diag.id,
            "plant_id": diag.plant_id,
            "plant_name": plant.name if plant else "Sin planta",
            "diagnosis_text": diag.diagnosis_text,
            "disease_name": diag.disease_name,
            "severity": diag.severity,
            "confidence": diag.confidence,
            "image_url": get_full_image_url(diag.image_url, request),
            "recommendations": json.loads(diag.recommendations) if diag.recommendations else [],
            "created_at": diag.created_at.isoformat()
        })
    
    logger.info(f"Historial obtenido: {len(result)} diagnósticos para usuario {user_id}")
    return {"diagnoses": result, "total": len(result)}


@router.get("/plant/{plant_id}/history")
async def get_diagnosis_history_by_plant(plant_id: int, request: Request, limit: int = 20, db: Session = Depends(get_db)):
    """CU-08: Obtener historial de diagnósticos de una planta específica."""
    diagnoses = db.query(DiagnosisDB).filter(
        DiagnosisDB.plant_id == plant_id
    ).order_by(
        DiagnosisDB.created_at.desc()
    ).limit(limit).all()
    
    plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
    plant_name = plant.name if plant else "Planta desconocida"
    
    result = []
    for diag in diagnoses:
        result.append({
            "id": diag.id,
            "plant_id": diag.plant_id,
            "plant_name": plant_name,
            "diagnosis_text": diag.diagnosis_text,
            "disease_name": diag.disease_name,
            "severity": diag.severity,
            "confidence": diag.confidence,
            "image_url": get_full_image_url(diag.image_url, request),
            "recommendations": json.loads(diag.recommendations) if diag.recommendations else [],
            "created_at": diag.created_at.isoformat()
        })
    
    return result


@router.post("/{diagnosis_id}/feedback")
async def submit_diagnosis_feedback(
    diagnosis_id: int,
    feedback: DiagnosisFeedbackRequest,
    user_id: int = 1,
    db: Session = Depends(get_db)
):
    """
    CU-12: Feedback/corrección del diagnóstico (Active Learning)
    
    Permite al usuario indicar si el diagnóstico fue correcto o incorrecto,
    y proporcionar información adicional para mejorar el modelo.
    """
    # Verificar que existe el diagnóstico
    diagnosis = db.query(DiagnosisDB).filter(DiagnosisDB.id == diagnosis_id).first()
    if not diagnosis:
        raise HTTPException(404, "Diagnóstico no encontrado")
    
    # Crear registro de feedback
    try:
        db_feedback = DiagnosisFeedbackDB(
            diagnosis_id=diagnosis_id,
            user_id=user_id,
            is_correct=feedback.is_correct,
            correct_diagnosis=feedback.correct_diagnosis,
            feedback_text=feedback.feedback_text
        )
        db.add(db_feedback)
        
        # Si el usuario proporcionó corrección, marcar para reentrenamiento
        if not feedback.is_correct and feedback.correct_diagnosis:
            logger.info(f"Diagnóstico {diagnosis_id} marcado para corrección: {feedback.correct_diagnosis}")
        
        db.commit()
        db.refresh(db_feedback)
        
        return {
            "message": "Feedback recibido. ¡Gracias por ayudarnos a mejorar!",
            "feedback_id": db_feedback.id,
            "is_correct": feedback.is_correct
        }
        
    except Exception as e:
        db.rollback()
        logger.error(f"Error al guardar feedback: {e}")
        raise HTTPException(500, f"Error al guardar feedback: {str(e)}")


@router.get("/{diagnosis_id}/feedback")
async def get_diagnosis_feedback(diagnosis_id: int, db: Session = Depends(get_db)):
    """Obtener feedback de un diagnóstico específico"""
    feedbacks = db.query(DiagnosisFeedbackDB).filter(
        DiagnosisFeedbackDB.diagnosis_id == diagnosis_id
    ).all()
    
    if not feedbacks:
        return {"feedbacks": [], "summary": {"correct_count": 0, "incorrect_count": 0}}
    
    correct_count = sum(1 for f in feedbacks if f.is_correct)
    incorrect_count = len(feedbacks) - correct_count
    
    return {
        "feedbacks": [
            {
                "id": f.id,
                "user_id": f.user_id,
                "is_correct": f.is_correct,
                "correct_diagnosis": f.correct_diagnosis,
                "feedback_text": f.feedback_text,
                "created_at": f.created_at.isoformat()
            }
            for f in feedbacks
        ],
        "summary": {
            "correct_count": correct_count,
            "incorrect_count": incorrect_count,
            "accuracy_rating": correct_count / len(feedbacks) if feedbacks else 0
        }
    }
