"""
Script para verificar tabla post_likes y posibles problemas
"""
import sqlite3

conn = sqlite3.connect('jardin.db')
cursor = conn.cursor()

print("=" * 70)
print("VERIFICACIÓN DE BASE DE DATOS - COMUNIDAD")
print("=" * 70)

# 1. Verificar si existe tabla post_likes
print("\n1. Verificando tabla post_likes...")
cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='post_likes'")
result = cursor.fetchone()

if result:
    print("   ✅ Tabla post_likes existe")
    
    # Ver estructura
    cursor.execute("PRAGMA table_info(post_likes)")
    columns = cursor.fetchall()
    print("\n   Columnas:")
    for col in columns:
        print(f"      • {col[1]} ({col[2]})")
else:
    print("   ❌ Tabla post_likes NO existe")
    print("   ⚠️  Ejecuta: python migrate_likes_system.py")

# 2. Verificar columnas en community_posts
print("\n2. Verificando tabla community_posts...")
cursor.execute("PRAGMA table_info(community_posts)")
columns = cursor.fetchall()
column_names = [col[1] for col in columns]

required_columns = ['description', 'plant_name', 'symptoms', 'image_url']
missing = [col for col in required_columns if col not in column_names]

if missing:
    print(f"   ❌ Faltan columnas: {', '.join(missing)}")
    print("   ⚠️  Ejecuta: python fix_database_complete.py")
else:
    print("   ✅ Todas las columnas necesarias existen")

# 3. Contar publicaciones
cursor.execute("SELECT COUNT(*) FROM community_posts")
count = cursor.fetchone()[0]
print(f"\n3. Publicaciones en BD: {count}")

# 4. Ver últimas publicaciones
print("\n4. Últimas 3 publicaciones:")
cursor.execute("""
    SELECT id, plant_name, description, is_anonymous, likes 
    FROM community_posts 
    ORDER BY created_at DESC 
    LIMIT 3
""")
posts = cursor.fetchall()

if posts:
    for post in posts:
        print(f"\n   ID: {post[0]}")
        print(f"   Planta: {post[1] or 'Sin nombre'}")
        print(f"   Descripción: {(post[2][:50] + '...') if post[2] and len(post[2]) > 50 else (post[2] or 'Sin descripción')}")
        print(f"   Anónimo: {post[3]}")
        print(f"   Likes: {post[4]}")
else:
    print("   No hay publicaciones")

conn.close()

print("\n" + "=" * 70)
print("VERIFICACIÓN COMPLETADA")
print("=" * 70)
print("\n⚠️  IMPORTANTE: Copia los logs del backend cuando intentes publicar")
print("=" * 70)
