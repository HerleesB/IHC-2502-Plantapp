"""
Script para actualizar automáticamente el endpoint de likes
"""
import re

# Leer archivo
with open('app/routes/community.py', 'r', encoding='utf-8') as f:
    content = f.read()

# Código antiguo a buscar
old_code = '''@router.post("/posts/{post_id}/like")
async def toggle_like(post_id: int, user_id: int = 1, db: Session = Depends(get_db)):
    """Dar/quitar like a un post"""
    post = db.query(CommunityPostDB).filter(CommunityPostDB.id == post_id).first()
    if not post:
        raise HTTPException(404, "Post no encontrado")
    
    post.likes += 1
    db.commit()
    return {"message": "Like agregado", "likes": post.likes}'''

# Código nuevo
new_code = '''@router.post("/posts/{post_id}/like")
async def toggle_like(post_id: int, user_id: int = Form(...), db: Session = Depends(get_db)):
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
    
    return {"liked": liked}'''

# Reemplazar
if old_code in content:
    content = content.replace(old_code, new_code)
    print("✅ Endpoint de likes actualizado")
else:
    print("⚠️ No se encontró el código antiguo, intentando con regex...")
    # Buscar con regex más flexible
    pattern = r'@router\.post\("/posts/\{post_id\}/like"\).*?return \{.*?\}'
    if re.search(pattern, content, re.DOTALL):
        content = re.sub(pattern, new_code, content, flags=re.DOTALL)
        print("✅ Endpoint de likes actualizado (regex)")
    else:
        print("❌ No se pudo encontrar el endpoint de likes")
        exit(1)

# Guardar
with open('app/routes/community.py', 'w', encoding='utf-8') as f:
    f.write(content)

print("✅ Archivo community.py actualizado exitosamente")
