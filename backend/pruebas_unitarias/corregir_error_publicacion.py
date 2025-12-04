"""
Script para corregir el error 500 en publicación de comunidad
El problema: recommendations=[] debe ser json.dumps([])
"""

# Leer archivo
with open('app/routes/community.py', 'r', encoding='utf-8') as f:
    content = f.read()

# Encontrar y reemplazar el problema
old_code = '''        # Crear diagnóstico temporal para el post
        temp_diagnosis = DiagnosisDB(
            plant_id=0,
            user_id=user_id_int,
            image_url=image_url,
            diagnosis_text=description,
            disease_name=symptoms or "Consulta de la comunidad",
            confidence=0.0,
            severity="low",
            recommendations=[]
        )'''

new_code = '''        # Crear diagnóstico temporal para el post
        import json
        temp_diagnosis = DiagnosisDB(
            plant_id=0,
            user_id=user_id_int,
            image_url=image_url,
            diagnosis_text=description,
            disease_name=symptoms or "Consulta de la comunidad",
            confidence=0.0,
            severity="low",
            recommendations=json.dumps([])  # Debe ser string JSON, no lista
        )'''

if old_code in content:
    content = content.replace(old_code, new_code)
    print("✅ Error corregido: recommendations ahora usa json.dumps([])")
else:
    print("⚠️ No se encontró el código exacto, intentando corrección alternativa...")
    
    # Buscar solo la línea problemática
    if 'recommendations=[]' in content and 'temp_diagnosis = DiagnosisDB(' in content:
        # Agregar import json si no existe
        if 'import json' not in content:
            content = content.replace(
                'from datetime import datetime',
                'from datetime import datetime\nimport json'
            )
            print("✅ Import json agregado")
        
        # Reemplazar la línea problemática
        content = content.replace(
            '            recommendations=[]',
            '            recommendations=json.dumps([])'
        )
        print("✅ recommendations=[] reemplazado por json.dumps([])")
    else:
        print("❌ No se pudo aplicar la corrección automática")
        print("Por favor edita manualmente:")
        print("   Línea ~104: recommendations=[] → recommendations=json.dumps([])")
        exit(1)

# Guardar
with open('app/routes/community.py', 'w', encoding='utf-8') as f:
    f.write(content)

print()
print("=" * 70)
print("✅ ARCHIVO community.py CORREGIDO")
print("=" * 70)
print()
print("Problema solucionado:")
print("  • recommendations ahora se guarda como JSON string")
print("  • Esto evita el error 500 al publicar en comunidad")
print()
print("🔄 Ahora reinicia el backend:")
print("   python -m app.main")
print()
print("=" * 70)
