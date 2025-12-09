"""Rutas para diagnóstico de plantas (CU-01, CU-02, CU-03, CU-08, CU-12)"""
from fastapi import APIRouter, Depends, HTTPException, File, UploadFile, Form, Request
from sqlalchemy.orm import Session
from typing import Optional
from pydantic import BaseModel
from app.models.database import get_db, DiagnosisDB, PlantDB, DiagnosisFeedbackDB
from app.models.schemas import DiagnosisResponse, CaptureGuidance
from app.services.groq_service import get_plant_diagnosis, validate_photo_quality, validate_photo_quality_fast
from app.services.communication_adapter import CommunicationAdapter, UserLevel, adapt_full_diagnosis
from app.utils.image_processing import save_image
from sqlalchemy import text, func
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
    """CU-02: Diagnóstico automático + explicación LLM - ACTUALIZADO con Mejora #2"""
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
    
    # ========== MEJORA #2: Adaptar comunicación según nivel de usuario ==========
    diagnosis_count = db.query(func.count(DiagnosisDB.id)).filter(
        DiagnosisDB.user_id == user_id
    ).scalar() or 0
    
    user_level = CommunicationAdapter.detect_user_level(diagnosis_count)
    diagnosis_data = adapt_full_diagnosis(diagnosis_data, user_level)
    
    logger.info(f"Diagnóstico adaptado para nivel {user_level.value} (count: {diagnosis_count})")
    # ========== FIN DE MEJORA #2 ==========
    
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
    
    # Incrementar contador de diagnósticos del usuario
    db.execute(
        text("UPDATE users SET diagnosis_count = diagnosis_count + 1 WHERE id = :user_id"),
        {"user_id": user_id}
    )
    db.commit()
    
    logger.info(f"Diagnóstico guardado con ID: {diagnosis.id}, imagen: {diagnosis.image_url}")
    
    # Actualizar health_score e imagen de la planta si existe
    if plant_id > 0:
        plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
        if plant:
            # Calcular nuevo health_score basado en severidad
            severity_scores = {"low": 80, "medium": 50, "high": 25, "critical": 10}
            new_score = severity_scores.get(diagnosis_data["severity"].lower(), 70)
            plant.health_score = int((plant.health_score + new_score) / 2)  # Promedio
            
            # Actualizar la imagen de la planta con la nueva imagen del diagnóstico
            plant.image_url = image_path
            logger.info(f"Imagen de planta {plant_id} actualizada a: {image_path}")
            
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
        weekly_plan=weekly_plan,
        user_level=diagnosis_data.get("user_level"),
        level_badge=diagnosis_data.get("level_badge"),
        educational_tips=diagnosis_data.get("educational_tips", [])
    )


@router.post("/validate-fast")
async def validate_photo_fast(
    image: UploadFile = File(...),
    user_id: int = Form(1)
):
    """
    MEJORA #1: Endpoint de validación RÁPIDA para streaming en tiempo real.
    Optimizado para < 2 segundos de respuesta.
    """
    try:
        image_bytes = await image.read()
        result = await validate_photo_quality_fast(image_bytes)
        
        logger.info(f"Validación rápida para user {user_id}: {result['success']}")
        
        return {
            "success": result["success"],
            "guidance": result["guidance"],
            "details": result["details"]
        }
        
    except Exception as e:
        logger.error(f"Error en validación rápida: {e}")
        return {
            "success": False,
            "guidance": "Error al validar foto. Intenta de nuevo.",
            "details": {
                "lighting": 0.0,
                "focus": 0.0,
                "distance": 0.0,
                "overall": 0.0
            }
        }


@router.get("/communication-profile/{user_id}")
async def get_communication_profile(
    user_id: int,
    db: Session = Depends(get_db)
):
    """
    MEJORA #2: Obtener perfil de comunicación del usuario.
    """
    diagnosis_count = db.query(func.count(DiagnosisDB.id)).filter(
        DiagnosisDB.user_id == user_id
    ).scalar() or 0
    
    user_level = CommunicationAdapter.detect_user_level(diagnosis_count)
    
    if user_level == UserLevel.BEGINNER:
        next_level = "Intermedio"
        diagnoses_needed = 3 - diagnosis_count
    elif user_level == UserLevel.INTERMEDIATE:
        next_level = "Experto"
        diagnoses_needed = 11 - diagnosis_count
    else:
        next_level = "Experto (ya alcanzado)"
        diagnoses_needed = 0
    
    descriptions = {
        UserLevel.BEGINNER: "Estás comenzando tu viaje en jardinería. Los términos se simplifican para ti.",
        UserLevel.INTERMEDIATE: "Ya tienes experiencia. Recibes términos técnicos con explicaciones.",
        UserLevel.EXPERT: "Eres un experto jardinero. Recibes información técnica completa."
    }
    
    return {
        "user_id": user_id,
        "current_level": user_level.value,
        "level_badge": CommunicationAdapter.get_level_badge(user_level),
        "diagnosis_count": diagnosis_count,
        "next_level": next_level,
        "diagnoses_needed_for_next": max(0, diagnoses_needed),
        "level_description": descriptions.get(user_level, "")
    }


