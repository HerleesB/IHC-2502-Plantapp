"""Rutas para gestión de plantas (CU-04, CU-08, CU-16, CU-20)"""
from fastapi import APIRouter, Depends, HTTPException, Request, File, Form, UploadFile
from sqlalchemy.orm import Session
from typing import Optional
from app.models.database import get_db, PlantDB, UserDB, DiagnosisDB
from app.models.schemas import Plant, PlantCreate, ProgressStats, PlantUpdate
from datetime import datetime
import os
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/plants", tags=["Plants"])

# Directorio para imágenes de plantas
UPLOAD_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "uploads", "plants")
os.makedirs(UPLOAD_DIR, exist_ok=True)


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


@router.post("/")
async def create_plant(plant: PlantCreate, request: Request, db: Session = Depends(get_db)):
    """CU-04, CU-20: Crear nueva planta (opcionalmente desde diagnóstico)"""
    
    # Obtener imagen y health_score del diagnóstico si existe
    image_url = plant.image_url
    initial_health = 100
    
    if plant.diagnosis_id:
        diagnosis = db.query(DiagnosisDB).filter(DiagnosisDB.id == plant.diagnosis_id).first()
        if diagnosis:
            if not image_url:
                image_url = diagnosis.image_url
            # Calcular health_score basado en severidad
            severity_scores = {"low": 85, "medium": 60, "high": 35, "critical": 15}
            initial_health = severity_scores.get(diagnosis.severity.lower(), 70)
            logger.info(f"Creando planta desde diagnóstico {plant.diagnosis_id}, severidad: {diagnosis.severity}, health: {initial_health}, image: {image_url}")
    
    # Determinar status basado en health_score
    if initial_health >= 70:
        status = "healthy"
    elif initial_health >= 40:
        status = "needs_attention"
    else:
        status = "critical"
    
    db_plant = PlantDB(
        name=plant.name,
        user_id=plant.user_id,
        species=plant.species,
        description=plant.description,
        location=plant.location,
        image_url=image_url,
        status=status,
        health_score=initial_health
    )
    db.add(db_plant)
    db.commit()
    db.refresh(db_plant)
    
    # Vincular diagnóstico a la planta si existe
    if plant.diagnosis_id:
        diagnosis = db.query(DiagnosisDB).filter(DiagnosisDB.id == plant.diagnosis_id).first()
        if diagnosis:
            diagnosis.plant_id = db_plant.id
            db.commit()
            logger.info(f"Diagnóstico {plant.diagnosis_id} vinculado a planta {db_plant.id}")
    
    logger.info(f"Planta creada: id={db_plant.id}, name={db_plant.name}, health={db_plant.health_score}, image={db_plant.image_url}")
    
    return {
        "id": db_plant.id,
        "name": db_plant.name,
        "species": db_plant.species,
        "description": db_plant.description,
        "image_url": get_full_image_url(db_plant.image_url, request),
        "status": db_plant.status,
        "health_score": db_plant.health_score,
        "location": db_plant.location,
        "last_watered": db_plant.last_watered.isoformat() if db_plant.last_watered else None,
        "last_fertilized": db_plant.last_fertilized.isoformat() if db_plant.last_fertilized else None,
        "created_at": db_plant.created_at.isoformat()
    }


@router.get("/user/{user_id}")
async def get_user_plants(user_id: int, request: Request, db: Session = Depends(get_db)):
    """CU-04, CU-08, CU-16: Obtener inventario de plantas del usuario"""
    plants = db.query(PlantDB).filter(PlantDB.user_id == user_id).order_by(PlantDB.created_at.desc()).all()
    
    return [
        {
            "id": p.id,
            "name": p.name,
            "species": p.species,
            "description": p.description,
            "image_url": get_full_image_url(p.image_url, request),
            "status": p.status,
            "health_score": p.health_score,
            "location": p.location,
            "last_watered": p.last_watered.isoformat() if p.last_watered else None,
            "last_fertilized": p.last_fertilized.isoformat() if p.last_fertilized else None,
            "created_at": p.created_at.isoformat()
        }
        for p in plants
    ]


@router.get("/{plant_id}")
async def get_plant(plant_id: int, request: Request, db: Session = Depends(get_db)):
    """Obtener planta por ID"""
    plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
    if not plant:
        raise HTTPException(404, "Planta no encontrada")
    
    return {
        "id": plant.id,
        "name": plant.name,
        "species": plant.species,
        "description": plant.description,
        "image_url": get_full_image_url(plant.image_url, request),
        "status": plant.status,
        "health_score": plant.health_score,
        "location": plant.location,
        "last_watered": plant.last_watered.isoformat() if plant.last_watered else None,
        "last_fertilized": plant.last_fertilized.isoformat() if plant.last_fertilized else None,
        "created_at": plant.created_at.isoformat()
    }


