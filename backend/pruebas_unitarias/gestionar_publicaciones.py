"""
Script interactivo para gestionar publicaciones
Permite ver y eliminar publicaciones fácilmente
"""
import sqlite3

def mostrar_publicaciones():
    """Muestra todas las publicaciones"""
    conn = sqlite3.connect('jardin.db')
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT 
            cp.id,
            cp.plant_name,
            cp.description,
            u.username,
            cp.is_anonymous,
            cp.likes,
            cp.comments_count
        FROM community_posts cp
        LEFT JOIN users u ON cp.user_id = u.id
        ORDER BY cp.id
    """)
    
    posts = cursor.fetchall()
    conn.close()
    
    if not posts:
        print("\n❌ No hay publicaciones")
        return False
    
    print("\n" + "=" * 80)
    print("  📋 PUBLICACIONES EN LA COMUNIDAD")
    print("=" * 80)
    
    for post in posts:
        post_id, plant, desc, username, anon, likes, comments = post
        user_display = "Anónimo" if anon else (username or "Desconocido")
        desc_short = (desc[:50] + "...") if desc and len(desc) > 50 else (desc or "Sin descripción")
        
        print(f"\nID: {post_id}")
        print(f"  🌱 Planta: {plant or 'Sin nombre'}")
        print(f"  👤 Usuario: {user_display}")
        print(f"  📝 {desc_short}")
        print(f"  ❤️  {likes} likes | 💬 {comments} comentarios")
    
    print("\n" + "=" * 80)
    return True

def eliminar_publicaciones():
    """Elimina publicaciones seleccionadas"""
    if not mostrar_publicaciones():
        return
    
    print("\n🗑️  ELIMINAR PUBLICACIONES")
    print("-" * 80)
    print("Opciones:")
    print("  • Escribe los IDs separados por comas (ej: 1,3,5)")
    print("  • Escribe 'todas' para eliminar todas")
    print("  • Escribe 'cancelar' para salir")
    print()
    
    opcion = input("¿Qué deseas eliminar? ").strip().lower()
    
    if opcion == 'cancelar':
        print("❌ Cancelado")
        return
    
    conn = sqlite3.connect('jardin.db')
    cursor = conn.cursor()
    
    try:
        if opcion == 'todas':
            confirmar = input("⚠️  ¿SEGURO que quieres eliminar TODAS las publicaciones? (si/no): ").strip().lower()
            if confirmar == 'si':
                cursor.execute("DELETE FROM community_posts")
                count = cursor.rowcount
                conn.commit()
                print(f"✅ {count} publicaciones eliminadas")
            else:
                print("❌ Cancelado")
        else:
            # Eliminar IDs específicos
            ids = [int(id.strip()) for id in opcion.split(',')]
            placeholders = ','.join('?' * len(ids))
            cursor.execute(f"DELETE FROM community_posts WHERE id IN ({placeholders})", ids)
            count = cursor.rowcount
            conn.commit()
            print(f"✅ {count} publicaciones eliminadas")
            
    except ValueError:
        print("❌ Error: Formato inválido")
    except Exception as e:
        print(f"❌ Error: {e}")
    finally:
        conn.close()

def menu():
    """Menú principal"""
    while True:
        print("\n" + "=" * 80)
        print("  🌱 GESTOR DE PUBLICACIONES - Jardín Inteligente")
        print("=" * 80)
        print("\n1. Ver todas las publicaciones")
        print("2. Eliminar publicaciones")
        print("3. Salir")
        print()
        
        opcion = input("Selecciona una opción (1-3): ").strip()
        
        if opcion == '1':
            mostrar_publicaciones()
        elif opcion == '2':
            eliminar_publicaciones()
        elif opcion == '3':
            print("\n👋 ¡Hasta luego!")
            break
        else:
            print("❌ Opción inválida")

if __name__ == "__main__":
    menu()
