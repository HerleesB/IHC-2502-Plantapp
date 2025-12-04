"""
Script para agregar columna last_activity faltante
"""
import sqlite3
from datetime import datetime

def add_missing_columns():
    """Agrega columnas faltantes a la BD"""
    conn = sqlite3.connect('jardin.db')
    cursor = conn.cursor()
    
    try:
        # Verificar si la columna existe
        cursor.execute("PRAGMA table_info(users)")
        columns = [col[1] for col in cursor.fetchall()]
        
        if 'last_activity' not in columns:
            print("Agregando columna last_activity a users...")
            cursor.execute("""
                ALTER TABLE users 
                ADD COLUMN last_activity DATETIME
            """)
            conn.commit()
            print("✅ Columna last_activity agregada")
        else:
            print("✅ Columna last_activity ya existe")
        
        # Verificar columnas de mejoras
        if 'user_level' not in columns:
            print("Agregando columna user_level a users...")
            cursor.execute("""
                ALTER TABLE users 
                ADD COLUMN user_level VARCHAR(20) DEFAULT 'beginner'
            """)
            conn.commit()
            print("✅ Columna user_level agregada")
        else:
            print("✅ Columna user_level ya existe")
        
        if 'diagnosis_count' not in columns:
            print("Agregando columna diagnosis_count a users...")
            cursor.execute("""
                ALTER TABLE users 
                ADD COLUMN diagnosis_count INTEGER DEFAULT 0
            """)
            conn.commit()
            print("✅ Columna diagnosis_count agregada")
        else:
            print("✅ Columna diagnosis_count ya existe")
        
        print("\n✅ Todas las columnas verificadas/agregadas correctamente")
        
    except sqlite3.OperationalError as e:
        print(f"❌ Error: {e}")
    finally:
        conn.close()

if __name__ == "__main__":
    print("=" * 60)
    print("  REPARACIÓN DE BASE DE DATOS - Jardín Inteligente")
    print("=" * 60)
    print()
    add_missing_columns()
    print()
    print("=" * 60)
    print("  COMPLETADO - Reinicia el backend ahora")
    print("=" * 60)
