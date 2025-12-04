"""
ACTUALIZACIÓN DE RUTAS DE DIAGNÓSTICO
Integra Mejora #1 (validación rápida) y Mejora #2 (adaptador de comunicación)
"""

# ========== AGREGAR AL INICIO DEL ARCHIVO diagnosis.py ==========

from app.services.communication_adapter import (
    CommunicationAdapter, 
    UserLevel, 
    adapt_full_diagnosis
)
from app.services.groq_service import validate_photo_quality_fast  # Nueva función


# ========== NUEVO ENDPOINT: Validación Rápida para Streaming ==========

@router.post("/validate-fast")
async def validate_photo_fast(
    image: UploadFile = File(...),
    user_id: int = Form(1)
):
    """
    MEJORA #1: Endpoint de validación RÁPIDA para streaming en tiempo real.
    Optimizado para < 2 segundos de respuesta.
    
    Args:
        image: Imagen capturada por la cámara
        user_id: ID del usuario (para logging)
    
    Returns:
        Resultado de validación con guía de voz
    """
    try:
        # Leer bytes de la imagen
        image_bytes = await image.read()
        
        # Validar con función rápida (< 2 segundos)
        result = await validate_photo_quality_fast(image_bytes)
        
        logger.info(f"Validación rápida para user {user_id}: {result['success']}")
        
        return {
            "success": result["success"],
            "guidance": result["guidance"],
            "details": result["details"]
        }
        
    except Exception as e:
        logger.error(f"Error en validación rápida: {e}")
        return {
            "success": False,
            "guidance": "Error al validar foto. Intenta de nuevo.",
            "details": {
                "lighting": 0.0,
                "focus": 0.0,
                "distance": 0.0,
                "overall": 0.0
            }
        }


# ========== MODIFICACIÓN DE ENDPOINT EXISTENTE: /analyze ==========

@router.post("/analyze", response_model=DiagnosisResponse)
async def analyze_plant(
    request: Request,
    plant_id: int = Form(0),
    image: UploadFile = File(...),
    symptoms: Optional[str] = Form(None),
    user_id: int = Form(1),
    db: Session = Depends(get_db)
):
    """
    CU-02: Diagnóstico automático + explicación LLM
    ACTUALIZADO: Ahora incluye adaptación de comunicación (Mejora #2)
    """
    # Si plant_id es 0, es un diagnóstico sin planta asociada
    if plant_id > 0:
        plant = db.query(PlantDB).filter(PlantDB.id == plant_id).first()
        if not plant:
            raise HTTPException(404, "Planta no encontrada")
    
    # Guardar imagen
    image_data = await image.read()
    image_path = await save_image(image_data, plant_id if plant_id > 0 else user_id)
    
    logger.info(f"Imagen guardada en: {image_path}")
    
    # Obtener diagnóstico de Groq
    diagnosis_data = await get_plant_diagnosis(image_path, symptoms)
    
    # ========== MEJORA #2: Adaptar comunicación según nivel de usuario ==========
    
    # Obtener nivel de usuario actual
    from sqlalchemy import func
    diagnosis_count = db.query(func.count(DiagnosisDB.id)).filter(
        DiagnosisDB.user_id == user_id
    ).scalar() or 0
    
    # Detectar nivel automáticamente
    user_level = CommunicationAdapter.detect_user_level(diagnosis_count)
    
    # Adaptar todo el diagnóstico al nivel del usuario
    diagnosis_data = adapt_full_diagnosis(diagnosis_data, user_level)
    
    logger.info(f"Diagnóstico adaptado para nivel {user_level.value} (count: {diagnosis_count})")
    
    # ========== FIN DE MEJORA #2 ==========
    
    # Guardar en DB
    diagnosis = DiagnosisDB(
        plant_id=plant_id if plant_id > 0 else None,
        user_id=user_id,
        image_url=image_path,
        diagnosis_text=diagnosis_data["diagnosis"],
        confidence=diagnosis_data["confidence"],
        disease_name=diagnosis_data.get("disease_name"),
        severity=diagnosis_data["severity"],
        recommendations=json.dumps(diagnosis_data["recommendations"]),
        weekly_plan=json.dumps(diagnosis_data.get("weekly_plan", []))
    )
    
    db.add(diagnosis)
    db.commit()
    db.refresh(diagnosis)
    
    # Incrementar contador de diagnósticos del usuario
    db.execute(text("""
        UPDATE users 
        SET diagnosis_count = diagnosis_count + 1 
        WHERE id = :user_id
    """), {"user_id": user_id})
    db.commit()
    
    # Convertir rutas a URLs completas
    full_image_url = get_full_image_url(image_path, request)
    
    # Parsear weekly_plan si existe
    weekly_plan = []
    if diagnosis.weekly_plan:
        try:
            weekly_plan = json.loads(diagnosis.weekly_plan)
        except:
            weekly_plan = []
    
    logger.info(f"Diagnóstico #{diagnosis.id} creado para user {user_id}")
    
    return DiagnosisResponse(
        diagnosis_id=diagnosis.id,
        plant_id=plant_id if plant_id > 0 else None,
        diagnosis=diagnosis_data["diagnosis"],
        confidence=diagnosis_data["confidence"],
        severity=diagnosis_data["severity"],
        disease_name=diagnosis_data.get("disease_name"),
        recommendations=diagnosis_data["recommendations"],
        weekly_plan=weekly_plan,
        image_url=full_image_url,
        timestamp=diagnosis.created_at.isoformat(),
        user_level=diagnosis_data.get("user_level"),  # Nuevo campo
        level_badge=diagnosis_data.get("level_badge"),  # Nuevo campo
        educational_tips=diagnosis_data.get("educational_tips", [])  # Nuevo campo
    )


