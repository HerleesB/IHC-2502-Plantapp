"""
Script para poblar la comunidad con datos realistas
VERSIÓN SIN BCRYPT - Compatible con Python 3.14
"""
import sys
sys.path.insert(0, '.')

from app.models.database import SessionLocal, UserDB, CommunityPostDB, CommentDB, DiagnosisDB
from datetime import datetime, timedelta
import random
import hashlib

# Función simple de hash (solo para demo)
def simple_hash(password: str) -> str:
    """Hash simple para passwords (solo demo, no producción)"""
    return hashlib.sha256(password.encode()).hexdigest()

# Datos realistas de usuarios
USUARIOS = [
    {
        "username": "maria_jardinera",
        "email": "maria@example.com",
        "full_name": "María González",
        "password": "password123"
    },
    {
        "username": "pedro_plantas",
        "email": "pedro@example.com",
        "full_name": "Pedro Martínez",
        "password": "password123"
    },
    {
        "username": "ana_verde",
        "email": "ana@example.com",
        "full_name": "Ana López",
        "password": "password123"
    },
    {
        "username": "carlos_botanico",
        "email": "carlos@example.com",
        "full_name": "Carlos Rodríguez",
        "password": "password123"
    },
    {
        "username": "lucia_flores",
        "email": "lucia@example.com",
        "full_name": "Lucía Fernández",
        "password": "password123"
    }
]

# Publicaciones realistas con imágenes
PUBLICACIONES = [
    {
        "plant_name": "Tomate Cherry",
        "description": "Mis tomates tienen manchas marrones en las hojas. ¿Será algún hongo? Las plantas están en el balcón con buena luz.",
        "symptoms": "Manchas marrones, hojas secas",
        "image_url": "https://images.unsplash.com/photo-1592841200221-a6898f307baa?w=400",
        "likes": 15,
        "comments_count": 3
    },
    {
        "plant_name": "Monstera Deliciosa",
        "description": "Las hojas de mi monstera se están poniendo amarillas desde las puntas. ¿Puede ser exceso de riego?",
        "symptoms": "Hojas amarillas, puntas secas",
        "image_url": "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?w=400",
        "likes": 23,
        "comments_count": 5
    },
    {
        "plant_name": "Suculenta Echeveria",
        "description": "Mi suculenta está perdiendo hojas inferiores. Las hojas se caen muy fácilmente. ¿Es normal?",
        "symptoms": "Caída de hojas, hojas blandas",
        "image_url": "https://images.unsplash.com/photo-1459156212016-c812468e2115?w=400",
        "likes": 8,
        "comments_count": 2
    },
    {
        "plant_name": "Orquídea Phalaenopsis",
        "description": "Las raíces de mi orquídea se ven grises y algunas están blandas. ¿Qué puedo hacer?",
        "symptoms": "Raíces grises, raíces blandas",
        "image_url": "https://images.unsplash.com/photo-1517650862521-d580d5348145?w=400",
        "likes": 19,
        "comments_count": 4
    },
    {
        "plant_name": "Albahaca",
        "description": "Mi albahaca tiene pequeños agujeros en las hojas. Creo que son insectos pero no los veo.",
        "symptoms": "Agujeros en hojas, bordes mordidos",
        "image_url": "https://images.unsplash.com/photo-1618375569909-3c8616cf7e6e?w=400",
        "likes": 12,
        "comments_count": 6
    },
    {
        "plant_name": "Pothos Dorado",
        "description": "Las hojas nuevas de mi pothos salen muy pequeñas y pálidas. ¿Le falta luz o nutrientes?",
        "symptoms": "Hojas pequeñas, color pálido",
        "image_url": "https://images.unsplash.com/photo-1614594895304-fe7116ac2b58?w=400",
        "likes": 31,
        "comments_count": 7
    },
    {
        "plant_name": "Rosa del Desierto",
        "description": "Las hojas de mi rosa del desierto se están arrugando. La riego poco porque sé que es suculenta.",
        "symptoms": "Hojas arrugadas, tallo blando",
        "image_url": "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=400",
        "likes": 5,
        "comments_count": 1
    },
    {
        "plant_name": "Ficus Lyrata",
        "description": "Mi ficus tiene manchas marrones en los bordes de las hojas. Las manchas son secas y crujientes.",
        "symptoms": "Manchas marrones, bordes secos",
        "image_url": "https://images.unsplash.com/photo-1545241047-6083a3684587?w=400",
        "likes": 27,
        "comments_count": 8
    },
    {
        "plant_name": "Cactus San Pedro",
        "description": "Mi cactus se está poniendo amarillo desde la base. ¿Será pudrición por exceso de agua?",
        "symptoms": "Amarillamiento, base blanda",
        "image_url": "https://images.unsplash.com/photo-1509937528035-ad76254b0356?w=400",
        "likes": 14,
        "comments_count": 4
    },
    {
        "plant_name": "Helecho de Boston",
        "description": "Las puntas de mi helecho se están secando a pesar de que lo rocío con agua todos los días.",
        "symptoms": "Puntas secas, hojas marrones",
        "image_url": "https://images.unsplash.com/photo-1523890525028-16f4b2f2e02f?w=400",
        "likes": 18,
        "comments_count": 3
    }
]

