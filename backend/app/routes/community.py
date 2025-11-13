"""Rutas para comunidad (CU-07, CU-09)"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.models.database import get_db, CommunityPostDB, CommentDB, DiagnosisDB, UserDB
from app.models.schemas import CommunityPost, CommentCreate, CommunityPostCreate
from app.services.groq_service import moderate_content

router = APIRouter(prefix="/api/community", tags=["Community"])

@router.post("/posts", response_model=CommunityPost)
async def create_post(post: CommunityPostCreate, user_id: int = 1, db: Session = Depends(get_db)):
    """CU-07: Publicar caso a la comunidad"""
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
        is_anonymous=post.is_anonymous
    )
    db.add(db_post)
    db.commit()
    db.refresh(db_post)
    
    user = db.query(UserDB).filter(UserDB.id == user_id).first()
    author_name = "Anónimo" if post.is_anonymous else user.username
    
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

@router.get("/posts")
async def get_posts(limit: int = 20, db: Session = Depends(get_db)):
    """Obtener posts de la comunidad"""
    posts = db.query(CommunityPostDB).order_by(CommunityPostDB.created_at.desc()).limit(limit).all()
    return posts

@router.post("/posts/{post_id}/comments")
async def add_comment(post_id: int, comment: CommentCreate, user_id: int = 1, db: Session = Depends(get_db)):
    """CU-09: Agregar comentario/respuesta"""
    # Moderar contenido
    is_appropriate = await moderate_content(comment.content)
    if not is_appropriate:
        raise HTTPException(400, "Comentario inapropiado")
    
    db_comment = CommentDB(
        post_id=post_id,
        user_id=user_id,
        content=comment.content,
        is_solution=comment.is_solution
    )
    db.add(db_comment)
    
    # Actualizar contador
    post = db.query(CommunityPostDB).filter(CommunityPostDB.id == post_id).first()
    post.comments_count += 1
    if comment.is_solution:
        post.status = "resolved"
    
    db.commit()
    return {"message": "Comentario agregado", "comment_id": db_comment.id}
