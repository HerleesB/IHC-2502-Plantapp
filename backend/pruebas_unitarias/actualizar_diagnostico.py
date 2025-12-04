"""
Script para actualizar el endpoint de validación + diagnóstico
UMBRAL: 40% para diagnóstico automático
"""

# Leer archivo
with open('app/routes/diagnosis.py', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Buscar el endpoint /capture-guidance (línea 211)
start_line = None
end_line = None

for i, line in enumerate(lines):
    if '@router.post("/capture-guidance"' in line:
        start_line = i
        print(f"✅ Encontrado endpoint en línea {i+1}")
    if start_line is not None and i > start_line:
        # Buscar el siguiente @router o el final del archivo
        if line.strip().startswith('@router.'):
            end_line = i
            print(f"✅ Fin del endpoint en línea {i+1}")
            break

if start_line is None:
    print("❌ No se encontró el endpoint /capture-guidance")
    exit(1)

if end_line is None:
    end_line = len(lines)
    print(f"✅ Fin del endpoint al final del archivo (línea {end_line})")

print(f"📝 Reemplazando líneas {start_line+1} a {end_line}...")

# Código nuevo del endpoint
new_endpoint = '''@router.post("/capture-guidance")
async def get_capture_guidance(
    image: UploadFile = File(...),
    user_id: int = Form(1),
    db: Session = Depends(get_db)
):
    """
    Validar foto + Hacer diagnóstico completo
    
    MEJORA: Ahora hace DOS cosas en una sola llamada:
    1. Validación de calidad de foto (siempre)
    2. Diagnóstico completo (si calidad >= 40%)
    
    Retorna:
    - guidance: Recomendaciones para mejorar la foto
    - diagnosis: Diagnóstico de la planta (si calidad >= 40%)
    - quality_score: Puntaje de calidad de la foto (0-100)
    """
    try:
        # Leer imagen
        image_bytes = await image.read()
        logger.info(f"📸 Validando imagen capturada ({len(image_bytes)} bytes)")
        
        # 1. VALIDAR CALIDAD DE LA FOTO (SIEMPRE)
        validation_result = await validate_photo_quality(image_bytes)
        quality_score = validation_result.get("overall_quality", 0) * 100
        
        logger.info(f"📊 Calidad de foto: {quality_score:.1f}%")
        
        # 2. SI LA FOTO ES ACEPTABLE (>=40%), HACER DIAGNÓSTICO COMPLETO
        diagnosis_result = None
        if quality_score >= 40:
            logger.info(f"✅ Calidad suficiente ({quality_score:.1f}%), realizando diagnóstico completo...")
            
            # Guardar imagen temporalmente
            import uuid
            import os
            temp_filename = f"temp_{uuid.uuid4()}.jpg"
            temp_path = f"./uploads/{temp_filename}"
            
            os.makedirs("./uploads", exist_ok=True)
            
            with open(temp_path, "wb") as f:
                f.write(image_bytes)
            
            # Realizar diagnóstico completo
            diagnosis_data = await get_plant_diagnosis(image_bytes)
            
            if diagnosis_data.get("success"):
                # Guardar en base de datos
                diagnosis_db = DiagnosisDB(
                    plant_id=None,
                    user_id=user_id,
                    image_url=f"/uploads/{temp_filename}",
                    diagnosis_text=diagnosis_data.get("diagnosis_text", ""),
                    disease_name=diagnosis_data.get("disease_name", "Desconocido"),
                    confidence=diagnosis_data.get("confidence", 0.0),
                    severity=diagnosis_data.get("severity", "unknown"),
                    recommendations=json.dumps(diagnosis_data.get("recommendations", []))
                )
                
                db.add(diagnosis_db)
                db.commit()
                db.refresh(diagnosis_db)
                
                diagnosis_result = {
                    "diagnosis_id": diagnosis_db.id,
                    "diagnosis_text": diagnosis_data.get("diagnosis_text"),
                    "disease_name": diagnosis_data.get("disease_name"),
                    "confidence": diagnosis_data.get("confidence"),
                    "severity": diagnosis_data.get("severity"),
                    "recommendations": diagnosis_data.get("recommendations", [])
                }
                
                logger.info(f"✅ Diagnóstico completado: {diagnosis_db.disease_name} (ID: {diagnosis_db.id})")
        else:
            logger.info(f"⚠️ Calidad insuficiente ({quality_score:.1f}%), solo validación")
        
        # 3. PREPARAR RESPUESTA CON VALIDACIÓN + DIAGNÓSTICO (si aplica)
        response = {
            "success": True,
            "quality_score": round(quality_score, 1),
            "validation": {
                "lighting": round(validation_result.get("lighting", 0) * 100, 1),
                "focus": round(validation_result.get("focus", 0) * 100, 1),
                "distance": round(validation_result.get("distance", 0) * 100, 1),
                "angle": round(validation_result.get("angle", 0) * 100, 1)
            },
            "guidance": validation_result.get("guidance", "Foto aceptable"),
            "details": validation_result.get("details", {})
        }
        
        # Agregar diagnóstico si existe
        if diagnosis_result:
            response["diagnosis"] = diagnosis_result
            response["has_diagnosis"] = True
            response["message"] = "✅ Diagnóstico completado"
        else:
            response["has_diagnosis"] = False
            response["message"] = "⚠️ Mejora la calidad de la foto para obtener un diagnóstico preciso (mínimo 40%)"
        
        logger.info(f"✅ Respuesta completa: validación {'+ diagnóstico' if diagnosis_result else 'sin diagnóstico'}")
        
        return response
        
    except Exception as e:
        logger.error(f"❌ Error en validación de foto: {e}")
        import traceback
        traceback.print_exc()
        
        return {
            "success": False,
            "quality_score": 0,
            "has_diagnosis": False,
            "guidance": "Error al analizar la foto. Por favor intenta de nuevo.",
            "message": f"Error: {str(e)}"
        }


'''

# Reemplazar el endpoint
new_lines = lines[:start_line] + [new_endpoint] + lines[end_line:]

# Guardar
with open('app/routes/diagnosis.py', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print()
print("=" * 70)
print("✅ Archivo diagnosis.py actualizado exitosamente")
print("=" * 70)
print(f"📝 Endpoint reemplazado: líneas {start_line+1} a {end_line}")
print(f"🎯 Umbral de diagnóstico: 40%")
print()
print("FUNCIONALIDAD NUEVA:")
print("  • Validación de calidad: SIEMPRE")
print("  • Diagnóstico automático: Si calidad >= 40%")
print("  • Respuesta incluye: validation + guidance + diagnosis (opcional)")
print("=" * 70)
