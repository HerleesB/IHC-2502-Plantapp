"""
Script para estandarizar URLs de imágenes en comunidad
"""
import sqlite3

conn = sqlite3.connect('jardin.db')
cursor = conn.cursor()

print("=" * 70)
print("ESTANDARIZACIÓN DE URLs DE IMÁGENES")
print("=" * 70)

# 1. Ver URLs actuales
cursor.execute("SELECT id, image_url FROM community_posts WHERE image_url LIKE '/uploads/%'")
posts_with_slash = cursor.fetchall()

if posts_with_slash:
    print(f"\n📋 Publicaciones con / inicial: {len(posts_with_slash)}")
    for post_id, url in posts_with_slash:
        new_url = url.lstrip('/')
        print(f"   ID {post_id}: {url} → {new_url}")
        cursor.execute("UPDATE community_posts SET image_url = ? WHERE id = ?", (new_url, post_id))
    
    conn.commit()
    print("\n✅ URLs actualizadas (/ inicial removido)")
else:
    print("\n✅ Todas las URLs ya están correctas (sin / inicial)")

# 2. Verificar resultado
cursor.execute("SELECT id, plant_name, image_url FROM community_posts ORDER BY id DESC LIMIT 5")
posts = cursor.fetchall()

print("\n" + "=" * 70)
print("URLs ACTUALES (últimas 5 publicaciones):")
print("=" * 70)
for post_id, plant, url in posts:
    print(f"\nID {post_id}: {plant}")
    print(f"   URL: {url}")

conn.close()

print("\n" + "=" * 70)
print("✅ ESTANDARIZACIÓN COMPLETADA")
print("=" * 70)
print()
print("📱 En tu app Android, construye las URLs así:")
print('   val imageUrl = "http://${API_BASE_URL}/${post.image_url}"')
print('   // Ejemplo: http://192.168.18.213:8000/uploads/community/xxx.jpg')
print()
print("=" * 70)
