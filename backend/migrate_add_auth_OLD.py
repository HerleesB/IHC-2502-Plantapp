"""
Script de migración para agregar autenticación
Ejecutar: python migrate_add_auth.py
"""
import sys
import os
from pathlib import Path

# Agregar el directorio app al path
sys.path.insert(0, str(Path(__file__).parent))

from sqlalchemy import create_engine, inspect, text
from app.models.database import Base, UserDB, PlantDB, DiagnosisDB, CommunityPostDB, CommentDB, AchievementDB
from app.utils.auth import get_password_hash
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

DATABASE_URL = "sqlite:///./jardin.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})


def check_table_exists(table_name: str) -> bool:
    """Verifica si una tabla existe en la base de datos"""
    inspector = inspect(engine)
    return table_name in inspector.get_table_names()


def migrate_database():
    """Ejecuta la migración completa"""
    logger.info("=" * 60)
    logger.info("🔄 INICIANDO MIGRACIÓN DE AUTENTICACIÓN")
    logger.info("=" * 60)
    
    # 1. Crear tabla users si no existe
    if not check_table_exists("users"):
        logger.info("📝 Creando tabla 'users'...")
        UserDB.__table__.create(engine)
        logger.info("✅ Tabla 'users' creada")
        
        # Crear usuario demo para migración de datos existentes
        logger.info("👤 Creando usuario demo para datos existentes...")
        from sqlalchemy.orm import sessionmaker
        Session = sessionmaker(bind=engine)
        session = Session()
        
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
        logger.info(f"✅ Usuario demo creado con ID: {demo_user.id}")
        
        # 2. Actualizar registros existentes sin user_id
        logger.info("🔄 Asignando datos existentes al usuario demo...")
        
        # Plantas sin usuario
        result = session.execute(
            text("UPDATE plants SET user_id = :user_id WHERE user_id IS NULL OR user_id = 1"),
            {"user_id": demo_user.id}
        )
        logger.info(f"✅ {result.rowcount} plantas actualizadas")
        
        # Diagnósticos sin usuario
        result = session.execute(
            text("UPDATE diagnoses SET user_id = :user_id WHERE user_id IS NULL OR user_id = 1"),
            {"user_id": demo_user.id}
        )
        logger.info(f"✅ {result.rowcount} diagnósticos actualizados")
        
        # Posts de comunidad sin usuario
        if check_table_exists("community_posts"):
            result = session.execute(
                text("UPDATE community_posts SET user_id = :user_id WHERE user_id IS NULL OR user_id = 1"),
                {"user_id": demo_user.id}
            )
            logger.info(f"✅ {result.rowcount} posts de comunidad actualizados")
        
        # Comentarios sin usuario
        if check_table_exists("comments"):
            result = session.execute(
                text("UPDATE comments SET user_id = :user_id WHERE user_id IS NULL OR user_id = 1"),
                {"user_id": demo_user.id}
            )
            logger.info(f"✅ {result.rowcount} comentarios actualizados")
        
        # Logros sin usuario
        if check_table_exists("achievements"):
            result = session.execute(
                text("UPDATE achievements SET user_id = :user_id WHERE user_id IS NULL OR user_id = 1"),
                {"user_id": demo_user.id}
            )
            logger.info(f"✅ {result.rowcount} logros actualizados")
        
        session.commit()
        session.close()
        
    else:
        logger.info("ℹ️  Tabla 'users' ya existe, saltando creación")
    
    # 3. Verificar que todas las tablas tengan las columnas necesarias
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
                logger.warning(f"   Se recomienda ejecutar Base.metadata.create_all() o recrear la BD")
            else:
                logger.info(f"✅ Tabla '{table}' tiene todas las columnas requeridas")
    
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


if __name__ == "__main__":
    try:
        migrate_database()
    except Exception as e:
        logger.error(f"❌ Error durante la migración: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
