"""
Script para verificar dónde se guardan las imágenes de comunidad
"""
import sqlite3
import os

# Verificar últimas publicaciones
conn = sqlite3.connect('jardin.db')
cursor = conn.cursor()

print("=" * 70)
print("DIAGNÓSTICO: IMÁGENES EN COMUNIDAD")
print("=" * 70)

# Ver últimas 3 publicaciones con sus URLs
cursor.execute("""
    SELECT id, plant_name, image_url, created_at 
    FROM community_posts 
    ORDER BY id DESC 
    LIMIT 3
""")

posts = cursor.fetchall()

print("\n📋 Últimas 3 publicaciones:")
for post in posts:
    print(f"\nID: {post[0]}")
    print(f"Planta: {post[1]}")
    print(f"URL en BD: {post[2]}")
    print(f"Fecha: {post[3]}")

conn.close()

# Verificar si el directorio uploads/community existe
print("\n" + "=" * 70)
print("📁 VERIFICACIÓN DE DIRECTORIOS")
print("=" * 70)

dirs_to_check = [
    './uploads',
    './uploads/community',
    '../uploads',
    '../uploads/community',
]

for dir_path in dirs_to_check:
    if os.path.exists(dir_path):
        print(f"\n✅ Existe: {dir_path}")
        # Listar archivos
        try:
            files = os.listdir(dir_path)
            if files:
                print(f"   Archivos ({len(files)}):")
                for f in files[:5]:  # Mostrar primeros 5
                    print(f"      • {f}")
            else:
                print("   (vacío)")
        except:
            pass
    else:
        print(f"\n❌ NO existe: {dir_path}")

print("\n" + "=" * 70)
