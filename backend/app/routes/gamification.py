"""Rutas para gamificación (CU-06, CU-17)"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.models.database import get_db, UserDB, PlantDB, DiagnosisDB, CommunityPostDB
from datetime import datetime, timedelta

router = APIRouter(prefix="/api/gamification", tags=["Gamification"])

# Logros predefinidos
ACHIEVEMENTS = [
    {"id": 1, "name": "Primera Planta", "description": "Agrega tu primera planta", "icon": "🌱", "points": 10, "requirement": "plants_created", "threshold": 1},
    {"id": 2, "name": "Jardinero Novato", "description": "Cuida 5 plantas", "icon": "🌿", "points": 25, "requirement": "plants_created", "threshold": 5},
    {"id": 3, "name": "Experto Verde", "description": "Cuida 10 plantas", "icon": "🌳", "points": 50, "requirement": "plants_created", "threshold": 10},
    {"id": 4, "name": "Coleccionista", "description": "Cuida 25 plantas", "icon": "🏡", "points": 100, "requirement": "plants_created", "threshold": 25},
    {"id": 5, "name": "Doctor de Plantas", "description": "Realiza 10 diagnósticos", "icon": "🔬", "points": 30, "requirement": "diagnoses_count", "threshold": 10},
    {"id": 6, "name": "Diagnóstico Experto", "description": "Realiza 50 diagnósticos", "icon": "🩺", "points": 75, "requirement": "diagnoses_count", "threshold": 50},
    {"id": 7, "name": "Racha de Campeón", "description": "Mantén una racha de 7 días", "icon": "🔥", "points": 40, "requirement": "streak_days", "threshold": 7},
    {"id": 8, "name": "Racha Legendaria", "description": "Mantén una racha de 30 días", "icon": "⚡", "points": 100, "requirement": "streak_days", "threshold": 30},
    {"id": 9, "name": "Miembro de la Comunidad", "description": "Publica tu primer caso", "icon": "💬", "points": 15, "requirement": "posts_created", "threshold": 1},
    {"id": 10, "name": "Colaborador Activo", "description": "Publica 10 casos", "icon": "🤝", "points": 50, "requirement": "posts_created", "threshold": 10},
    {"id": 11, "name": "Plantas Saludables", "description": "Mantén 5 plantas con salud >80%", "icon": "💚", "points": 35, "requirement": "healthy_plants", "threshold": 5},
    {"id": 12, "name": "Maestro Jardinero", "description": "Alcanza nivel 10", "icon": "👑", "points": 200, "requirement": "level", "threshold": 10},
]

MISSIONS = [
    {"id": 1, "title": "Regar tus plantas", "description": "Riega al menos una planta hoy", "xp": 15, "type": "daily", "action": "water", "target": 1},
    {"id": 2, "title": "Revisar tu jardín", "description": "Abre la sección Mi Jardín", "xp": 10, "type": "daily", "action": "view_garden", "target": 1},
    {"id": 3, "title": "Diagnosticar una planta", "description": "Realiza un diagnóstico con IA", "xp": 25, "type": "daily", "action": "diagnose", "target": 1},
    {"id": 4, "title": "Regar todas tus plantas", "description": "Riega al menos 3 plantas hoy", "xp": 30, "type": "daily", "action": "water", "target": 3},
    {"id": 5, "title": "Compartir en comunidad", "description": "Publica un caso en la comunidad", "xp": 35, "type": "weekly", "action": "post", "target": 1},
    {"id": 6, "title": "Ayudar a la comunidad", "description": "Comenta en 3 posts de la comunidad", "xp": 40, "type": "weekly", "action": "comment", "target": 3},
    {"id": 7, "title": "Agregar nueva planta", "description": "Agrega una planta a tu jardín", "xp": 20, "type": "weekly", "action": "add_plant", "target": 1},
]


@router.get("/achievements/{user_id}")
async def get_user_achievements(user_id: int, db: Session = Depends(get_db)):
    """CU-06, CU-17: Obtener logros del usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    
    # Si no existe el usuario, devolver logros vacíos
    if not user:
        return {
            "unlocked": [],
            "locked": ACHIEVEMENTS,
            "total_points": 0,
            "level": 1,
            "xp": 0,
            "next_level_xp": 100
        }
    
    # Obtener estadísticas del usuario
    plants = db.query(PlantDB).filter(PlantDB.user_id == user_id).all()
    diagnoses_count = db.query(DiagnosisDB).filter(DiagnosisDB.user_id == user_id).count()
    posts_count = db.query(CommunityPostDB).filter(CommunityPostDB.user_id == user_id).count()
    healthy_plants = len([p for p in plants if p.health_score >= 80])
    
    stats = {
        "plants_created": len(plants),
        "diagnoses_count": diagnoses_count,
        "streak_days": user.streak_days,
        "level": user.level,
        "posts_created": posts_count,
        "healthy_plants": healthy_plants
    }
    
    # Verificar logros desbloqueados
    unlocked_achievements = []
    locked_achievements = []
    
    for achievement in ACHIEVEMENTS:
        requirement = achievement["requirement"]
        threshold = achievement["threshold"]
        current_progress = stats.get(requirement, 0)
        
        is_unlocked = current_progress >= threshold
        
        achievement_data = {
            **achievement,
            "unlocked": is_unlocked,
            "progress": min(current_progress, threshold),
            "progress_max": threshold,
            "unlocked_at": None  # TODO: Implementar fecha de desbloqueo
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
        return {
            "daily": [{"id": m["id"], **m, "progress": 0, "completed": False} for m in MISSIONS if m["type"] == "daily"],
            "weekly": [{"id": m["id"], **m, "progress": 0, "completed": False} for m in MISSIONS if m["type"] == "weekly"],
            "streak_days": 0,
            "level": 1,
            "xp": 0,
            "next_level_xp": 100
        }
    
    # Calcular progreso real de misiones
    today = datetime.utcnow().date()
    week_start = today - timedelta(days=today.weekday())
    
    # Estadísticas de hoy
    plants_watered_today = db.query(PlantDB).filter(
        PlantDB.user_id == user_id,
        PlantDB.last_watered >= datetime.combine(today, datetime.min.time())
    ).count()
    
    diagnoses_today = db.query(DiagnosisDB).filter(
        DiagnosisDB.user_id == user_id,
        DiagnosisDB.created_at >= datetime.combine(today, datetime.min.time())
    ).count()
    
    # Estadísticas de la semana
    posts_this_week = db.query(CommunityPostDB).filter(
        CommunityPostDB.user_id == user_id,
        CommunityPostDB.created_at >= datetime.combine(week_start, datetime.min.time())
    ).count()
    
    plants_added_this_week = db.query(PlantDB).filter(
        PlantDB.user_id == user_id,
        PlantDB.created_at >= datetime.combine(week_start, datetime.min.time())
    ).count()
    
    # Mapear progreso a misiones
    progress_map = {
        "water": plants_watered_today,
        "diagnose": diagnoses_today,
        "post": posts_this_week,
        "add_plant": plants_added_this_week,
        "view_garden": 1,  # Asumimos que ya lo vio
        "comment": 0  # TODO: Implementar conteo de comentarios
    }
    
    missions_with_progress = []
    for mission in MISSIONS:
        action = mission.get("action", "")
        target = mission.get("target", 1)
        progress = min(progress_map.get(action, 0), target)
        
        missions_with_progress.append({
            **mission,
            "progress": progress,
            "target": target,
            "completed": progress >= target
        })
    
    return {
        "daily": [m for m in missions_with_progress if m["type"] == "daily"],
        "weekly": [m for m in missions_with_progress if m["type"] == "weekly"],
        "streak_days": user.streak_days,
        "level": user.level,
        "xp": user.xp,
        "next_level_xp": user.level * 100
    }


@router.get("/stats/{user_id}")
async def get_gamification_stats(user_id: int, db: Session = Depends(get_db)):
    """CU-17: Obtener estadísticas de gamificación del usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    
    if not user:
        return {
            "level": 1,
            "xp": 0,
            "next_level_xp": 100,
            "total_points": 0,
            "streak_days": 0,
            "unlocked_achievements": 0,
            "total_achievements": len(ACHIEVEMENTS),
            "completed_missions": 0
        }
    
    # Contar logros desbloqueados
    plants = db.query(PlantDB).filter(PlantDB.user_id == user_id).all()
    diagnoses_count = db.query(DiagnosisDB).filter(DiagnosisDB.user_id == user_id).count()
    posts_count = db.query(CommunityPostDB).filter(CommunityPostDB.user_id == user_id).count()
    healthy_plants = len([p for p in plants if p.health_score >= 80])
    
    stats = {
        "plants_created": len(plants),
        "diagnoses_count": diagnoses_count,
        "streak_days": user.streak_days,
        "level": user.level,
        "posts_created": posts_count,
        "healthy_plants": healthy_plants
    }
    
    unlocked_count = sum(
        1 for a in ACHIEVEMENTS
        if stats.get(a["requirement"], 0) >= a["threshold"]
    )
    
    return {
        "level": user.level,
        "xp": user.xp,
        "next_level_xp": user.level * 100,
        "total_points": user.points,
        "streak_days": user.streak_days,
        "unlocked_achievements": unlocked_count,
        "total_achievements": len(ACHIEVEMENTS),
        "completed_missions": 0  # TODO: Implementar conteo real
    }


@router.post("/award-xp/{user_id}")
async def award_xp(user_id: int, xp: int, reason: str = "acción", db: Session = Depends(get_db)):
    """Otorgar XP al usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        raise HTTPException(404, "Usuario no encontrado")
    
    user.xp += xp
    
    # Subir de nivel si es necesario
    leveled_up = False
    xp_for_next_level = user.level * 100
    while user.xp >= xp_for_next_level:
        user.xp -= xp_for_next_level
        user.level += 1
        xp_for_next_level = user.level * 100
        leveled_up = True
    
    user.points += xp
    
    db.commit()
    db.refresh(user)
    
    message = f"+{xp} XP por {reason}"
    if leveled_up:
        message += f" ¡Subiste al nivel {user.level}! 🎉"
    
    return {
        "level": user.level,
        "xp": user.xp,
        "next_level_xp": user.level * 100,
        "total_points": user.points,
        "leveled_up": leveled_up,
        "message": message
    }


@router.post("/update-streak/{user_id}")
async def update_streak(user_id: int, db: Session = Depends(get_db)):
    """Actualizar racha del usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    if not user:
        raise HTTPException(404, "Usuario no encontrado")
    
    # Verificar si ya actualizó hoy
    today = datetime.utcnow().date()
    
    if user.last_activity:
        last_activity_date = user.last_activity.date()
        
        if last_activity_date == today:
            # Ya actualizó hoy
            return {
                "streak_days": user.streak_days,
                "message": f"Racha actual: {user.streak_days} días 🔥",
                "already_updated": True
            }
        elif last_activity_date == today - timedelta(days=1):
            # Día consecutivo - aumentar racha
            user.streak_days += 1
        else:
            # Se rompió la racha
            user.streak_days = 1
    else:
        user.streak_days = 1
    
    user.last_activity = datetime.utcnow()
    
    # Bonus XP por racha
    bonus_xp = 0
    if user.streak_days == 7:
        bonus_xp = 50
    elif user.streak_days == 30:
        bonus_xp = 200
    elif user.streak_days % 7 == 0:
        bonus_xp = 25
    
    if bonus_xp > 0:
        user.xp += bonus_xp
        user.points += bonus_xp
    
    db.commit()
    db.refresh(user)
    
    message = f"¡Racha de {user.streak_days} días! 🔥"
    if bonus_xp > 0:
        message += f" +{bonus_xp} XP bonus"
    
    return {
        "streak_days": user.streak_days,
        "message": message,
        "bonus_xp": bonus_xp
    }
