"""
Script COMPLETO para agregar TODAS las columnas faltantes
"""
import sqlite3
from datetime import datetime

def add_all_missing_columns():
    """Agrega TODAS las columnas faltantes a la BD"""
    conn = sqlite3.connect('jardin.db')
    cursor = conn.cursor()
    
    print("=" * 60)
    print("  REPARACIÓN COMPLETA DE BASE DE DATOS")
    print("=" * 60)
    print()
    
    columns_added = 0
    
    try:
        # ========== TABLA USERS ==========
        print("📋 Verificando tabla USERS...")
        cursor.execute("PRAGMA table_info(users)")
        user_columns = [col[1] for col in cursor.fetchall()]
        
        if 'last_activity' not in user_columns:
            print("  Agregando: last_activity")
            cursor.execute("ALTER TABLE users ADD COLUMN last_activity DATETIME")
            columns_added += 1
        
        if 'user_level' not in user_columns:
            print("  Agregando: user_level")
            cursor.execute("ALTER TABLE users ADD COLUMN user_level VARCHAR(20) DEFAULT 'beginner'")
            columns_added += 1
        
        if 'diagnosis_count' not in user_columns:
            print("  Agregando: diagnosis_count")
            cursor.execute("ALTER TABLE users ADD COLUMN diagnosis_count INTEGER DEFAULT 0")
            columns_added += 1
        
        # ========== TABLA PLANTS ==========
        print("\n📋 Verificando tabla PLANTS...")
        cursor.execute("PRAGMA table_info(plants)")
        plant_columns = [col[1] for col in cursor.fetchall()]
        
        if 'location' not in plant_columns:
            print("  Agregando: location")
            cursor.execute("ALTER TABLE plants ADD COLUMN location VARCHAR(255)")
            columns_added += 1
        
        # ========== TABLA COMMUNITY_POSTS ==========
        print("\n📋 Verificando tabla COMMUNITY_POSTS...")
        cursor.execute("PRAGMA table_info(community_posts)")
        community_columns = [col[1] for col in cursor.fetchall()]
        
        if 'description' not in community_columns:
            print("  Agregando: description")
            cursor.execute("ALTER TABLE community_posts ADD COLUMN description TEXT")
            columns_added += 1
        
        if 'plant_name' not in community_columns:
            print("  Agregando: plant_name")
            cursor.execute("ALTER TABLE community_posts ADD COLUMN plant_name VARCHAR(255)")
            columns_added += 1
        
        if 'symptoms' not in community_columns:
            print("  Agregando: symptoms")
            cursor.execute("ALTER TABLE community_posts ADD COLUMN symptoms VARCHAR(500)")
            columns_added += 1
        
        if 'image_url' not in community_columns:
            print("  Agregando: image_url")
            cursor.execute("ALTER TABLE community_posts ADD COLUMN image_url VARCHAR(500)")
            columns_added += 1
        
        conn.commit()
        
        print()
        print("=" * 60)
        if columns_added > 0:
            print(f"✅ {columns_added} columnas agregadas exitosamente")
        else:
            print("✅ Todas las columnas ya existen")
        print("=" * 60)
        
    except sqlite3.OperationalError as e:
        print(f"❌ Error: {e}")
        conn.rollback()
    finally:
        conn.close()

if __name__ == "__main__":
    add_all_missing_columns()
    print("\n🎉 Reparación completada - Reinicia el backend ahora")
