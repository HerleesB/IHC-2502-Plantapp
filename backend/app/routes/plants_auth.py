"""Rutas para gestión de plantas (CU-04, CU-08) - CON AUTENTICACIÓN"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.models.database import get_db, PlantDB, UserDB
from app.models.schemas import Plant, PlantCreate, ProgressStats, PlantBase
from app.utils.auth import get_current_user
from datetime import datetime

router = APIRouter(prefix="/api/plants", tags=["Plants"])


@router.post("/", response_model=Plant)
async def create_plant(
    plant: PlantBase,
    db: Session = Depends(get_db),
    current_user: UserDB = Depends(get_current_user)
):
    """
    CU-04: Crear nueva planta (REQUIERE AUTENTICACIÓN)
    La planta se asocia automáticamente al usuario autenticado.
    """
    db_plant = PlantDB(
        **plant.dict(),
        user_id=current_user.id  # Usuario desde JWT
    )
    db.add(db_plant)
    db.commit()
    db.refresh(db_plant)
    return db_plant


@router.get("/", response_model=list[Plant])
async def get_my_plants(
    db: Session = Depends(get_db),
    current_user: UserDB = Depends(get_current_user)
):
    """
    CU-04, CU-08: Obtener inventario de plantas del usuario autenticado
    Reemplaza el endpoint anterior que requería user_id en la URL
    """
    plants = db.query(PlantDB).filter(PlantDB.user_id == current_user.id).all()
    return plants


@router.get("/{plant_id}", response_model=Plant)
async def get_plant(
    plant_id: int,
    db: Session = Depends(get_db),
    current_user: UserDB = Depends(get_current_user)
):
    """
    Obtener planta por ID (solo si pertenece al usuario autenticado)
    """
    plant = db.query(PlantDB).filter(
        PlantDB.id == plant_id,
        PlantDB.user_id == current_user.id
    ).first()
    
    if not plant:
        raise HTTPException(404, "Planta no encontrada o no tienes permisos")
    
    return plant


@router.put("/{plant_id}", response_model=Plant)
async def update_plant(
    plant_id: int,
    plant_data: PlantBase,
    db: Session = Depends(get_db),
    current_user: UserDB = Depends(get_current_user)
):
    """
    Actualizar información de una planta
    """
    plant = db.query(PlantDB).filter(
        PlantDB.id == plant_id,
        PlantDB.user_id == current_user.id
    ).first()
    
    if not plant:
        raise HTTPException(404, "Planta no encontrada o no tienes permisos")
    
    for key, value in plant_data.dict(exclude_unset=True).items():
        setattr(plant, key, value)
    
    db.commit()
    db.refresh(plant)
    return plant


@router.delete("/{plant_id}")
async def delete_plant(
    plant_id: int,
    db: Session = Depends(get_db),
    current_user: UserDB = Depends(get_current_user)
):
    """
    Eliminar una planta
    """
    plant = db.query(PlantDB).filter(
        PlantDB.id == plant_id,
        PlantDB.user_id == current_user.id
    ).first()
    
    if not plant:
        raise HTTPException(404, "Planta no encontrada o no tienes permisos")
    
    db.delete(plant)
    db.commit()
    return {"message": "Planta eliminada exitosamente", "plant_id": plant_id}


@router.put("/{plant_id}/water")
async def water_plant(
    plant_id: int,
    db: Session = Depends(get_db),
    current_user: UserDB = Depends(get_current_user)
):
    """
    Registrar riego de planta (solo si pertenece al usuario)
    """
    plant = db.query(PlantDB).filter(
        PlantDB.id == plant_id,
        PlantDB.user_id == current_user.id
    ).first()
    
    if not plant:
        raise HTTPException(404, "Planta no encontrada o no tienes permisos")
    
    plant.last_watered = datetime.utcnow()
    db.commit()
    return {"message": "Planta regada", "last_watered": plant.last_watered}


@router.put("/{plant_id}/fertilize")
async def fertilize_plant(
    plant_id: int,
    db: Session = Depends(get_db),
    current_user: UserDB = Depends(get_current_user)
):
    """
    Registrar fertilización de planta
    """
    plant = db.query(PlantDB).filter(
        PlantDB.id == plant_id,
        PlantDB.user_id == current_user.id
    ).first()
    
    if not plant:
        raise HTTPException(404, "Planta no encontrada o no tienes permisos")
    
    plant.last_fertilized = datetime.utcnow()
    db.commit()
    return {"message": "Planta fertilizada", "last_fertilized": plant.last_fertilized}


@router.get("/me/progress", response_model=ProgressStats)
async def get_my_progress(
    db: Session = Depends(get_db),
    current_user: UserDB = Depends(get_current_user)
):
    """
    CU-08: Obtener progreso del usuario autenticado
    """
    plants = db.query(PlantDB).filter(PlantDB.user_id == current_user.id).all()
    healthy = len([p for p in plants if p.status == "healthy"])
    
    return ProgressStats(
        total_plants=len(plants),
        healthy_plants=healthy,
        diagnoses_count=len(current_user.diagnoses),
        streak_days=current_user.streak_days,
        level=current_user.level,
        xp=current_user.xp,
        next_level_xp=current_user.level * 100
    )


# ========== ENDPOINTS LEGACY (MANTENER COMPATIBILIDAD TEMPORAL) ==========

@router.get("/user/{user_id}", response_model=list[Plant])
async def get_user_plants_legacy(user_id: int, db: Session = Depends(get_db)):
    """
    [LEGACY - DEPRECADO] Obtener plantas por user_id en URL
    Usar /api/plants/ con autenticación JWT en su lugar
    """
    plants = db.query(PlantDB).filter(PlantDB.user_id == user_id).all()
    return plants


@router.get("/user/{user_id}/progress", response_model=ProgressStats)
async def get_user_progress_legacy(user_id: int, db: Session = Depends(get_db)):
    """
    [LEGACY - DEPRECADO] Obtener progreso por user_id en URL
    Usar /api/plants/me/progress con autenticación JWT en su lugar
    """
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        raise HTTPException(404, "Usuario no encontrado")
    
    plants = db.query(PlantDB).filter(PlantDB.user_id == user_id).all()
    healthy = len([p for p in plants if p.status == "healthy"])
    
    return ProgressStats(
        total_plants=len(plants),
        healthy_plants=healthy,
        diagnoses_count=len(user.diagnoses),
        streak_days=user.streak_days,
        level=user.level,
        xp=user.xp,
        next_level_xp=user.level * 100
    )