# Comentarios de ejemplo
COMENTARIOS = [
    "He tenido el mismo problema y lo solucioné mejorando el drenaje",
    "Parece ser exceso de riego, déjala secar bien entre riegos",
    "Yo tuve algo similar, prueba con un fungicida orgánico",
    "Puede ser falta de humedad ambiental, prueba poniéndola cerca de otras plantas",
    "A mi me funcionó cambiarla de maceta con tierra nueva",
    "Creo que necesita más luz indirecta",
    "Revisa si tiene plagas en el envés de las hojas"
]

def crear_usuarios(db):
    """Crea usuarios realistas"""
    print("\n👥 Creando usuarios realistas...")
    usuarios_creados = []
    
    for user_data in USUARIOS:
        # Verificar si ya existe
        existing = db.query(UserDB).filter(UserDB.username == user_data["username"]).first()
        if existing:
            print(f"  ⏭️  Usuario '{user_data['username']}' ya existe")
            usuarios_creados.append(existing)
            continue
        
        user = UserDB(
            email=user_data["email"],
            username=user_data["username"],
            full_name=user_data["full_name"],
            hashed_password=simple_hash(user_data["password"]),
            level=1,
            xp=random.randint(100, 500),
            points=random.randint(50, 300),
            created_at=datetime.utcnow() - timedelta(days=random.randint(30, 180))
        )
        db.add(user)
        usuarios_creados.append(user)
        print(f"  ✅ Creado: {user_data['full_name']} (@{user_data['username']})")
    
    db.commit()
    return usuarios_creados

def crear_publicaciones(db, usuarios):
    """Crea publicaciones con imágenes y descripciones realistas"""
    print("\n📝 Creando publicaciones con imágenes...")
    
    for i, pub_data in enumerate(PUBLICACIONES):
        # Asignar usuario aleatorio
        user = random.choice(usuarios)
        is_anonymous = random.random() < 0.2  # 20% anónimos
        
        post = CommunityPostDB(
            user_id=user.id,
            diagnosis_id=None,  # Sin diagnóstico por ahora
            is_anonymous=is_anonymous,
            description=pub_data["description"],
            plant_name=pub_data["plant_name"],
            symptoms=pub_data["symptoms"],
            image_url=pub_data["image_url"],
            likes=pub_data["likes"],
            comments_count=pub_data["comments_count"],
            status="approved",
            created_at=datetime.utcnow() - timedelta(hours=random.randint(1, 72))
        )
        
        db.add(post)
        db.flush()
        
        # Crear algunos comentarios
        num_comments = random.randint(1, min(4, pub_data["comments_count"]))
        for j in range(num_comments):
            commenter = random.choice(usuarios)
            comment = CommentDB(
                post_id=post.id,
                user_id=commenter.id,
                content=random.choice(COMENTARIOS),
                created_at=datetime.utcnow() - timedelta(hours=random.randint(1, 48))
            )
            db.add(comment)
        
        username_display = "Anónimo" if is_anonymous else user.username
        print(f"  ✅ Post {i+1}: {pub_data['plant_name']} por {username_display}")
    
    db.commit()

def main():
    print("=" * 70)
    print("  🌱 POBLACIÓN DE COMUNIDAD CON DATOS REALISTAS")
    print("=" * 70)
    
    db = SessionLocal()
    
    try:
        # 1. Crear usuarios
        usuarios = crear_usuarios(db)
        
        # 2. Crear publicaciones
        crear_publicaciones(db, usuarios)
        
        print("\n" + "=" * 70)
        print("  ✅ COMUNIDAD POBLADA EXITOSAMENTE")
        print("=" * 70)
        print(f"\n  📊 Resumen:")
        print(f"     • {len(USUARIOS)} usuarios creados")
        print(f"     • {len(PUBLICACIONES)} publicaciones con imágenes")
        print(f"     • Comentarios y likes distribuidos")
        print(f"\n  ⚠️  NOTA: Usuarios creados con hash simple (no bcrypt)")
        print(f"     Para login de prueba, usa el usuario demo existente")
        print("\n" + "=" * 70)
        
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    main()
