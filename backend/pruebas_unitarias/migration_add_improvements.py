"""
Script de migración para agregar soporte de Perfil de Comunicación (Mejora #2)
y Comparador Visual (CU-05)
"""
import sys
import os

# Agregar el directorio raíz al path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from sqlalchemy import create_engine, Column, Integer, String, text
from sqlalchemy.orm import sessionmaker
from app.models.database import Base, engine
from app.models.comparison_models import PlantComparison, ComparisonMetric
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def run_migration():
    """Ejecuta la migración de base de datos"""
    try:
        logger.info("=== Iniciando migración de base de datos ===")
        
        # 1. Agregar columna user_level a tabla users (si no existe)
        logger.info("PASO 1: Agregando columna user_level a tabla users")
        with engine.connect() as conn:
            try:
                conn.execute(text("""
                    ALTER TABLE users 
                    ADD COLUMN user_level VARCHAR(20) DEFAULT 'beginner'
                """))
                conn.commit()
                logger.info("✅ Columna user_level agregada exitosamente")
            except Exception as e:
                if "duplicate column" in str(e).lower() or "already exists" in str(e).lower():
                    logger.info("⚠️ Columna user_level ya existe, saltando...")
                else:
                    raise e
        
        # 2. Agregar columna diagnosis_count a tabla users (para tracking)
        logger.info("PASO 2: Agregando columna diagnosis_count a tabla users")
        with engine.connect() as conn:
            try:
                conn.execute(text("""
                    ALTER TABLE users 
                    ADD COLUMN diagnosis_count INTEGER DEFAULT 0
                """))
                conn.commit()
                logger.info("✅ Columna diagnosis_count agregada exitosamente")
            except Exception as e:
                if "duplicate column" in str(e).lower() or "already exists" in str(e).lower():
                    logger.info("⚠️ Columna diagnosis_count ya existe, saltando...")
                else:
                    raise e
        
        # 3. Crear tablas de comparación si no existen
        logger.info("PASO 3: Creando tablas de comparación")
        Base.metadata.create_all(bind=engine, checkfirst=True)
        logger.info("✅ Tablas plant_comparisons y comparison_metrics creadas")
        
        # 4. Actualizar contador de diagnósticos para usuarios existentes
        logger.info("PASO 4: Actualizando contadores de diagnósticos existentes")
        with engine.connect() as conn:
            conn.execute(text("""
                UPDATE users
                SET diagnosis_count = (
                    SELECT COUNT(*) 
                    FROM diagnoses 
                    WHERE diagnoses.user_id = users.id
                )
                WHERE diagnosis_count = 0
            """))
            conn.commit()
            logger.info("✅ Contadores actualizados")
        
        # 5. Asignar niveles automáticos basados en diagnosis_count
        logger.info("PASO 5: Asignando niveles automáticos a usuarios")
        with engine.connect() as conn:
            # Principiantes (0-2 diagnósticos)
            conn.execute(text("""
                UPDATE users
                SET user_level = 'beginner'
                WHERE diagnosis_count <= 2
            """))
            
            # Intermedios (3-10 diagnósticos)
            conn.execute(text("""
                UPDATE users
                SET user_level = 'intermediate'
                WHERE diagnosis_count BETWEEN 3 AND 10
            """))
            
            # Expertos (11+ diagnósticos)
            conn.execute(text("""
                UPDATE users
                SET user_level = 'expert'
                WHERE diagnosis_count >= 11
            """))
            
            conn.commit()
            logger.info("✅ Niveles asignados automáticamente")
        
        # 6. Verificar migración
        logger.info("PASO 6: Verificando migración")
        with engine.connect() as conn:
            result = conn.execute(text("SELECT user_level, COUNT(*) as count FROM users GROUP BY user_level"))
            rows = result.fetchall()
            
            logger.info("📊 Distribución de niveles de usuario:")
            for row in rows:
                level = row[0] if row[0] else "NULL"
                count = row[1]
                logger.info(f"  - {level}: {count} usuarios")
        
        logger.info("\n✅ ¡MIGRACIÓN COMPLETADA EXITOSAMENTE!")
        logger.info("\nResumen de cambios:")
        logger.info("  ✓ Columna user_level agregada a users")
        logger.info("  ✓ Columna diagnosis_count agregada a users")
        logger.info("  ✓ Tabla plant_comparisons creada")
        logger.info("  ✓ Tabla comparison_metrics creada")
        logger.info("  ✓ Niveles automáticos asignados a usuarios existentes")
        
        return True
        
    except Exception as e:
        logger.error(f"\n❌ ERROR EN MIGRACIÓN: {e}")
        logger.error("Por favor revisa el error y vuelve a intentar")
        return False


if __name__ == "__main__":
    print("\n" + "="*60)
    print("MIGRACIÓN DE BASE DE DATOS - MEJORAS #2 Y CU-05")
    print("="*60 + "\n")
    
    print("⚠️  IMPORTANTE: Esta migración modificará la base de datos")
    print("   Se recomienda hacer un backup antes de continuar\n")
    
    response = input("¿Deseas continuar? (si/no): ").lower().strip()
    
    if response in ['si', 's', 'yes', 'y']:
        success = run_migration()
        sys.exit(0 if success else 1)
    else:
        print("\n❌ Migración cancelada por el usuario")
        sys.exit(0)
