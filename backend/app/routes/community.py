"""Rutas para comunidad (CU-07, CU-09, CU-18, CU-19)"""
from fastapi import APIRouter, Depends, HTTPException, File, UploadFile, Form
from sqlalchemy.orm import Session
from app.models.database import get_db, CommunityPostDB, CommentDB, DiagnosisDB, UserDB
from app.models.schemas import CommunityPost, CommentCreate, CommunityPostCreate
from app.services.groq_service import moderate_content
from typing import Optional
import os
import uuid
from datetime import datetime

router = APIRouter(prefix="/api/community", tags=["Community"])

# Directorio para guardar imágenes
UPLOAD_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "uploads", "community")
os.makedirs(UPLOAD_DIR, exist_ok=True)

@router.post("/posts", response_model=CommunityPost)
async def create_post(post: CommunityPostCreate, user_id: int = 1, db: Session = Depends(get_db)):
    """CU-07: Publicar caso a la comunidad (desde diagnóstico existente)"""
    diagnosis = db.query(DiagnosisDB).filter(DiagnosisDB.id == post.diagnosis_id).first()
    if not diagnosis:
        raise HTTPException(404, "Diagnóstico no encontrado")
    
    # CU-09: Moderación asistida por IA
    is_appropriate = await moderate_content(diagnosis.diagnosis_text)
    if not is_appropriate:
        raise HTTPException(400, "Contenido inapropiado detectado")
    
    db_post = CommunityPostDB(
        diagnosis_id=post.diagnosis_id,
        user_id=user_id,
        is_anonymous=post.is_anonymous,
        status="approved"
    )
    db.add(db_post)
    db.commit()
    db.refresh(db_post)
    
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    author_name = "Anónimo" if post.is_anonymous else (user.username if user else f"Usuario #{user_id}")
    
    return CommunityPost(
        id=db_post.id,
        diagnosis_id=db_post.diagnosis_id,
        user_id=db_post.user_id,
        author_name=author_name,
        is_anonymous=db_post.is_anonymous,
        likes=db_post.likes,
        comments_count=db_post.comments_count,
        status=db_post.status,
        created_at=db_post.created_at
    )


@router.post("/posts/with-image")
async def create_post_with_image(
    image: UploadFile = File(...),
    description: str = Form(...),
    plant_name: Optional[str] = Form(None),
    symptoms: Optional[str] = Form(None),
    is_anonymous: str = Form("false"),
    user_id: str = Form(...)
):
    """CU-18: Publicar caso con imagen directamente (sin diagnóstico previo)"""
    db = next(get_db())
    
    try:
        # Validar usuario
        user_id_int = int(user_id)
        user = db.query(UserDB).filter(UserDB.id == user_id_int).first()
        if not user:
            raise HTTPException(404, "Usuario no encontrado")
        
        # CU-09: Moderar contenido
        content_to_moderate = f"{description} {plant_name or ''} {symptoms or ''}"
        is_appropriate = await moderate_content(content_to_moderate)
        if not is_appropriate:
            raise HTTPException(400, "Contenido inapropiado detectado")
        
        # Guardar imagen
        file_extension = image.filename.split(".")[-1] if "." in image.filename else "jpg"
        file_name = f"{uuid.uuid4()}.{file_extension}"
        file_path = os.path.join(UPLOAD_DIR, file_name)
        
        with open(file_path, "wb") as f:
            content = await image.read()
            f.write(content)
        
        image_url = f"uploads/community/{file_name}"  # Sin / inicial para que funcione con base_url
        
        # Crear diagnóstico temporal para el post
        import json
        temp_diagnosis = DiagnosisDB(
            plant_id=0,
            user_id=user_id_int,
            image_url=image_url,
            diagnosis_text=description,
            disease_name=symptoms or "Consulta de la comunidad",
            confidence=0.0,
            severity="low",
            recommendations=json.dumps([])  # Debe ser string JSON, no lista
        )
        db.add(temp_diagnosis)
        db.commit()
        db.refresh(temp_diagnosis)
        
        # Crear post
        is_anon = is_anonymous.lower() == "true"
        db_post = CommunityPostDB(
            diagnosis_id=temp_diagnosis.id,
            user_id=user_id_int,
            is_anonymous=is_anon,
            description=description,
            plant_name=plant_name,
            symptoms=symptoms,
            image_url=image_url,
            status="approved"
        )
        db.add(db_post)
        db.commit()
        db.refresh(db_post)
        
        author_name = "Anónimo" if is_anon else user.username
        
        return {
            "id": db_post.id,
            "diagnosis_id": temp_diagnosis.id,
            "user_id": user_id_int,
            "author_name": author_name,
            "is_anonymous": is_anon,
            "likes": 0,
            "comments_count": 0,
            "status": "approved",
            "description": description,
            "plant_name": plant_name,
            "symptoms": symptoms,
            "image_url": image_url,
            "created_at": db_post.created_at.isoformat()
        }
        
    except ValueError:
        raise HTTPException(400, "user_id inválido")
    except Exception as e:
        db.rollback()
        raise HTTPException(500, f"Error al crear post: {str(e)}")
    finally:
        db.close()


