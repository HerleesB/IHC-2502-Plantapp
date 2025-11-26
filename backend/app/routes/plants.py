"""Rutas para gestión de plantas (CU-04, CU-08)"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.models.database import get_db, PlantDB, UserDB
from app.models.schemas import Plant, PlantCreate, ProgressStats
from datetime import datetime

router = APIRouter(prefix="/api/plants", tags=["Plants"])

@router.post("/", response_model=Plant)
async def create_plant(plant: PlantCreate, db: Session = Depends(get_db)):
    """CU-04: Crear nueva planta"""
    db_plant = PlantDB(**plant.dict())
    db.add(db_plant)
    db.commit()
    db.refresh(db_plant)
    return db_plant

@router.get("/user/{user_id}", response_model=list[Plant])
async def get_user_plants(user_id: int, db: Session = Depends(get_db)):
    """CU-04, CU-08: Obtener inventario de plantas del usuario"""
    plants = db.query(PlantDB).filter(PlantDB.user_id == user_id).all()
    return plants

@router.get("/{plant_id}", response_model=Plant)
async def get_plant(plant_id: int, db: Session = Depends(get_db)):
    """Obtener planta por ID"""
    plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
    if not plant:
        raise HTTPException(404, "Planta no encontrada")
    return plant

@router.put("/{plant_id}/water")
async def water_plant(plant_id: int, db: Session = Depends(get_db)):
    """Registrar riego de planta"""
    plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
    if not plant:
        raise HTTPException(404, "Planta no encontrada")
    plant.last_watered = datetime.utcnow()
    db.commit()
    return {"message": "Planta regada", "last_watered": plant.last_watered}

@router.get("/user/{user_id}/progress", response_model=ProgressStats)
async def get_user_progress(user_id: int, db: Session = Depends(get_db)):
    """CU-08: Obtener progreso del usuario"""
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