@router.post("/capture-guidance")
async def get_capture_guidance(
    image: UploadFile = File(...),
    user_id: int = Form(1),
    db: Session = Depends(get_db)
):
    """
    Validar foto + Hacer diagnóstico completo
    
    MEJORA: Ahora hace DOS cosas en una sola llamada:
    1. Validación de calidad de foto (siempre)
    2. Diagnóstico completo (si calidad >= 40%)
    
    Retorna:
    - guidance: Recomendaciones para mejorar la foto
    - diagnosis: Diagnóstico de la planta (si calidad >= 40%)
    - quality_score: Puntaje de calidad de la foto (0-100)
    """
    try:
        # Leer imagen
        image_bytes = await image.read()
        logger.info(f"📸 Validando imagen capturada ({len(image_bytes)} bytes)")
        
        # 1. VALIDAR CALIDAD DE LA FOTO (SIEMPRE)
        validation_result = await validate_photo_quality(image_bytes)
        quality_score = validation_result.get("overall_quality", 0) * 100
        
        logger.info(f"📊 Calidad de foto: {quality_score:.1f}%")
        
        # 2. SI LA FOTO ES ACEPTABLE (>=40%), HACER DIAGNÓSTICO COMPLETO
        diagnosis_result = None
        if quality_score >= 40:
            logger.info(f"✅ Calidad suficiente ({quality_score:.1f}%), realizando diagnóstico completo...")
            
            # Guardar imagen temporalmente
            import uuid
            import os
            temp_filename = f"temp_{uuid.uuid4()}.jpg"
            temp_path = f"./uploads/{temp_filename}"
            
            os.makedirs("./uploads", exist_ok=True)
            
            with open(temp_path, "wb") as f:
                f.write(image_bytes)
            
            # Realizar diagnóstico completo
            diagnosis_data = await get_plant_diagnosis(image_bytes)
            
            if diagnosis_data.get("success"):
                # Guardar en base de datos
                diagnosis_db = DiagnosisDB(
                    plant_id=None,
                    user_id=user_id,
                    image_url=f"/uploads/{temp_filename}",
                    diagnosis_text=diagnosis_data.get("diagnosis_text", ""),
                    disease_name=diagnosis_data.get("disease_name", "Desconocido"),
                    confidence=diagnosis_data.get("confidence", 0.0),
                    severity=diagnosis_data.get("severity", "unknown"),
                    recommendations=json.dumps(diagnosis_data.get("recommendations", []))
                )
                
                db.add(diagnosis_db)
                db.commit()
                db.refresh(diagnosis_db)
                
                diagnosis_result = {
                    "diagnosis_id": diagnosis_db.id,
                    "diagnosis_text": diagnosis_data.get("diagnosis_text"),
                    "disease_name": diagnosis_data.get("disease_name"),
                    "confidence": diagnosis_data.get("confidence"),
                    "severity": diagnosis_data.get("severity"),
                    "recommendations": diagnosis_data.get("recommendations", [])
                }
                
                logger.info(f"✅ Diagnóstico completado: {diagnosis_db.disease_name} (ID: {diagnosis_db.id})")
        else:
            logger.info(f"⚠️ Calidad insuficiente ({quality_score:.1f}%), solo validación")
        
        # 3. PREPARAR RESPUESTA CON VALIDACIÓN + DIAGNÓSTICO (si aplica)
        response = {
            "success": True,
            "quality_score": round(quality_score, 1),
            "validation": {
                "lighting": round(validation_result.get("lighting", 0) * 100, 1),
                "focus": round(validation_result.get("focus", 0) * 100, 1),
                "distance": round(validation_result.get("distance", 0) * 100, 1),
                "angle": round(validation_result.get("angle", 0) * 100, 1)
            },
            "guidance": validation_result.get("guidance", "Foto aceptable"),
            "details": validation_result.get("details", {})
        }
        
        # Agregar diagnóstico si existe
        if diagnosis_result:
            response["diagnosis"] = diagnosis_result
            response["has_diagnosis"] = True
            response["message"] = "✅ Diagnóstico completado"
        else:
            response["has_diagnosis"] = False
            response["message"] = "⚠️ Mejora la calidad de la foto para obtener un diagnóstico preciso (mínimo 40%)"
        
        logger.info(f"✅ Respuesta completa: validación {'+ diagnóstico' if diagnosis_result else 'sin diagnóstico'}")
        
        return response
        
    except Exception as e:
        logger.error(f"❌ Error en validación de foto: {e}")
        import traceback
        traceback.print_exc()
        
        return {
            "success": False,
            "quality_score": 0,
            "has_diagnosis": False,
            "guidance": "Error al analizar la foto. Por favor intenta de nuevo.",
            "message": f"Error: {str(e)}"
        }


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
    """CU-12: Feedback/corrección del diagnóstico (Active Learning)
    
    - Solo se permite UN feedback por usuario por diagnóstico
    - Si ya existe feedback, se actualiza en lugar de crear uno nuevo
    """
    diagnosis = db.query(DiagnosisDB).filter(DiagnosisDB.id == diagnosis_id).first()
    if not diagnosis:
        raise HTTPException(404, "Diagnóstico no encontrado")
    
    try:
        # Verificar si ya existe feedback de este usuario para este diagnóstico
        existing_feedback = db.query(DiagnosisFeedbackDB).filter(
            DiagnosisFeedbackDB.diagnosis_id == diagnosis_id,
            DiagnosisFeedbackDB.user_id == user_id
        ).first()
        
        if existing_feedback:
            # Actualizar feedback existente
            existing_feedback.is_correct = feedback.is_correct
            existing_feedback.correct_diagnosis = feedback.correct_diagnosis
            existing_feedback.feedback_text = feedback.feedback_text
            existing_feedback.created_at = datetime.utcnow()  # Actualizar timestamp
            
            db.commit()
            db.refresh(existing_feedback)
            
            logger.info(f"Feedback actualizado para diagnóstico {diagnosis_id} por usuario {user_id}")
            
            return {
                "success": True,
                "message": "Feedback actualizado. ¡Gracias por tu corrección!",
                "feedback_id": existing_feedback.id,
                "is_correct": feedback.is_correct,
                "updated": True
            }
        else:
            # Crear nuevo feedback
            db_feedback = DiagnosisFeedbackDB(
                diagnosis_id=diagnosis_id,
                user_id=user_id,
                is_correct=feedback.is_correct,
                correct_diagnosis=feedback.correct_diagnosis,
                feedback_text=feedback.feedback_text
            )
            db.add(db_feedback)
            
            if not feedback.is_correct and feedback.correct_diagnosis:
                logger.info(f"Diagnóstico {diagnosis_id} marcado para corrección: {feedback.correct_diagnosis}")
            
            db.commit()
            db.refresh(db_feedback)
            
            logger.info(f"Nuevo feedback creado para diagnóstico {diagnosis_id} por usuario {user_id}")
            
            return {
                "success": True,
                "message": "Feedback recibido. ¡Gracias por ayudarnos a mejorar!",
                "feedback_id": db_feedback.id,
                "is_correct": feedback.is_correct,
                "updated": False
            }
        
    except Exception as e:
        db.rollback()
        logger.error(f"Error al guardar feedback: {e}")
        raise HTTPException(500, f"Error al guardar feedback: {str(e)}")


@router.get("/{diagnosis_id}/feedback/user/{user_id}")
async def get_user_feedback_for_diagnosis(
    diagnosis_id: int,
    user_id: int,
    db: Session = Depends(get_db)
):
    """Verificar si un usuario ya envió feedback para un diagnóstico específico"""
    feedback = db.query(DiagnosisFeedbackDB).filter(
        DiagnosisFeedbackDB.diagnosis_id == diagnosis_id,
        DiagnosisFeedbackDB.user_id == user_id
    ).first()
    
    if feedback:
        return {
            "has_feedback": True,
            "feedback": {
                "id": feedback.id,
                "is_correct": feedback.is_correct,
                "correct_diagnosis": feedback.correct_diagnosis,
                "feedback_text": feedback.feedback_text,
                "created_at": feedback.created_at.isoformat()
            }
        }
    else:
        return {
            "has_feedback": False,
            "feedback": None
        }


@router.get("/{diagnosis_id}/feedback")
async def get_diagnosis_feedback(diagnosis_id: int, db: Session = Depends(get_db)):
    """Obtener todos los feedbacks de un diagnóstico específico"""
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