# ========== NUEVO ENDPOINT: Obtener perfil de comunicación del usuario ==========

@router.get("/communication-profile/{user_id}")
async def get_communication_profile(
    user_id: int,
    db: Session = Depends(get_db)
):
    """
    MEJORA #2: Obtener perfil de comunicación del usuario.
    
    Returns:
        Nivel actual, diagnósticos realizados, y próximo nivel
    """
    from sqlalchemy import func
    
    # Contar diagnósticos del usuario
    diagnosis_count = db.query(func.count(DiagnosisDB.id)).filter(
        DiagnosisDB.user_id == user_id
    ).scalar() or 0
    
    # Detectar nivel
    user_level = CommunicationAdapter.detect_user_level(diagnosis_count)
    
    # Calcular diagnósticos para próximo nivel
    if user_level == UserLevel.BEGINNER:
        next_level = "Intermedio"
        diagnoses_needed = 3 - diagnosis_count
    elif user_level == UserLevel.INTERMEDIATE:
        next_level = "Experto"
        diagnoses_needed = 11 - diagnosis_count
    else:
        next_level = "Experto (ya alcanzado)"
        diagnoses_needed = 0
    
    return {
        "user_id": user_id,
        "current_level": user_level.value,
        "level_badge": CommunicationAdapter.get_level_badge(user_level),
        "diagnosis_count": diagnosis_count,
        "next_level": next_level,
        "diagnoses_needed_for_next": max(0, diagnoses_needed),
        "level_description": get_level_description(user_level)
    }


def get_level_description(level: UserLevel) -> str:
    """Retorna descripción del nivel"""
    descriptions = {
        UserLevel.BEGINNER: "Estás comenzando tu viaje en jardinería. Los términos se simplifican para ti.",
        UserLevel.INTERMEDIATE: "Ya tienes experiencia. Recibes términos técnicos con explicaciones.",
        UserLevel.EXPERT: "Eres un experto jardinero. Recibes información técnica completa."
    }
    return descriptions.get(level, "")


# ========== INSTRUCCIONES DE INSTALACIÓN ==========

"""
PASOS PARA INTEGRAR ESTAS ACTUALIZACIONES:

1. Copiar las importaciones al inicio de diagnosis.py:
   - from app.services.communication_adapter import ...
   - from app.services.groq_service import validate_photo_quality_fast

2. Agregar el nuevo endpoint /validate-fast después de las funciones auxiliares

3. MODIFICAR el endpoint /analyze existente:
   - Buscar la línea: diagnosis_data = await get_plant_diagnosis(image_path, symptoms)
   - Agregar después las líneas marcadas con "MEJORA #2"

4. Agregar el nuevo endpoint /communication-profile/{user_id} al final

5. Actualizar el modelo DiagnosisResponse en schemas.py para incluir:
   - user_level: Optional[str] = None
   - level_badge: Optional[str] = None  
   - educational_tips: List[str] = []

6. Reiniciar el servidor backend

7. Probar con:
   curl -X POST http://localhost:8000/api/diagnosis/validate-fast -F "image=@test.jpg"
   curl http://localhost:8000/api/diagnosis/communication-profile/1
"""
