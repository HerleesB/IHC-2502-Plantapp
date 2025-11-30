"""Servicio de Autenticación"""
from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from app.models.database import UserDB
from app.models.schemas import UserCreate, UserLogin, TokenResponse, UserResponse
from app.utils.auth import get_password_hash, authenticate_user, create_access_token
from datetime import timedelta


class AuthService:
    """Servicio para gestionar autenticación de usuarios"""
    
    @staticmethod
    def register_user(db: Session, user_data: UserCreate) -> TokenResponse:
        """
        Registra un nuevo usuario y retorna token JWT
        """
        # Verificar si el email ya existe
        existing_user = db.query(UserDB).filter(UserDB.email == user_data.email).first()
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El email ya está registrado"
            )
        
        # Verificar si el username ya existe
        existing_username = db.query(UserDB).filter(UserDB.username == user_data.username).first()
        if existing_username:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El nombre de usuario ya está en uso"
            )
        
        # Crear nuevo usuario
        hashed_password = get_password_hash(user_data.password)
        new_user = UserDB(
            email=user_data.email,
            username=user_data.username,
            full_name=user_data.full_name,
            hashed_password=hashed_password,
            level=1,
            xp=0,
            points=0,
            streak_days=0
        )
        
        db.add(new_user)
        db.commit()
        db.refresh(new_user)
        
        # Crear logros iniciales para el usuario
        AuthService._create_initial_achievements(db, new_user.id)
        
        # Generar token
        access_token = create_access_token(
            data={"sub": new_user.id},
            expires_delta=timedelta(days=7)
        )
        
        # Retornar respuesta con token y datos del usuario
        return TokenResponse(
            access_token=access_token,
            token_type="bearer",
            user=UserResponse(
                id=new_user.id,
                username=new_user.username,
                email=new_user.email,
                full_name=new_user.full_name,
                level=new_user.level,
                xp=new_user.xp,
                points=new_user.points,
                streak_days=new_user.streak_days
            )
        )
    
    @staticmethod
    def login_user(db: Session, credentials: UserLogin) -> TokenResponse:
        """
        Autentica un usuario y retorna token JWT
        """
        user = authenticate_user(db, credentials.email_or_username, credentials.password)
        
        if not user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Email/usuario o contraseña incorrectos"
            )
        
        # Generar token
        access_token = create_access_token(
            data={"sub": user.id},
            expires_delta=timedelta(days=7)
        )
        
        return TokenResponse(
            access_token=access_token,
            token_type="bearer",
            user=UserResponse(
                id=user.id,
                username=user.username,
                email=user.email,
                full_name=user.full_name,
                level=user.level,
                xp=user.xp,
                points=user.points,
                streak_days=user.streak_days
            )
        )
    
    @staticmethod
    def _create_initial_achievements(db: Session, user_id: int):
        """Crea logros iniciales para un nuevo usuario"""
        from app.models.database import AchievementDB
        
        initial_achievements = [
            {
                "name": "Primera Planta",
                "description": "Agregaste tu primera planta al jardín",
                "icon": "🌱",
                "points": 10,
            },
            {
                "name": "Primer Diagnóstico",
                "description": "Realizaste tu primer diagnóstico con IA",
                "icon": "🔍",
                "points": 20,
            },
            {
                "name": "Jardinero Social",
                "description": "Compartiste un diagnóstico en la comunidad",
                "icon": "🤝",
                "points": 15,
            },
            {
                "name": "Racha de 7 días",
                "description": "Mantuviste una racha de 7 días consecutivos",
                "icon": "🔥",
                "points": 50,
            },
            {
                "name": "Experto en Plantas",
                "description": "Tienes 10 o más plantas en tu jardín",
                "icon": "🏆",
                "points": 100,
            },
        ]
        
        for achievement_data in initial_achievements:
            achievement = AchievementDB(
                user_id=user_id,
                name=achievement_data["name"],
                description=achievement_data["description"],
                icon=achievement_data["icon"],
                points=achievement_data["points"],
                unlocked=False
            )
            db.add(achievement)
        
        db.commit()
    
    @staticmethod
    def get_user_profile(db: Session, user_id: int) -> UserResponse:
        """Obtiene el perfil completo de un usuario"""
        user = db.query(UserDB).filter(UserDB.id == user_id).first()
        
        if not user:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Usuario no encontrado"
            )
        
        return UserResponse(
            id=user.id,
            username=user.username,
            email=user.email,
            full_name=user.full_name,
            level=user.level,
            xp=user.xp,
            points=user.points,
            streak_days=user.streak_days
        )
