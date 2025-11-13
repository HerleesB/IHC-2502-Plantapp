"""Rutas para gamificación (CU-06)"""
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.models.database import get_db, UserDB, AchievementDB
from app.models.schemas import Achievement, Mission

router = APIRouter(prefix="/api/gamification", tags=["Gamification"])

@router.get("/achievements/{user_id}")
async def get_achievements(user_id: int, db: Session = Depends(get_db)):
    """CU-06: Obtener logros del usuario"""
    achievements = db.query(AchievementDB).filter(AchievementDB.user_id == user_id).all()
    return achievements

@router.get("/missions/{user_id}")
async def get_missions(user_id: int):
    """CU-06: Obtener misiones semanales"""
    # Misiones hardcoded por ahora
    missions = [
        {"id": 1, "title": "Cuida 3 plantas", "progress": 2, "target": 3, "reward_xp": 50},
        {"id": 2, "title": "Comparte un diagnóstico", "progress": 0, "target": 1, "reward_xp": 30},
        {"id": 3, "title": "Mantén racha de 7 días", "progress": 5, "target": 7, "reward_xp": 100}
    ]
    return missions

@router.post("/award-xp/{user_id}")
async def award_xp(user_id: int, xp: int, db: Session = Depends(get_db)):
    """Otorgar XP al usuario"""
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    user.xp += xp
    if user.xp >= user.level * 100:
        user.level += 1
        user.xp = 0
    db.commit()
    return {"level": user.level, "xp": user.xp}
