"""Script para reiniciar la base de datos con el nuevo schema"""
import sys
import os

# Agregar el directorio raíz al path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.models.database import Base, engine, SessionLocal, UserDB
import hashlib

def reset_database():
    """Elimina y recrea todas las tablas"""
    print("🗑️ Eliminando tablas existentes...")
    Base.metadata.drop_all(bind=engine)
    
    print("✅ Creando nuevas tablas...")
    Base.metadata.create_all(bind=engine)
    
    print("👤 Creando usuario demo...")
    db = SessionLocal()
    try:
        # Hash simple con SHA256
        password_hash = hashlib.sha256("demo123".encode()).hexdigest()
        
        demo_user = UserDB(
            email="demo@jardininteligente.com",
            username="demo",
            full_name="Usuario Demo",
            hashed_password=password_hash,
            level=1,
            xp=0,
            points=0,
            streak_days=0
        )
        db.add(demo_user)
        db.commit()
        print(f"✅ Usuario demo creado con ID: {demo_user.id}")
    except Exception as e:
        print(f"⚠️ Error creando usuario demo: {e}")
        db.rollback()
    finally:
        db.close()
    
    print("\n🎉 Base de datos reiniciada exitosamente!")
    print("📋 Tablas creadas:")
    for table in Base.metadata.sorted_tables:
        print(f"   - {table.name}")

if __name__ == "__main__":
    confirm = input("⚠️ Esto eliminará TODOS los datos. ¿Continuar? (s/n): ")
    if confirm.lower() == 's':
        reset_database()
    else:
        print("Operación cancelada.")
