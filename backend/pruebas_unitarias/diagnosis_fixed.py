"""Rutas para diagnóstico de plantas (CU-02) - VERSIÓN CORREGIDA"""
from fastapi import APIRouter, Depends, HTTPException, File, UploadFile, Form
from sqlalchemy.orm import Session
from typing import Optional
from app.models.database import get_db, DiagnosisDB, PlantDB  # ← CORREGIDO
from app.models.schemas import DiagnosisResponse, CaptureGuidance  # ← CORREGIDO
from app.services.groq_service import get_plant_diagnosis  # ← CORREGIDO
from app.utils.image_processing import validate_image_quality, save_image  # ← CORREGIDO
import json
from datetime import datetime

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
    
    # CU-03: Generar plan semanal
    weekly_plan = generate_weekly_plan(diagnosis_data, plant)
    
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
    """CU-01: Captura guiada de foto - Validación de encuadre"""
    image_data = await image.read()
    quality = await validate_image_quality(image_data)
    
    if quality["lighting"] < 0.6:
        guidance = "Necesitas más luz. Acércate a una ventana o enciende más luces."
    elif quality["focus"] < 0.7:
        guidance = "La imagen está borrosa. Sostén el teléfono firme y enfoca bien."
    elif quality["distance"] > 0.8:
        guidance = "Estás muy lejos. Acércate más a la planta."
    else:
        guidance = "¡Perfecto! La foto tiene buena calidad."
    
    return CaptureGuidance(
        step="validation",
        message="Imagen analizada",
        success=quality["overall"] > 0.7,
        guidance=guidance
    )

@router.get("/{diagnosis_id}")
async def get_diagnosis(diagnosis_id: int, db: Session = Depends(get_db)):
    """Obtener diagnóstico por ID"""
    diagnosis = db.query(DiagnosisDB).filter(DiagnosisDB.id == diagnosis_id).first()
    if not diagnosis:
        raise HTTPException(404, "Diagnóstico no encontrado")
    return diagnosis

def generate_weekly_plan(diagnosis_data: dict, plant: PlantDB) -> list:
    """CU-03: Generar plan semanal accionable"""
    plan = []
    severity = diagnosis_data["severity"]
    
    if severity == "high" or severity == "critical":
        plan = [
            {"day": "Lunes", "task": "Aplicar tratamiento urgente", "priority": "high"},
            {"day": "Miércoles", "task": "Revisar progreso", "priority": "high"},
            {"day": "Viernes", "task": "Segunda aplicación de tratamiento", "priority": "high"},
            {"day": "Domingo", "task": "Evaluación semanal", "priority": "medium"}
        ]
    elif severity == "medium" or severity == "warning":
        plan = [
            {"day": "Lunes", "task": "Iniciar tratamiento", "priority": "medium"},
            {"day": "Jueves", "task": "Riego especial + fertilizante", "priority": "medium"},
            {"day": "Domingo", "task": "Monitoreo de síntomas", "priority": "low"}
        ]
    else:  # healthy
        plan = [
            {"day": "Miércoles", "task": "Riego regular", "priority": "low"},
            {"day": "Sábado", "task": "Inspección general", "priority": "low"}
        ]
    
    return plan