@router.put("/{plant_id}")
async def update_plant(plant_id: int, update: PlantUpdate, request: Request, db: Session = Depends(get_db)):
    """CU-04: Actualizar planta"""
    plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
    if not plant:
        raise HTTPException(404, "Planta no encontrada")
    
    if update.name is not None:
        plant.name = update.name
    if update.species is not None:
        plant.species = update.species
    if update.description is not None:
        plant.description = update.description
    if update.location is not None:
        plant.location = update.location
    
    db.commit()
    db.refresh(plant)
    
    return {
        "id": plant.id,
        "name": plant.name,
        "species": plant.species,
        "description": plant.description,
        "image_url": get_full_image_url(plant.image_url, request),
        "status": plant.status,
        "health_score": plant.health_score,
        "location": plant.location,
        "last_watered": plant.last_watered.isoformat() if plant.last_watered else None,
        "last_fertilized": plant.last_fertilized.isoformat() if plant.last_fertilized else None,
        "created_at": plant.created_at.isoformat()
    }


@router.delete("/{plant_id}")
async def delete_plant(plant_id: int, user_id: int, db: Session = Depends(get_db)):
    """
    CU-04: Eliminar planta
    
    ADVERTENCIA: Esto eliminará permanentemente:
    - La planta
    - Todos sus diagnósticos
    - Todo su historial de riego
    - Todas las fotos asociadas
    - Todo el progreso de esta planta
    """
    import logging
    logger = logging.getLogger(__name__)
    
    # Buscar la planta
    plant = db.query(PlantDB).filter(
        PlantDB.id == plant_id,
        PlantDB.user_id == user_id
    ).first()
    
    if not plant:
        raise HTTPException(404, "Planta no encontrada o no tienes permiso para eliminarla")
    
    plant_name = plant.name
    
    # Contar diagnósticos asociados para información
    diagnoses_count = db.query(DiagnosisDB).filter(
        DiagnosisDB.plant_id == plant_id
    ).count()
    
    logger.info(f"🗑️ Eliminando planta '{plant_name}' (ID: {plant_id}) con {diagnoses_count} diagnósticos")
    
    # Eliminar diagnósticos asociados (por seguridad, aunque CASCADE debería hacerlo)
    db.query(DiagnosisDB).filter(DiagnosisDB.plant_id == plant_id).delete()
    
    # Eliminar la planta
    db.delete(plant)
    db.commit()
    
    logger.info(f"✅ Planta '{plant_name}' eliminada exitosamente")
    
    return {
        "success": True,
        "message": f"Planta '{plant_name}' y todo su historial eliminados exitosamente",
        "plant_name": plant_name,
        "diagnoses_deleted": diagnoses_count
    }


@router.put("/{plant_id}/water")
async def water_plant(plant_id: int, db: Session = Depends(get_db)):
    """CU-06: Registrar riego de planta"""
    plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
    if not plant:
        raise HTTPException(404, "Planta no encontrada")
    
    plant.last_watered = datetime.utcnow()
    
    # Mejorar ligeramente el health_score al regar
    if plant.health_score < 100:
        plant.health_score = min(100, plant.health_score + 5)
        # Actualizar status si mejora
        if plant.health_score >= 70:
            plant.status = "healthy"
        elif plant.health_score >= 40:
            plant.status = "needs_attention"
    
    db.commit()
    return {
        "message": "Planta regada",
        "last_watered": plant.last_watered.isoformat(),
        "health_score": plant.health_score,
        "status": plant.status
    }


@router.put("/{plant_id}/fertilize")
async def fertilize_plant(plant_id: int, db: Session = Depends(get_db)):
    """Registrar fertilización de planta"""
    plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
    if not plant:
        raise HTTPException(404, "Planta no encontrada")
    
    plant.last_fertilized = datetime.utcnow()
    
    # Mejorar el health_score al fertilizar
    if plant.health_score < 100:
        plant.health_score = min(100, plant.health_score + 10)
        if plant.health_score >= 70:
            plant.status = "healthy"
        elif plant.health_score >= 40:
            plant.status = "needs_attention"
    
    db.commit()
    return {
        "message": "Planta fertilizada",
        "last_fertilized": plant.last_fertilized.isoformat(),
        "health_score": plant.health_score,
        "status": plant.status
    }




