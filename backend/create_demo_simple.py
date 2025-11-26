"""
Script simple para crear usuario demo sin bcrypt
Ejecutar: python create_demo_simple.py
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.models.database import UserDB
import hashlib

DATABASE_URL = "sqlite:///./jardin.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
Session = sessionmaker(bind=engine)


def simple_hash(password: str) -> str:
    """Hash simple con SHA256 (solo para desarrollo)"""
    return hashlib.sha256(password.encode()).hexdigest()


def create_demo_user():
    session = Session()
    
    try:
        # Verificar si existe
        demo_user = session.query(UserDB).filter(
            UserDB.username == "demo"
        ).first()
        
        if demo_user:
            print(f"✅ Usuario demo ya existe (ID: {demo_user.id})")
            # Actualizar con hash simple
            demo_user.hashed_password = simple_hash("demo123")
            session.commit()
            print("✅ Contraseña actualizada (hash simple)")
        else:
            print("👤 Creando usuario demo...")
            demo_user = UserDB(
                email="demo@jardin.app",
                username="demo",
                full_name="Usuario Demo",
                hashed_password=simple_hash("demo123"),
                level=1,
                xp=0,
                points=0,
                streak_days=0
            )
            session.add(demo_user)
            session.commit()
            session.refresh(demo_user)
            print(f"✅ Usuario demo creado con ID: {demo_user.id}")
        
        # Verificación
        verification = session.query(UserDB).filter(
            UserDB.username == "demo"
        ).first()
        
        if verification:
            print("")
            print("=" * 60)
            print("✅ USUARIO DEMO CREADO EXITOSAMENTE")
            print("=" * 60)
            print("")
            print("📝 CREDENCIALES:")
            print("   Username: demo")
            print("   Password: demo123")
            print("")
            print("⚠️  NOTA: Se usó hash SHA256 simple (solo para desarrollo)")
            print("   Para producción, arreglar bcrypt/passlib")
            print("")
        else:
            print("❌ ERROR: Usuario no se guardó correctamente")
            
    except Exception as e:
        session.rollback()
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()
    finally:
        session.close()


if __name__ == "__main__":
    create_demo_user()
