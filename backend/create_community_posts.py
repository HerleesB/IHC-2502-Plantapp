"""
Script para crear posts de ejemplo en la comunidad
Ejecutar: python create_community_posts.py
"""
import sys
from pathlib import Path
from datetime import datetime, timedelta

sys.path.insert(0, str(Path(__file__).parent))

from sqlalchemy.orm import sessionmaker
from app.models.database import engine, CommunityPostDB, CommentDB, UserDB

Session = sessionmaker(bind=engine)

def create_sample_posts():
    """Crea 3 posts de ejemplo con comentarios"""
    session = Session()
    
    try:
        # Verificar que existe el usuario demo
        demo_user = session.query(UserDB).filter(UserDB.username == "demo").first()
        if not demo_user:
            print("❌ Usuario demo no existe.")
            print("   Ejecuta primero: python create_demo_simple.py")
            return
        
        user_id = demo_user.id
        print(f"✅ Usuario demo encontrado (ID: {user_id})")
        
        # Verificar si ya existen posts
        existing_posts = session.query(CommunityPostDB).count()
        if existing_posts > 0:
            print(f"ℹ️  Ya existen {existing_posts} posts en la comunidad")
            response = input("   ¿Deseas crear posts adicionales? (s/n): ")
            if response.lower() != 's':
                print("   Cancelado")
                return
        
        print("\n👤 Creando posts de comunidad...")
        
        # Post 1: Monstera con hojas amarillas
        post1 = CommunityPostDB(
            user_id=user_id,
            user_name="María García",
            title="¿Hojas amarillas en mi Monstera?",
            content="Hola comunidad! Mi Monstera ha empezado a tener hojas amarillas en la parte inferior. Riego una vez por semana. ¿Es normal o debo preocuparme? He leído que podría ser exceso de riego, pero no estoy segura.",
            is_anonymous=False,
            likes_count=12,
            comments_count=2,
            created_at=datetime.now() - timedelta(days=6)
        )
        session.add(post1)
        session.flush()  # Para obtener el ID
        
        # Comentarios del post 1
        comment1_1 = CommentDB(
            post_id=post1.id,
            user_id=user_id,
            user_name="Carlos López",
            content="Es completamente normal que las hojas viejas se pongan amarillas. La planta está redistribuyendo los nutrientes a las hojas nuevas. Simplemente córtalas cuando estén completamente amarillas.",
            created_at=datetime.now() - timedelta(days=6, hours=-2)
        )
        session.add(comment1_1)
        
        comment1_2 = CommentDB(
            post_id=post1.id,
            user_id=user_id,
            user_name="Ana Martínez",
            content="También verifica que no estés regando demasiado. Las Monsteras prefieren secarse un poco entre riegos. Yo riego la mía solo cuando los primeros 5 cm de tierra están secos.",
            created_at=datetime.now() - timedelta(days=5, hours=-18)
        )
        session.add(comment1_2)
        
        print(f"   ✅ Post 1 creado: {post1.title}")
        
        # Post 2: Suculenta saludable
        post2 = CommunityPostDB(
            user_id=user_id,
            user_name="Pedro Sánchez",
            title="Mi suculenta está creciendo increíble 🌵",
            content="Después de seguir los consejos de esta comunidad, mi suculenta finalmente está saludable. La clave fue reducir el riego a una vez cada 2 semanas y darle más luz solar directa. Gracias a todos por la ayuda!",
            is_anonymous=False,
            likes_count=24,
            comments_count=3,
            created_at=datetime.now() - timedelta(days=7)
        )
        session.add(post2)
        session.flush()
        
        # Comentarios del post 2
        comment2_1 = CommentDB(
            post_id=post2.id,
            user_id=user_id,
            user_name="María García",
            content="¡Qué bien! Las suculentas son más resistentes de lo que pensamos. Menos es más con el riego.",
            created_at=datetime.now() - timedelta(days=7, hours=-3)
        )
        session.add(comment2_1)
        
        comment2_2 = CommentDB(
            post_id=post2.id,
            user_id=user_id,
            user_name="Laura Fernández",
            content="Totalmente de acuerdo. Yo casi mato la mía por regarla demasiado al principio 😅",
            created_at=datetime.now() - timedelta(days=6, hours=-20)
        )
        session.add(comment2_2)
        
        comment2_3 = CommentDB(
            post_id=post2.id,
            user_id=user_id,
            user_name="Carlos López",
            content="Pro tip: usa macetas con buen drenaje. Marca la diferencia.",
            created_at=datetime.now() - timedelta(days=6, hours=-15)
        )
        session.add(comment2_3)
        
        print(f"   ✅ Post 2 creado: {post2.title}")
        
        # Post 3: Tomate con manchas (anónimo)
        post3 = CommunityPostDB(
            user_id=user_id,
            user_name="Usuario Anónimo",
            title="Ayuda: Manchas marrones en hojas de tomate 🍅",
            content="He notado manchas marrones en las hojas de mis plantas de tomate. ¿Podría ser un hongo? ¿Qué fungicida natural recomiendan? No quiero usar químicos fuertes.",
            is_anonymous=True,
            likes_count=8,
            comments_count=2,
            created_at=datetime.now() - timedelta(days=8)
        )
        session.add(post3)
        session.flush()
        
        # Comentarios del post 3
        comment3_1 = CommentDB(
            post_id=post3.id,
            user_id=user_id,
            user_name="Laura Fernández",
            content="Podría ser tizón temprano. Prueba con una solución de bicarbonato de sodio: 1 cucharada por litro de agua, rocía las hojas. Funciona como fungicida natural.",
            created_at=datetime.now() - timedelta(days=8, hours=-4)
        )
        session.add(comment3_1)
        
        comment3_2 = CommentDB(
            post_id=post3.id,
            user_id=user_id,
            user_name="Carlos López",
            content="También asegúrate de que las plantas tengan buena circulación de aire y evita mojar las hojas al regar. Riega directo al suelo.",
            created_at=datetime.now() - timedelta(days=7, hours=-22)
        )
        session.add(comment3_2)
        
        print(f"   ✅ Post 3 creado: {post3.title}")
        
        # Commit de todos los cambios
        session.commit()
        
        print("\n" + "=" * 60)
        print("✅ POSTS DE COMUNIDAD CREADOS EXITOSAMENTE")
        print("=" * 60)
        print(f"\n📊 Resumen:")
        print(f"   • {post1.title} (12 likes, 2 comentarios)")
        print(f"   • {post2.title} (24 likes, 3 comentarios)")
        print(f"   • {post3.title} (8 likes, 2 comentarios)")
        print(f"\n   Total: 3 posts, 7 comentarios")
        print("\n🎉 Ahora puedes ver estos posts en la app en la sección Comunidad")
        
    except Exception as e:
        session.rollback()
        print(f"\n❌ Error al crear posts: {e}")
        import traceback
        traceback.print_exc()
    finally:
        session.close()


if __name__ == "__main__":
    print("\n" + "=" * 60)
    print("🌱 CREACIÓN DE POSTS DE EJEMPLO PARA COMUNIDAD")
    print("=" * 60)
    print()
    create_sample_posts()
    print()
