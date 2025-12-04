"""
Script para agregar configuración de archivos estáticos en main.py
"""

# Leer archivo
with open('app/main.py', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Buscar donde crear la app
app_creation_line = None
for i, line in enumerate(lines):
    if 'app = FastAPI(' in line:
        app_creation_line = i
        # Buscar el cierre del paréntesis
        for j in range(i, min(i+10, len(lines))):
            if ')' in lines[j]:
                app_creation_line = j
                break
        break

if app_creation_line is None:
    print("❌ No se encontró la creación de app = FastAPI()")
    exit(1)

print(f"✅ Encontrado app = FastAPI() en línea {app_creation_line + 1}")

# Buscar si ya existe la configuración de uploads
has_uploads_mount = any('app.mount("/uploads"' in line for line in lines)

if has_uploads_mount:
    print("✅ La configuración de /uploads ya existe")
else:
    print("➕ Agregando configuración de archivos estáticos...")
    
    # Código a insertar después de la creación de app
    static_config = '''
# Servir archivos estáticos (imágenes subidas)
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")

'''
    
    # Insertar después de la creación de app
    lines.insert(app_creation_line + 1, static_config)
    
    # Guardar
    with open('app/main.py', 'w', encoding='utf-8') as f:
        f.writelines(lines)
    
    print("✅ Configuración agregada")

print()
print("=" * 70)
print("✅ ARCHIVOS ESTÁTICOS CONFIGURADOS")
print("=" * 70)
print()
print("Ahora el servidor podrá servir imágenes desde:")
print("  • http://localhost:8000/uploads/community/imagen.jpg")
print("  • http://localhost:8000/uploads/plants/imagen.jpg")
print("  • http://localhost:8000/uploads/diagnosis/imagen.jpg")
print()
print("🔄 Reinicia el backend:")
print("   python -m app.main")
print()
print("=" * 70)
