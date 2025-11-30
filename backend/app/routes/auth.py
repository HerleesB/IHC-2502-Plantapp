"""Rutas de Autenticación"""
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.models.database import get_db, UserDB
from app.models.schemas import UserCreate, UserLogin, TokenResponse, UserResponse
from app.services.auth_service import AuthService
from app.utils.auth import get_current_user

router = APIRouter(prefix="/api/auth", tags=["Autenticación"])


@router.post("/register", response_model=TokenResponse)
async def register(user_data: UserCreate, db: Session = Depends(get_db)):
    """
    Registra un nuevo usuario en el sistema.
    
    - **email**: Email único del usuario
    - **username**: Nombre de usuario único
    - **password**: Contraseña (mínimo 6 caracteres recomendado)
    - **full_name**: Nombre completo (opcional)
    
    Retorna token JWT y datos del usuario.
    """
    return AuthService.register_user(db, user_data)


@router.post("/login", response_model=TokenResponse)
async def login(credentials: UserLogin, db: Session = Depends(get_db)):
    """
    Inicia sesión con email/username y contraseña.
    
    - **email_or_username**: Email o nombre de usuario
    - **password**: Contraseña del usuario
    
    Retorna token JWT y datos del usuario.
    """
    return AuthService.login_user(db, credentials)


@router.get("/me", response_model=UserResponse)
async def get_my_profile(current_user: UserDB = Depends(get_current_user)):
    """
    Obtiene el perfil del usuario autenticado.
    Requiere token JWT en header: Authorization: Bearer <token>
    """
    return UserResponse(
        id=current_user.id,
        username=current_user.username,
        email=current_user.email,
        full_name=current_user.full_name,
        level=current_user.level,
        xp=current_user.xp,
        points=current_user.points,
        streak_days=current_user.streak_days
    )


@router.post("/logout")
async def logout(current_user: UserDB = Depends(get_current_user)):
    """
    Cierra sesión del usuario actual.
    
    Nota: Con JWT stateless, el logout se maneja en el cliente
    eliminando el token. Este endpoint es para tracking o
    invalidación de tokens si se implementa una blacklist.
    """
    return {
        "message": "Sesión cerrada exitosamente",
        "user_id": current_user.id
    }


@router.post("/refresh", response_model=TokenResponse)
async def refresh_token(current_user: UserDB = Depends(get_current_user), db: Session = Depends(get_db)):
    """
    Refresca el token JWT del usuario autenticado.
    Útil para extender la sesión sin requerir login nuevamente.
    """
    from app.utils.auth import create_access_token
    from datetime import timedelta
    
    # Generar nuevo token
    new_token = create_access_token(
        data={"sub": current_user.id},
        expires_delta=timedelta(days=7)
    )
    
    return TokenResponse(
        access_token=new_token,
        token_type="bearer",
        user=UserResponse(
            id=current_user.id,
            username=current_user.username,
            email=current_user.email,
            full_name=current_user.full_name,
            level=current_user.level,
            xp=current_user.xp,
            points=current_user.points,
            streak_days=current_user.streak_days
        )
    )