@router.get("/posts")
async def get_posts(limit: int = 20, db: Session = Depends(get_db)):
    """CU-19: Obtener feed de posts de la comunidad"""
    posts = db.query(CommunityPostDB).order_by(CommunityPostDB.created_at.desc()).limit(limit).all()
    
    result = []
    for post in posts:
        user = db.query(UserDB).filter(UserDB.id == post.user_id).first()
        author_name = "Anónimo" if post.is_anonymous else (user.username if user else f"Usuario #{post.user_id}")
        
        # Obtener imagen del diagnóstico si el post no tiene imagen propia
        image_url = post.image_url
        if not image_url and post.diagnosis_id:
            diagnosis = db.query(DiagnosisDB).filter(DiagnosisDB.id == post.diagnosis_id).first()
            if diagnosis:
                image_url = diagnosis.image_url
        
        result.append({
            "id": post.id,
            "diagnosis_id": post.diagnosis_id,
            "user_id": post.user_id,
            "author_name": author_name,
            "is_anonymous": post.is_anonymous,
            "likes": post.likes,
            "comments_count": post.comments_count,
            "status": post.status,
            "description": post.description,
            "plant_name": post.plant_name,
            "symptoms": post.symptoms,
            "image_url": image_url,
            "created_at": post.created_at.isoformat()
        })
    
    return result


@router.post("/posts/{post_id}/comments")
async def add_comment(post_id: int, comment: CommentCreate, user_id: int = 1, db: Session = Depends(get_db)):
    """CU-09: Agregar comentario/respuesta con moderación asistida"""
    post = db.query(CommunityPostDB).filter(CommunityPostDB.id == post_id).first()
    if not post:
        raise HTTPException(404, "Post no encontrado")
    
    # Moderar contenido
    is_appropriate = await moderate_content(comment.content)
    if not is_appropriate:
        raise HTTPException(400, "Comentario inapropiado detectado")
    
    # Obtener nombre del usuario
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    author_name = user.username if user else f"Usuario #{user_id}"
    
    db_comment = CommentDB(
        post_id=post_id,
        user_id=user_id,
        content=comment.content,
        is_solution=comment.is_solution
    )
    db.add(db_comment)
    
    # Actualizar contador
    post.comments_count += 1
    if comment.is_solution:
        post.status = "resolved"
    
    db.commit()
    db.refresh(db_comment)
    
    return {
        "message": "Comentario agregado",
        "comment_id": db_comment.id,
        "author_name": author_name,
        "content": comment.content,
        "is_solution": comment.is_solution
    }


@router.post("/posts/{post_id}/like")
async def toggle_like(post_id: int, user_id: int = Form(1), db: Session = Depends(get_db)):
    """Toggle like en un post (dar o quitar like) - 1 like por usuario"""
    from app.models.database import PostLikeDB
    
    # Verificar si el usuario ya dio like
    existing_like = db.query(PostLikeDB).filter(
        PostLikeDB.post_id == post_id,
        PostLikeDB.user_id == user_id
    ).first()
    
    post = db.query(CommunityPostDB).filter(CommunityPostDB.id == post_id).first()
    if not post:
        raise HTTPException(404, "Post no encontrado")
    
    if existing_like:
        # Quitar like
        db.delete(existing_like)
        post.likes = max(0, post.likes - 1)
        liked = False
        message = "Like removido"
    else:
        # Agregar like
        new_like = PostLikeDB(
            post_id=post_id,
            user_id=user_id
        )
        db.add(new_like)
        post.likes += 1
        liked = True
        message = "Like agregado"
    
    db.commit()
    
    return {
        "success": True,
        "message": message,
        "liked": liked,
        "total_likes": post.likes
    }


@router.get("/posts/{post_id}/liked-by/{user_id}")
async def check_user_liked(post_id: int, user_id: int, db: Session = Depends(get_db)):
    """Verificar si un usuario dio like a un post"""
    from app.models.database import PostLikeDB
    
    liked = db.query(PostLikeDB).filter(
        PostLikeDB.post_id == post_id,
        PostLikeDB.user_id == user_id
    ).first() is not None
    
    return {"liked": liked}


@router.get("/posts/{post_id}/comments")
async def get_comments(post_id: int, db: Session = Depends(get_db)):
    """Obtener comentarios de un post"""
    comments = db.query(CommentDB).filter(CommentDB.post_id == post_id).order_by(CommentDB.created_at.desc()).all()
    
    result = []
    for c in comments:
        user = db.query(UserDB).filter(UserDB.id == c.user_id).first()
        author_name = user.username if user else f"Usuario #{c.user_id}"
        
        result.append({
            "id": c.id,
            "user_id": c.user_id,
            "author_name": author_name,
            "content": c.content,
            "is_solution": c.is_solution,
            "likes": c.likes,
            "created_at": c.created_at.isoformat()
        })
    
    return result


@router.get("/posts/{post_id}")
async def get_post_detail(post_id: int, db: Session = Depends(get_db)):
    """Obtener detalle de un post específico"""
    post = db.query(CommunityPostDB).filter(CommunityPostDB.id == post_id).first()
    if not post:
        raise HTTPException(404, "Post no encontrado")
    
    user = db.query(UserDB).filter(UserDB.id == post.user_id).first()
    author_name = "Anónimo" if post.is_anonymous else (user.username if user else f"Usuario #{post.user_id}")
    
    # Obtener diagnóstico asociado
    diagnosis = None
    if post.diagnosis_id:
        diag = db.query(DiagnosisDB).filter(DiagnosisDB.id == post.diagnosis_id).first()
        if diag:
            diagnosis = {
                "id": diag.id,
                "diagnosis_text": diag.diagnosis_text,
                "disease_name": diag.disease_name,
                "confidence": diag.confidence,
                "severity": diag.severity,
                "recommendations": diag.recommendations,
                "image_url": diag.image_url
            }
    
    return {
        "id": post.id,
        "diagnosis_id": post.diagnosis_id,
        "user_id": post.user_id,
        "author_name": author_name,
        "is_anonymous": post.is_anonymous,
        "likes": post.likes,
        "comments_count": post.comments_count,
        "status": post.status,
        "description": post.description,
        "plant_name": post.plant_name,
        "symptoms": post.symptoms,
        "image_url": post.image_url,
        "created_at": post.created_at.isoformat(),
        "diagnosis": diagnosis
    }
