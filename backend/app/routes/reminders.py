"""Rutas para recordatorios (CU-06)"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import Optional
from datetime import datetime
from app.models.database import get_db, ReminderDB, PlantDB, UserDB

router = APIRouter(prefix="/api/reminders", tags=["Reminders"])


class ReminderCreate(BaseModel):
    plant_id: int
    user_id: int
    reminder_type: str  # "water", "fertilize", "diagnose", "check"
    message: str
    scheduled_time: datetime


class ReminderResponse(BaseModel):
    id: int
    plant_id: int
    plant_name: str
    reminder_type: str
    message: str
    scheduled_time: datetime
    completed: bool
    created_at: datetime


@router.post("/")
async def create_reminder(reminder: ReminderCreate, db: Session = Depends(get_db)):
    """CU-06: Crear recordatorio de cuidado"""
    # Verificar planta existe
    plant = db.query(PlantDB).filter(PlantDB.id == reminder.plant_id).first()
    if not plant:
        raise HTTPException(404, "Planta no encontrada")
    
    db_reminder = ReminderDB(
        plant_id=reminder.plant_id,
        user_id=reminder.user_id,
        reminder_type=reminder.reminder_type,
        message=reminder.message,
        scheduled_time=reminder.scheduled_time
    )
    db.add(db_reminder)
    db.commit()
    db.refresh(db_reminder)
    
    return {
        "id": db_reminder.id,
        "plant_id": db_reminder.plant_id,
        "plant_name": plant.name,
        "reminder_type": db_reminder.reminder_type,
        "message": db_reminder.message,
        "scheduled_time": db_reminder.scheduled_time.isoformat(),
        "completed": db_reminder.completed,
        "created_at": db_reminder.created_at.isoformat()
    }


@router.get("/user/{user_id}")
async def get_user_reminders(user_id: int, include_completed: bool = False, db: Session = Depends(get_db)):
    """Obtener recordatorios del usuario"""
    query = db.query(ReminderDB).filter(ReminderDB.user_id == user_id)
    
    if not include_completed:
        query = query.filter(ReminderDB.completed == False)
    
    reminders = query.order_by(ReminderDB.scheduled_time).all()
    
    result = []
    for r in reminders:
        plant = db.query(PlantDB).filter(PlantDB.id == r.plant_id).first()
        result.append({
            "id": r.id,
            "plant_id": r.plant_id,
            "plant_name": plant.name if plant else "Planta eliminada",
            "reminder_type": r.reminder_type,
            "message": r.message,
            "scheduled_time": r.scheduled_time.isoformat(),
            "completed": r.completed,
            "created_at": r.created_at.isoformat()
        })
    
    return result


@router.get("/user/{user_id}/pending")
async def get_pending_reminders(user_id: int, db: Session = Depends(get_db)):
    """Obtener recordatorios pendientes (no completados y vencidos)"""
    now = datetime.utcnow()
    
    reminders = db.query(ReminderDB).filter(
        ReminderDB.user_id == user_id,
        ReminderDB.completed == False,
        ReminderDB.scheduled_time <= now
    ).order_by(ReminderDB.scheduled_time).all()
    
    result = []
    for r in reminders:
        plant = db.query(PlantDB).filter(PlantDB.id == r.plant_id).first()
        result.append({
            "id": r.id,
            "plant_id": r.plant_id,
            "plant_name": plant.name if plant else "Planta eliminada",
            "reminder_type": r.reminder_type,
            "message": r.message,
            "scheduled_time": r.scheduled_time.isoformat(),
            "is_overdue": True
        })
    
    return result


@router.put("/{reminder_id}/complete")
async def complete_reminder(reminder_id: int, db: Session = Depends(get_db)):
    """CU-06: Marcar recordatorio como completado"""
    reminder = db.query(ReminderDB).filter(ReminderDB.id == reminder_id).first()
    if not reminder:
        raise HTTPException(404, "Recordatorio no encontrado")
    
    reminder.completed = True
    reminder.completed_at = datetime.utcnow()
    db.commit()
    
    return {"message": "Recordatorio completado", "reminder_id": reminder_id}


@router.delete("/{reminder_id}")
async def delete_reminder(reminder_id: int, db: Session = Depends(get_db)):
    """Eliminar recordatorio"""
    reminder = db.query(ReminderDB).filter(ReminderDB.id == reminder_id).first()
    if not reminder:
        raise HTTPException(404, "Recordatorio no encontrado")
    
    db.delete(reminder)
    db.commit()
    
    return {"message": "Recordatorio eliminado", "reminder_id": reminder_id}


@router.post("/plant/{plant_id}/auto")
async def create_auto_reminders(plant_id: int, user_id: int, db: Session = Depends(get_db)):
    """Crear recordatorios automáticos basados en el tipo de planta"""
    from datetime import timedelta
    
    plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
    if not plant:
        raise HTTPException(404, "Planta no encontrada")
    
    now = datetime.utcnow()
    reminders_created = []
    
    # Recordatorio de riego (cada 3 días)
    water_reminder = ReminderDB(
        plant_id=plant_id,
        user_id=user_id,
        reminder_type="water",
        message=f"Es hora de regar {plant.name}",
        scheduled_time=now + timedelta(days=3)
    )
    db.add(water_reminder)
    reminders_created.append("water")
    
    # Recordatorio de fertilización (cada 2 semanas)
    fertilize_reminder = ReminderDB(
        plant_id=plant_id,
        user_id=user_id,
        reminder_type="fertilize",
        message=f"Considera fertilizar {plant.name}",
        scheduled_time=now + timedelta(weeks=2)
    )
    db.add(fertilize_reminder)
    reminders_created.append("fertilize")
    
    # Recordatorio de chequeo (cada semana)
    check_reminder = ReminderDB(
        plant_id=plant_id,
        user_id=user_id,
        reminder_type="check",
        message=f"Revisa el estado de {plant.name}",
        scheduled_time=now + timedelta(weeks=1)
    )
    db.add(check_reminder)
    reminders_created.append("check")
    
    db.commit()
    
    return {
        "message": f"Recordatorios automáticos creados para {plant.name}",
        "reminders_created": reminders_created
    }
