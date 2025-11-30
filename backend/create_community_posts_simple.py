"""
Script CORREGIDO para crear posts de ejemplo en la comunidad
IMPORTANTE: Este script usa los campos REALES del modelo CommunityPostDB
Ejecutar: python create_community_posts_simple.py
"""
import sys
from pathlib import Path
from datetime import datetime, timedelta

sys.path.insert(0, str(Path(__file__).parent))

from sqlalchemy.orm import sessionmaker
from app.models.database import engine, CommunityPostDB, CommentDB, UserDB, DiagnosisDB, PlantDB

Session = sessionmaker(bind=engine)

def create_sample_data():
    """
    Crea posts de ejemplo usando el modelo REAL de CommunityPostDB
    que solo tiene: diagnosis_id, user_id, is_anonymous, likes, comments_count, status
    """
    session = Session()
    
    try:
        # Verificar usuario demo
        demo_user = session.query(UserDB).filter(UserDB.username == "demo").first()
        if not demo_user:
            print("❌ Usuario demo no existe.")
            print("   Ejecuta primero: python create_demo_simple.py")
            return
        
        user_id = demo_user.id
        print(f"✅ Usuario demo encontrado (ID: {user_id})")
        
        # Verificar si el usuario tiene plantas
        plant = session.query(PlantDB).filter(PlantDB.user_id == user_id).first()
        if not plant:
            print("\n📝 Creando planta de ejemplo...")
            plant = PlantDB(
                user_id=user_id,
                name="Monstera de María",
                species="Monstera deliciosa",
                description="Mi primera Monstera",
                status="healthy",
                health_score=85
            )
            session.add(plant)
            session.flush()
            print(f"   ✅ Planta creada (ID: {plant.id})")
        else:
            print(f"✅ Planta existente encontrada (ID: {plant.id})")
        
        print("\n👤 Creando diagnósticos y posts de comunidad...")
        
        # Diagnóstico 1: Hojas amarillas en Monstera
        diagnosis1 = DiagnosisDB(
            plant_id=plant.id,
            user_id=user_id,
            image_url="",
            diagnosis_text="Hojas amarillas en la parte inferior. Posible exceso de riego.",
            confidence=0.85,
            disease_name="Exceso de riego",
            severity="leve",
            recommendations='["Reducir frecuencia de riego", "Verificar drenaje del sustrato"]',
            created_at=datetime.now() - timedelta(days=6),
            is_shared=True
        )
        session.add(diagnosis1)
        session.flush()
        
        # Post 1 basado en diagnosis1
        post1 = CommunityPostDB(
            diagnosis_id=diagnosis1.id,
            user_id=user_id,
            is_anonymous=False,
            likes=12,
            comments_count=2,
            status="approved",
            created_at=datetime.now() - timedelta(days=6)
        )
        session.add(post1)
        session.flush()
        
        # Comentarios del post 1
        comment1_1 = CommentDB(
            post_id=post1.id,
            user_id=user_id,
            content="Es normal que las hojas viejas se pongan amarillas. Simplemente córtalas cuando estén completamente amarillas.",
            likes=5,
            created_at=datetime.now() - timedelta(days=6, hours=-2)
        )
        session.add(comment1_1)
        
        comment1_2 = CommentDB(
            post_id=post1.id,
            user_id=user_id,
            content="También verifica que no estés regando demasiado. Las Monsteras prefieren secarse un poco entre riegos.",
            likes=7,
            created_at=datetime.now() - timedelta(days=5, hours=-18)
        )
        session.add(comment1_2)
        
        print(f"   ✅ Post 1 creado: Hojas amarillas en Monstera (12 likes, 2 comentarios)")
        
        # Diagnóstico 2: Suculenta saludable
        diagnosis2 = DiagnosisDB(
            plant_id=plant.id,
            user_id=user_id,
            image_url="",
            diagnosis_text="Suculenta en excelente estado. Buen color, hojas firmes.",
            confidence=0.95,
            disease_name=None,
            severity="ninguno",
            recommendations='["Mantener riego actual", "Continuar con luz solar directa"]',
            created_at=datetime.now() - timedelta(days=7),
            is_shared=True
        )
        session.add(diagnosis2)
        session.flush()
        
        post2 = CommunityPostDB(
            diagnosis_id=diagnosis2.id,
            user_id=user_id,
            is_anonymous=False,
            likes=24,
            comments_count=3,
            status="approved",
            created_at=datetime.now() - timedelta(days=7)
        )
        session.add(post2)
        session.flush()
        
        # Comentarios del post 2
        session.add(CommentDB(
            post_id=post2.id,
            user_id=user_id,
            content="¡Qué bien! Las suculentas son más resistentes de lo que pensamos.",
            likes=3,
            created_at=datetime.now() - timedelta(days=7, hours=-3)
        ))
        session.add(CommentDB(
            post_id=post2.id,
            user_id=user_id,
            content="Totalmente de acuerdo. Yo casi mato la mía por regarla demasiado 😅",
            likes=2,
            created_at=datetime.now() - timedelta(days=6, hours=-20)
        ))
        session.add(CommentDB(
            post_id=post2.id,
            user_id=user_id,
            content="Pro tip: usa macetas con buen drenaje.",
            likes=4,
            created_at=datetime.now() - timedelta(days=6, hours=-15)
        ))
        
        print(f"   ✅ Post 2 creado: Suculenta saludable (24 likes, 3 comentarios)")
        
        # Diagnóstico 3: Manchas en tomate (anónimo)
        diagnosis3 = DiagnosisDB(
            plant_id=plant.id,
            user_id=user_id,
            image_url="",
            diagnosis_text="Manchas marrones en hojas. Posible tizón temprano.",
            confidence=0.78,
            disease_name="Tizón temprano",
            severity="moderado",
            recommendations='["Aplicar fungicida natural", "Mejorar circulación de aire", "Evitar mojar hojas al regar"]',
            created_at=datetime.now() - timedelta(days=8),
            is_shared=True
        )
        session.add(diagnosis3)
        session.flush()
        
        post3 = CommunityPostDB(
            diagnosis_id=diagnosis3.id,
            user_id=user_id,
            is_anonymous=True,  # Post anónimo
            likes=8,
            comments_count=2,
            status="approved",
            created_at=datetime.now() - timedelta(days=8)
        )
        session.add(post3)
        session.flush()
        
        # Comentarios del post 3
        session.add(CommentDB(
            post_id=post3.id,
            user_id=user_id,
            content="Podría ser tizón temprano. Prueba con bicarbonato de sodio: 1 cucharada por litro de agua.",
            likes=4,
            created_at=datetime.now() - timedelta(days=8, hours=-4)
        ))
        session.add(CommentDB(
            post_id=post3.id,
            user_id=user_id,
            content="Asegúrate de que las plantas tengan buena circulación de aire.",
            likes=3,
            created_at=datetime.now() - timedelta(days=7, hours=-22)
        ))
        
        print(f"   ✅ Post 3 creado: Manchas en tomate - Anónimo (8 likes, 2 comentarios)")
        
        session.commit()
        
        print("\n" + "=" * 60)
        print("✅ CONTENIDO DE COMUNIDAD CREADO EXITOSAMENTE")
        print("=" * 60)
        print(f"\n📊 Resumen:")
        print(f"   • 1 Planta creada/usada")
        print(f"   • 3 Diagnósticos creados")
        print(f"   • 3 Posts de comunidad")
        print(f"   • 7 Comentarios totales")
        print(f"\n   Total likes: 44")
        print(f"\n🎉 Ahora puedes ver estos posts en la app (sección Comunidad)")
        print(f"\nℹ️  NOTA: Los posts están vinculados a diagnósticos reales,")
        print(f"   por lo que aparecerán con el contenido del diagnóstico.")
        
    except Exception as e:
        session.rollback()
        print(f"\n❌ Error al crear contenido: {e}")
        import traceback
        traceback.print_exc()
    finally:
        session.close()


if __name__ == "__main__":
    print("\n" + "=" * 60)
    print("🌱 CREACIÓN DE CONTENIDO DE COMUNIDAD (VERSIÓN CORREGIDA)")
    print("=" * 60)
    print()
    create_sample_data()
    print()
