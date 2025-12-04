"""
Script de migración para agregar autenticación
Ejecutar: python scripts/migrate_add_auth.py
"""
import sys
from pathlib import Path

# Ajustar sys.path para importar desde directorio padre
sys.path.insert(0, str(Path(__file__).parent.parent))

from sqlalchemy import create_engine, inspect, text
from sqlalchemy.orm import sessionmaker
from app.models.database import (
    Base,
    UserDB,
    PlantDB,
    DiagnosisDB,
    CommunityPostDB,
    CommentDB,
    AchievementDB
)
from app.utils.auth import get_password_hash
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

DATABASE_URL = "sqlite:///./jardin.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
Session = sessionmaker(bind=engine)


def check_table_exists(table_name: str) -> bool:
    """Verifica si una tabla existe en la base de datos"""
    inspector = inspect(engine)
    return table_name in inspector.get_table_names()


def create_or_update_demo_user(session):
    """Crea o actualiza el usuario demo - CORRIGE BUG ORIGINAL"""
    demo_user = session.query(UserDB).filter(
        (UserDB.username == "demo") | (UserDB.email == "demo@jardin.app")
    ).first()
    
    if demo_user:
        logger.info(f"ℹ️  Usuario demo ya existe (ID: {demo_user.id})")
        demo_user.hashed_password = get_password_hash("demo123")
        session.commit()
        logger.info("✅ Contraseña actualizada")
        return demo_user
    else:
        logger.info("👤 Creando usuario demo...")
        demo_user = UserDB(
            email="demo@jardin.app",
            username="demo",
            full_name="Usuario Demo",
            hashed_password=get_password_hash("demo123"),
            level=1,
            xp=0,
            points=0,
            streak_days=0
        )
        session.add(demo_user)
        session.commit()
        session.refresh(demo_user)
        logger.info(f"✅ Usuario demo creado con ID: {demo_user.id}")
        return demo_user


def migrate_existing_data(session, user_id: int):
    """Asigna datos existentes sin user_id al usuario demo"""
    logger.info("🔄 Asignando datos existentes al usuario demo...")
    
    # Plantas sin usuario
    result = session.execute(
        text("UPDATE plants SET user_id = :user_id WHERE user_id IS NULL"),
        {"user_id": user_id}
    )
    if result.rowcount > 0:
        logger.info(f"✅ {result.rowcount} plantas asignadas")
    
    # Diagnósticos sin usuario
    result = session.execute(
        text("UPDATE diagnoses SET user_id = :user_id WHERE user_id IS NULL"),
        {"user_id": user_id}
    )
    if result.rowcount > 0:
        logger.info(f"✅ {result.rowcount} diagnósticos asignados")
    
    # Posts de comunidad sin usuario
    if check_table_exists("community_posts"):
        result = session.execute(
            text("UPDATE community_posts SET user_id = :user_id WHERE user_id IS NULL"),
            {"user_id": user_id}
        )
        if result.rowcount > 0:
            logger.info(f"✅ {result.rowcount} posts asignados")
    
    # Comentarios sin usuario
    if check_table_exists("comments"):
        result = session.execute(
            text("UPDATE comments SET user_id = :user_id WHERE user_id IS NULL"),
            {"user_id": user_id}
        )
        if result.rowcount > 0:
            logger.info(f"✅ {result.rowcount} comentarios asignados")
    
    # Logros sin usuario
    if check_table_exists("achievements"):
        result = session.execute(
            text("UPDATE achievements SET user_id = :user_id WHERE user_id IS NULL"),
            {"user_id": user_id}
        )
        if result.rowcount > 0:
            logger.info(f"✅ {result.rowcount} logros asignados")
    
    session.commit()


def verify_table_structure():
    """Verifica que todas las tablas tengan las columnas necesarias"""
    logger.info("🔍 Verificando estructura de tablas...")
    inspector = inspect(engine)
    
    tables_to_check = {
        "plants": ["user_id"],
        "diagnoses": ["user_id"],
        "community_posts": ["user_id"],
        "comments": ["user_id"],
        "achievements": ["user_id"]
    }
    
    for table, columns in tables_to_check.items():
        if check_table_exists(table):
            existing_columns = [col["name"] for col in inspector.get_columns(table)]
            missing = [col for col in columns if col not in existing_columns]
            if missing:
                logger.warning(f"⚠️  Tabla '{table}' no tiene columnas: {missing}")
                logger.warning("   Se recomienda ejecutar Base.metadata.create_all()")
            else:
                logger.info(f"✅ Tabla '{table}' tiene todas las columnas requeridas")


def migrate_database():
    """Ejecuta la migración completa"""
    logger.info("=" * 60)
    logger.info("🔄 INICIANDO MIGRACIÓN DE AUTENTICACIÓN")
    logger.info("=" * 60)
    
    session = Session()
    
    try:
        # 1. Crear tabla users si no existe
        if not check_table_exists("users"):
            logger.info("📝 Creando tabla 'users'...")
            Base.metadata.create_all(engine)
            logger.info("✅ Tabla 'users' creada")
        else:
            logger.info("ℹ️  Tabla 'users' ya existe")
        
        # 2. Crear o actualizar usuario demo (SIEMPRE - CORRIGE BUG)
        demo_user = create_or_update_demo_user(session)
        
        # 3. Migrar datos existentes
        migrate_existing_data(session, demo_user.id)
        
        # 4. Verificar estructura de tablas
        verify_table_structure()
        
        # 5. Verificación final
        verification = session.query(UserDB).filter(
            UserDB.username == "demo"
        ).first()
        
        if verification:
            logger.info("")
            logger.info("=" * 60)
            logger.info("✅ MIGRACIÓN COMPLETADA")
            logger.info("=" * 60)
            logger.info("")
            logger.info("📝 CREDENCIALES DEL USUARIO DEMO:")
            logger.info("   Email: demo@jardin.app")
            logger.info("   Username: demo")
            logger.info("   Password: demo123")
            logger.info("")
            logger.info("🔐 Para producción, cambiar SECRET_KEY en .env:")
            logger.info("   SECRET_KEY=tu-clave-secreta-super-segura-aleatoria")
            logger.info("")
        else:
            logger.error("❌ ERROR: Usuario demo no existe después de la migración")
            raise Exception("Usuario demo no se guardó correctamente")
        
    except Exception as e:
        session.rollback()
        raise e
    finally:
        session.close()


if __name__ == "__main__":
    try:
        migrate_database()
    except Exception as e:
        logger.error(f"❌ Error durante la migración: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
