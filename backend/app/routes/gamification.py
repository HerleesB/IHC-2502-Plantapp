"""Rutas para gamificación (CU-06, CU-08)"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.models.database import get_db, UserDB, AchievementDB
from typing import List, Dict

router = APIRouter(prefix="/api/gamification", tags=["Gamification"])

# Logros predefinidos
ACHIEVEMENTS = [
    {"id": 1, "name": "Primera Planta", "description": "Agrega tu primera planta", "icon": "🌱", "points": 10, "requirement": "plants_created", "threshold": 1},
    {"id": 2, "name": "Jardinero Novato", "description": "Cuida 5 plantas", "icon": "🌿", "points": 25, "requirement": "plants_created", "threshold": 5},
    {"id": 3, "name": "Experto Verde", "description": "Cuida 10 plantas", "icon": "🌳", "points": 50, "requirement": "plants_created", "threshold": 10},
    {"id": 4, "name": "Doctor de Plantas", "description": "Realiza 10 diagnósticos", "icon": "🔬", "points": 30, "requirement": "diagnoses_count", "threshold": 10},
    {"id": 5, "name": "Racha de Campeón", "description": "Mantén una racha de 7 días", "icon": "🔥", "points": 40, "requirement": "streak_days", "threshold": 7},
    {"id": 6, "name": "Racha Legendaria", "description": "Mantén una racha de 30 días", "icon": "⚡", "points": 100, "requirement": "streak_days", "threshold": 30},
    {"id": 7, "name": "Miembro de la Comunidad", "description": "Publica tu primer caso", "icon": "💬", "points": 15, "requirement": "posts_created", "threshold": 1},
    {"id": 8, "name": "Maestro Jardinero", "description": "Alcanza nivel 10", "icon": "👑", "points": 200, "requirement": "level", "threshold": 10},
]

MISSIONS = [
    {"id": 1, "title": "Regar tus plantas", "description": "Riega al menos 3 plantas hoy", "xp": 20, "type": "daily"},
    {"id": 2, "title": "Revisar tu jardín", "description": "Revisa el estado de todas tus plantas", "xp": 15, "type": "daily"},
    {"id": 3, "title": "Diagnosticar una planta", "description": "Realiza un diagnóstico con IA", "xp": 30, "type": "daily"},
    {"id": 4, "title": "Compartir en comunidad", "description": "Publica un caso en la comunidad", "xp": 25, "type": "weekly"},
    {"id": 5, "title": "Ayudar a la comunidad", "description": "Comenta en 3 posts de la comunidad", "xp": 35, "type": "weekly"},
]

@router.get("/achievements/{user_id}")
async def get_user_achievements(user_id: int, db: Session = Depends(get_db)):
    """CU-06: Obtener logros del usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        raise HTTPException(404, "Usuario no encontrado")
    
    # Obtener estadísticas del usuario
    stats = {
        "plants_created": len(user.plants),
        "diagnoses_count": len(user.diagnoses),
        "streak_days": user.streak_days,
        "level": user.level,
        "posts_created": 0  # TODO: Implementar cuando se agregue relación
    }
    
    # Verificar logros desbloqueados
    unlocked_achievements = []
    locked_achievements = []
    
    for achievement in ACHIEVEMENTS:
        requirement = achievement["requirement"]
        threshold = achievement["threshold"]
        
        is_unlocked = stats.get(requirement, 0) >= threshold
        
        achievement_data = {
            **achievement,
            "unlocked": is_unlocked,
            "progress": min(stats.get(requirement, 0), threshold),
            "progress_max": threshold
        }
        
        if is_unlocked:
            unlocked_achievements.append(achievement_data)
        else:
            locked_achievements.append(achievement_data)
    
    return {
        "unlocked": unlocked_achievements,
        "locked": locked_achievements,
        "total_points": user.points,
        "level": user.level,
        "xp": user.xp,
        "next_level_xp": user.level * 100
    }

@router.get("/missions/{user_id}")
async def get_user_missions(user_id: int, db: Session = Depends(get_db)):
    """CU-06: Obtener misiones del usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        raise HTTPException(404, "Usuario no encontrado")
    
    # TODO: Implementar progreso real de misiones
    missions_with_progress = []
    for mission in MISSIONS:
        missions_with_progress.append({
            **mission,
            "progress": 0,
            "completed": False
        })
    
    return {
        "daily": [m for m in missions_with_progress if m["type"] == "daily"],
        "weekly": [m for m in missions_with_progress if m["type"] == "weekly"],
        "streak_days": user.streak_days,
        "level": user.level,
        "xp": user.xp,
        "next_level_xp": user.level * 100
    }

@router.post("/award-xp/{user_id}")
async def award_xp(user_id: int, xp: int, db: Session = Depends(get_db)):
    """Otorgar XP al usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        raise HTTPException(404, "Usuario no encontrado")
    
    user.xp += xp
    
    # Subir de nivel si es necesario
    xp_for_next_level = user.level * 100
    while user.xp >= xp_for_next_level:
        user.xp -= xp_for_next_level
        user.level += 1
        xp_for_next_level = user.level * 100
    
    user.points += xp
    
    db.commit()
    db.refresh(user)
    
    return {
        "level": user.level,
        "xp": user.xp,
        "next_level_xp": user.level * 100,
        "total_points": user.points
    }

@router.post("/update-streak/{user_id}")
async def update_streak(user_id: int, db: Session = Depends(get_db)):
    """Actualizar racha del usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        raise HTTPException(404, "Usuario no encontrado")
    
    # TODO: Implementar lógica de racha basada en última actividad
    user.streak_days += 1
    
    db.commit()
    db.refresh(user)
    
    return {
        "streak_days": user.streak_days,
        "message": f"¡Racha de {user.streak_days} días! 🔥"
    }
