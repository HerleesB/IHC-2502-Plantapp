"""
Script para reiniciar la base de datos
Elimina la base de datos existente y crea una nueva con todas las tablas
"""
import os
import sys
import hashlib

# Agregar el directorio raíz al path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

def hash_password(password: str) -> str:
    """Hash simple con SHA256 para evitar problemas con bcrypt"""
    return hashlib.sha256(password.encode()).hexdigest()

def reset_database():
    """Eliminar y recrear la base de datos"""
    db_path = "jardin.db"
    
    # Eliminar base de datos existente
    if os.path.exists(db_path):
        os.remove(db_path)
        print(f"✅ Base de datos '{db_path}' eliminada")
    
    # Importar y crear tablas
    from app.models.database import Base, engine, SessionLocal, UserDB
    
    # Crear todas las tablas
    Base.metadata.create_all(bind=engine)
    print("✅ Tablas creadas correctamente")
    
    # Crear usuarios
    db = SessionLocal()
    
    try:
        # Usuario demo
        demo_user = UserDB(
            email="demo@jardininteligente.com",
            username="demo",
            full_name="Usuario Demo",
            hashed_password=hash_password("demo123"),
            level=1,
            xp=0,
            points=0,
            streak_days=0
        )
        db.add(demo_user)
        
        # Usuario test
        test_user = UserDB(
            email="test@jardininteligente.com",
            username="test",
            full_name="Usuario Test",
            hashed_password=hash_password("test123"),
            level=1,
            xp=0,
            points=0,
            streak_days=0
        )
        db.add(test_user)
        
        db.commit()
        print("✅ Usuario demo creado: demo / demo123")
        print("✅ Usuario test creado: test / test123")
        
    except Exception as e:
        print(f"❌ Error: {e}")
        db.rollback()
    finally:
        db.close()
    
    print("\n🌱 Base de datos reiniciada correctamente")
    print("   Puedes iniciar el servidor con: uvicorn app.main:app --reload")

if __name__ == "__main__":
    reset_database()
