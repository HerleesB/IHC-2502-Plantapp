"""Rutas para diagnóstico de plantas (CU-02) - VERSIÓN CORREGIDA"""
from fastapi import APIRouter, Depends, HTTPException, File, UploadFile, Form
from sqlalchemy.orm import Session
from typing import Optional
from app.models.database import get_db, DiagnosisDB, PlantDB  # ← CORREGIDO
from app.models.schemas import DiagnosisResponse, CaptureGuidance  # ← CORREGIDO
from app.services.groq_service import get_plant_diagnosis, validate_photo_quality  # ← CORREGIDO
from app.utils.image_processing import save_image  # ← CORREGIDO
import json
from datetime import datetime
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/diagnosis", tags=["Diagnosis"])

@router.post("/analyze", response_model=DiagnosisResponse)
async def analyze_plant(
    plant_id: int = Form(...),
    image: UploadFile = File(...),
    symptoms: Optional[str] = Form(None),
    user_id: int = Form(1),  # TODO: obtener de auth
    db: Session = Depends(get_db)
):
    """CU-02: Diagnóstico automático + explicación LLM"""
    # Validar planta existe
    plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
    if not plant:
        raise HTTPException(404, "Planta no encontrada")
    
    # Guardar imagen
    image_data = await image.read()
    image_path = await save_image(image_data, plant_id)
    
    # Obtener diagnóstico de Groq
    diagnosis_data = await get_plant_diagnosis(image_path, symptoms)
    
    # Guardar en DB
    diagnosis = DiagnosisDB(
        plant_id=plant_id,
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
    
    Returns:
        CaptureGuidance con mensaje personalizado de la IA
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
        details = validation_result.get("details", {})
        
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
            audio_url=None  # TODO: Implementar TTS si se requiere
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error en validación de captura: {e}", exc_info=True)
        raise HTTPException(500, f"Error al validar la imagen: {str(e)}")

@router.get("/")
async def get_diagnosis(diagnosis_id: int, db: Session = Depends(get_db)):
    """Obtener diagnóstico por ID"""
    diagnosis = db.query(DiagnosisDB).filter(DiagnosisDB.id == diagnosis_id).first()
    if not diagnosis:
        raise HTTPException(404, "Diagnóstico no encontrado")
    return diagnosis

@router.get("/history/{user_id}")
async def get_diagnosis_history(user_id: int, limit: int = 20, db: Session = Depends(get_db)):
    """
    CU-05: Obtener historial de diagnósticos del usuario.
    
    Args:
        user_id: ID del usuario
        limit: Número máximo de diagnósticos a retornar (default: 20)
    
    Returns:
        Lista de diagnósticos ordenados por fecha descendente
    """
    diagnoses = db.query(DiagnosisDB).filter(
        DiagnosisDB.user_id == user_id
    ).order_by(
        DiagnosisDB.created_at.desc()
    ).limit(limit).all()
    
    # Formatear respuesta con información de la planta
    result = []
    for diag in diagnoses:
        plant = db.query(PlantDB).filter(PlantDB.id == diag.plant_id).first()
        result.append({
            "id": diag.id,
            "plant_id": diag.plant_id,
            "plant_name": plant.name if plant else "Desconocida",
            "diagnosis_text": diag.diagnosis_text,
            "disease_name": diag.disease_name,
            "severity": diag.severity,
            "confidence": diag.confidence,
            "image_url": diag.image_url,
            "recommendations": json.loads(diag.recommendations) if diag.recommendations else [],
            "created_at": diag.created_at.isoformat()
        })
    
    logger.info(f"Historial obtenido: {len(result)} diagnósticos para usuario {user_id}")
    return {"diagnoses": result, "total": len(result)}
