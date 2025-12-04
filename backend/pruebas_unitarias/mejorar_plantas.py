"""
Script para mejorar endpoints de plantas
1. Mejora DELETE para incluir información de advertencia
2. Agrega PUT update-with-diagnosis
"""

# Leer archivo
with open('app/routes/plants.py', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# ========================================
# 1. MEJORAR ENDPOINT DELETE
# ========================================

delete_start = None
delete_end = None

for i, line in enumerate(lines):
    if '@router.delete("/{plant_id}")' in line:
        delete_start = i
        print(f"✅ Encontrado DELETE endpoint en línea {i+1}")
    if delete_start is not None and i > delete_start:
        if line.strip().startswith('@router.'):
            delete_end = i
            print(f"✅ Fin del DELETE en línea {i+1}")
            break

if delete_start is None:
    print("❌ No se encontró el endpoint DELETE")
    exit(1)

if delete_end is None:
    delete_end = len(lines)

# Nuevo código para DELETE mejorado
new_delete = '''@router.delete("/{plant_id}")
async def delete_plant(plant_id: int, user_id: int, db: Session = Depends(get_db)):
    """
    CU-04: Eliminar planta
    
    ADVERTENCIA: Esto eliminará permanentemente:
    - La planta
    - Todos sus diagnósticos
    - Todo su historial de riego
    - Todas las fotos asociadas
    - Todo el progreso de esta planta
    """
    import logging
    logger = logging.getLogger(__name__)
    
    # Buscar la planta
    plant = db.query(PlantDB).filter(
        PlantDB.id == plant_id,
        PlantDB.user_id == user_id
    ).first()
    
    if not plant:
        raise HTTPException(404, "Planta no encontrada o no tienes permiso para eliminarla")
    
    plant_name = plant.name
    
    # Contar diagnósticos asociados para información
    diagnoses_count = db.query(DiagnosisDB).filter(
        DiagnosisDB.plant_id == plant_id
    ).count()
    
    logger.info(f"🗑️ Eliminando planta '{plant_name}' (ID: {plant_id}) con {diagnoses_count} diagnósticos")
    
    # Eliminar diagnósticos asociados (por seguridad, aunque CASCADE debería hacerlo)
    db.query(DiagnosisDB).filter(DiagnosisDB.plant_id == plant_id).delete()
    
    # Eliminar la planta
    db.delete(plant)
    db.commit()
    
    logger.info(f"✅ Planta '{plant_name}' eliminada exitosamente")
    
    return {
        "success": True,
        "message": f"Planta '{plant_name}' y todo su historial eliminados exitosamente",
        "plant_name": plant_name,
        "diagnoses_deleted": diagnoses_count
    }


'''

# Reemplazar DELETE
lines = lines[:delete_start] + [new_delete] + lines[delete_end:]

print(f"✅ DELETE mejorado (líneas {delete_start+1} a {delete_end})")

# ========================================
# 2. AGREGAR NUEVO ENDPOINT UPDATE-WITH-DIAGNOSIS
# ========================================

# Buscar donde insertar (después del endpoint PUT /{plant_id}/fertilize)
insert_position = None

for i, line in enumerate(lines):
    if '@router.put("/{plant_id}/fertilize")' in line:
        # Buscar el final de este endpoint
        for j in range(i+1, len(lines)):
            if lines[j].strip().startswith('@router.') or lines[j].strip().startswith('# ==='):
                insert_position = j
                break
        if insert_position is None:
            insert_position = len(lines)
        break

if insert_position is None:
    # Si no encontró fertilize, agregar al final
    insert_position = len(lines)

print(f"📍 Insertando nuevo endpoint en línea {insert_position+1}")

# Nuevo endpoint para actualizar planta con diagnóstico
new_update_diagnosis = '''

@router.put("/{plant_id}/update-with-diagnosis")
async def update_plant_with_diagnosis(
    plant_id: int,
    image: UploadFile = File(...),
    user_id: int = Form(...),
    request: Request = None,
    db: Session = Depends(get_db)
):
    """
    Actualizar planta con nuevo diagnóstico e imagen
    
    Permite:
    - Actualizar la foto principal de la planta
    - Agregar un nuevo diagnóstico al historial
    - Actualizar el estado de salud de la planta
    - Actualizar health_score basado en el nuevo diagnóstico
    """
    import logging
    import uuid
    import os
    logger = logging.getLogger(__name__)
    
    try:
        # Buscar la planta
        plant = db.query(PlantDB).filter(
            PlantDB.id == plant_id,
            PlantDB.user_id == user_id
        ).first()
        
        if not plant:
            raise HTTPException(404, "Planta no encontrada o no tienes permiso")
        
        logger.info(f"📸 Actualizando planta '{plant.name}' (ID: {plant_id}) con nuevo diagnóstico")
        
        # Leer imagen
        image_bytes = await image.read()
        
        # Guardar imagen
        temp_filename = f"plant_{plant_id}_{uuid.uuid4()}.jpg"
        temp_path = f"./uploads/{temp_filename}"
        
        os.makedirs("./uploads", exist_ok=True)
        
        with open(temp_path, "wb") as f:
            f.write(image_bytes)
        
        image_url = f"/uploads/{temp_filename}"
        
        # Realizar diagnóstico
        from app.services.groq_service import get_plant_diagnosis
        diagnosis_data = await get_plant_diagnosis(image_bytes)
        
        if not diagnosis_data.get("success"):
            raise HTTPException(400, "Error al analizar la imagen")
        
        # Crear nuevo diagnóstico
        import json
        diagnosis_db = DiagnosisDB(
            plant_id=plant_id,
            user_id=user_id,
            image_url=image_url,
            diagnosis_text=diagnosis_data.get("diagnosis_text", ""),
            disease_name=diagnosis_data.get("disease_name", "Desconocido"),
            confidence=diagnosis_data.get("confidence", 0.0),
            severity=diagnosis_data.get("severity", "unknown"),
            recommendations=json.dumps(diagnosis_data.get("recommendations", []))
        )
        
        db.add(diagnosis_db)
        
        # Actualizar la planta con la nueva imagen y estado
        plant.image_url = image_url
        plant.status = diagnosis_data.get("severity", "unknown")
        
        # Actualizar health_score basado en severity
        severity_to_health = {
            "healthy": 100,
            "low": 80,
            "moderate": 60,
            "high": 40,
            "critical": 20,
            "unknown": 70
        }
        plant.health_score = severity_to_health.get(
            diagnosis_data.get("severity", "unknown"),
            70
        )
        
        from datetime import datetime
        plant.last_diagnosis = datetime.utcnow()
        
        db.commit()
        db.refresh(diagnosis_db)
        db.refresh(plant)
        
        logger.info(f"✅ Planta '{plant.name}' actualizada. Nuevo diagnóstico ID: {diagnosis_db.id}")
        
        # Construir URL completa para la imagen
        full_image_url = get_full_image_url(plant.image_url, request) if request else plant.image_url
        
        return {
            "success": True,
            "message": f"Planta '{plant.name}' actualizada exitosamente",
            "diagnosis": {
                "diagnosis_id": diagnosis_db.id,
                "diagnosis_text": diagnosis_data.get("diagnosis_text"),
                "disease_name": diagnosis_data.get("disease_name"),
                "confidence": diagnosis_data.get("confidence"),
                "severity": diagnosis_data.get("severity"),
                "recommendations": diagnosis_data.get("recommendations", [])
            },
            "plant": {
                "id": plant.id,
                "name": plant.name,
                "species": plant.species,
                "image_url": full_image_url,
                "status": plant.status,
                "health_score": plant.health_score,
                "last_diagnosis": plant.last_diagnosis.isoformat() if plant.last_diagnosis else None,
                "created_at": plant.created_at.isoformat() if plant.created_at else None
            }
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Error al actualizar planta: {e}")
        import traceback
        traceback.print_exc()
        db.rollback()
        raise HTTPException(500, f"Error al actualizar planta: {str(e)}")


'''

# Insertar el nuevo endpoint
lines.insert(insert_position, new_update_diagnosis)

print(f"✅ Nuevo endpoint UPDATE-WITH-DIAGNOSIS agregado")

# Guardar archivo
with open('app/routes/plants.py', 'w', encoding='utf-8') as f:
    f.writelines(lines)

print()
print("=" * 70)
print("✅ ARCHIVO plants.py ACTUALIZADO EXITOSAMENTE")
print("=" * 70)
print()
print("📝 Cambios realizados:")
print("   1. ✅ DELETE mejorado con advertencia de eliminación completa")
print("   2. ➕ Nuevo endpoint PUT /{plant_id}/update-with-diagnosis")
print()
print("🎯 Nuevas funcionalidades:")
print("   • Eliminar planta con advertencia detallada")
print("   • Actualizar planta con nuevo diagnóstico e imagen")
print("   • Actualizar health_score automáticamente")
print("   • Guardar nuevo diagnóstico en historial")
print()
print("=" * 70)
print()
print("🔄 Ahora reinicia el backend:")
print("   python -m app.main")
print()
print("=" * 70)
