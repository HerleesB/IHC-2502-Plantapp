"""
Script para corregir imports en plants.py
"""

# Leer archivo
with open('app/routes/plants.py', 'r', encoding='utf-8') as f:
    content = f.read()

# Buscar la línea de imports de fastapi
old_import = 'from fastapi import APIRouter, Depends, HTTPException, Request'
new_import = 'from fastapi import APIRouter, Depends, HTTPException, Request, File, Form, UploadFile'

if old_import in content:
    content = content.replace(old_import, new_import)
    print("✅ Imports actualizados correctamente")
else:
    print("⚠️ No se encontró el import exacto, buscando alternativa...")
    # Buscar con regex
    import re
    pattern = r'from fastapi import ([^\n]+)'
    match = re.search(pattern, content)
    if match:
        existing_imports = match.group(1)
        if 'File' not in existing_imports:
            # Agregar File, Form, UploadFile
            new_imports = existing_imports.rstrip() + ', File, Form, UploadFile'
            content = content.replace(
                f'from fastapi import {existing_imports}',
                f'from fastapi import {new_imports}'
            )
            print("✅ Imports agregados correctamente")
        else:
            print("✅ Los imports ya existen")
    else:
        print("❌ No se pudo encontrar la línea de imports")
        exit(1)

# Guardar
with open('app/routes/plants.py', 'w', encoding='utf-8') as f:
    f.write(content)

print()
print("=" * 70)
print("✅ ARCHIVO plants.py CORREGIDO")
print("=" * 70)
print()
print("Imports agregados:")
print("  • File (para recibir archivos)")
print("  • Form (para datos de formulario)")
print("  • UploadFile (para imágenes)")
print()
print("🔄 Ahora reinicia el backend:")
print("   python -m app.main")
print()
print("=" * 70)