@router.put("/{plant_id}/update-with-diagnosis")
async def update_plant_with_diagnosis(
    plant_id: int,
    image: UploadFile = File(...),
    user_id: int = Form(...),
    request: Request = None,
    db: Session = Depends(get_db)
):
    """
    Actualizar planta con nuevo diagnóstico e imagen
    
    Permite:
    - Actualizar la foto principal de la planta
    - Agregar un nuevo diagnóstico al historial
    - Actualizar el estado de salud de la planta
    - Actualizar health_score basado en el nuevo diagnóstico
    """
    import logging
    import uuid
    import os
    logger = logging.getLogger(__name__)
    
    try:
        # Buscar la planta
        plant = db.query(PlantDB).filter(
            PlantDB.id == plant_id,
            PlantDB.user_id == user_id
        ).first()
        
        if not plant:
            raise HTTPException(404, "Planta no encontrada o no tienes permiso")
        
        logger.info(f"📸 Actualizando planta '{plant.name}' (ID: {plant_id}) con nuevo diagnóstico")
        
        # Leer imagen
        image_bytes = await image.read()
        
        # Guardar imagen
        temp_filename = f"plant_{plant_id}_{uuid.uuid4()}.jpg"
        temp_path = f"./uploads/{temp_filename}"
        
        os.makedirs("./uploads", exist_ok=True)
        
        with open(temp_path, "wb") as f:
            f.write(image_bytes)
        
        image_url = f"/uploads/{temp_filename}"
        
        # Realizar diagnóstico
        from app.services.groq_service import get_plant_diagnosis
        diagnosis_data = await get_plant_diagnosis(image_bytes)
        
        if not diagnosis_data.get("success"):
            raise HTTPException(400, "Error al analizar la imagen")
        
        # Crear nuevo diagnóstico
        import json
        diagnosis_db = DiagnosisDB(
            plant_id=plant_id,
            user_id=user_id,
            image_url=image_url,
            diagnosis_text=diagnosis_data.get("diagnosis_text", ""),
            disease_name=diagnosis_data.get("disease_name", "Desconocido"),
            confidence=diagnosis_data.get("confidence", 0.0),
            severity=diagnosis_data.get("severity", "unknown"),
            recommendations=json.dumps(diagnosis_data.get("recommendations", []))
        )
        
        db.add(diagnosis_db)
        
        # Actualizar la planta con la nueva imagen y estado
        plant.image_url = image_url
        plant.status = diagnosis_data.get("severity", "unknown")
        
        # Actualizar health_score basado en severity
        severity_to_health = {
            "healthy": 100,
            "low": 80,
            "moderate": 60,
            "high": 40,
            "critical": 20,
            "unknown": 70
        }
        plant.health_score = severity_to_health.get(
            diagnosis_data.get("severity", "unknown"),
            70
        )
        
        from datetime import datetime
        plant.last_diagnosis = datetime.utcnow()
        
        db.commit()
        db.refresh(diagnosis_db)
        db.refresh(plant)
        
        logger.info(f"✅ Planta '{plant.name}' actualizada. Nuevo diagnóstico ID: {diagnosis_db.id}")
        
        # Construir URL completa para la imagen
        full_image_url = get_full_image_url(plant.image_url, request) if request else plant.image_url
        
        return {
            "success": True,
            "message": f"Planta '{plant.name}' actualizada exitosamente",
            "diagnosis": {
                "diagnosis_id": diagnosis_db.id,
                "diagnosis_text": diagnosis_data.get("diagnosis_text"),
                "disease_name": diagnosis_data.get("disease_name"),
                "confidence": diagnosis_data.get("confidence"),
                "severity": diagnosis_data.get("severity"),
                "recommendations": diagnosis_data.get("recommendations", [])
            },
            "plant": {
                "id": plant.id,
                "name": plant.name,
                "species": plant.species,
                "image_url": full_image_url,
                "status": plant.status,
                "health_score": plant.health_score,
                "last_diagnosis": plant.last_diagnosis.isoformat() if plant.last_diagnosis else None,
                "created_at": plant.created_at.isoformat() if plant.created_at else None
            }
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Error al actualizar planta: {e}")
        import traceback
        traceback.print_exc()
        db.rollback()
        raise HTTPException(500, f"Error al actualizar planta: {str(e)}")


@router.get("/user/{user_id}/progress", response_model=ProgressStats)
async def get_user_progress(user_id: int, db: Session = Depends(get_db)):
    """CU-08: Obtener progreso del usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        # Si no existe el usuario, devolver valores por defecto
        return ProgressStats(
            total_plants=0,
            healthy_plants=0,
            diagnoses_count=0,
            streak_days=0,
            level=1,
            xp=0,
            next_level_xp=100
        )
    
    plants = db.query(PlantDB).filter(PlantDB.user_id == user_id).all()
    healthy = len([p for p in plants if p.health_score >= 70])
    
    diagnoses_count = db.query(DiagnosisDB).filter(DiagnosisDB.user_id == user_id).count()
    
    return ProgressStats(
        total_plants=len(plants),
        healthy_plants=healthy,
        diagnoses_count=diagnoses_count,
        streak_days=user.streak_days,
        level=user.level,
        xp=user.xp,
        next_level_xp=user.level * 100
    )
