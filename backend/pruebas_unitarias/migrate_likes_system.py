"""
Migración: Sistema de Likes Único por Usuario
Evita que un usuario pueda dar múltiples likes al mismo post
"""
import sqlite3
from datetime import datetime

def migrate_likes_system():
    """Crea tabla post_likes para rastrear likes únicos"""
    conn = sqlite3.connect('jardin.db')
    cursor = conn.cursor()
    
    print("=" * 70)
    print("  🔧 MIGRACIÓN: Sistema de Likes Único")
    print("=" * 70)
    print()
    
    try:
        # Verificar si la tabla ya existe
        cursor.execute("""
            SELECT name FROM sqlite_master 
            WHERE type='table' AND name='post_likes'
        """)
        
        if cursor.fetchone():
            print("⏭️  Tabla post_likes ya existe")
        else:
            # Crear tabla post_likes
            cursor.execute("""
                CREATE TABLE post_likes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    post_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    UNIQUE(post_id, user_id)
                )
            """)
            
            # Crear índices para mejor performance
            cursor.execute("""
                CREATE INDEX idx_post_likes_post ON post_likes(post_id)
            """)
            cursor.execute("""
                CREATE INDEX idx_post_likes_user ON post_likes(user_id)
            """)
            
            conn.commit()
            print("✅ Tabla post_likes creada")
            print("✅ Índices creados")
            print()
            print("📋 Estructura:")
            print("   • id: ID único del like")
            print("   • post_id: ID de la publicación")
            print("   • user_id: ID del usuario")
            print("   • created_at: Fecha del like")
            print("   • UNIQUE(post_id, user_id): Evita likes duplicados")
        
        print()
        print("=" * 70)
        print("  ✅ MIGRACIÓN COMPLETADA")
        print("=" * 70)
        print()
        print("🔄 Ahora debes actualizar el backend para usar esta tabla")
        
    except sqlite3.Error as e:
        print(f"❌ Error: {e}")
        conn.rollback()
    finally:
        conn.close()

if __name__ == "__main__":
    migrate_likes_system()
