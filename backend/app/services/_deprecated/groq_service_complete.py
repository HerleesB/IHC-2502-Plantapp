"""Funciones adicionales para groq_service.py - Agregar al archivo principal"""
import json
import logging
from typing import Dict, Any
from app.services.groq_service import GroqService
from app.utils.prompts import DiagnosisPrompts

logger = logging.getLogger(__name__)

# ========== AGREGAR ESTAS FUNCIONES AL ARCHIVO groq_service.py ==========

async def get_plant_diagnosis(image_path: str, symptoms: str | None = None) -> Dict[str, Any]:
    """
    CU-02: Obtener diagnóstico completo de planta con análisis de imagen.
    
    Args:
        image_path: Ruta al archivo de imagen
        symptoms: Síntomas adicionales reportados por el usuario
    
    Returns:
        Diccionario con diagnóstico completo
    """
    service = GroqService()
    
    # Leer imagen del disco
    try:
        with open(image_path, "rb") as f:
            image_bytes = f.read()
    except Exception as e:
        logger.error(f"Error leyendo imagen {image_path}: {e}")
        return {
            "diagnosis": f"Error al leer imagen: {str(e)}",
            "confidence": 0.0,
            "severity": "unknown",
            "disease_name": None,
            "recommendations": [],
            "weekly_plan": []
        }
    
    # Obtener prompt de diagnóstico
    prompt = DiagnosisPrompts.get_diagnosis_prompt()
    if symptoms:
        prompt += f"\n\nSÍNTOMAS ADICIONALES REPORTADOS POR EL USUARIO: {symptoms}"
    
    # Analizar con Groq
    logger.info(f"Iniciando diagnóstico de planta desde {image_path}")
    result = await service.analyze_image_with_prompt(
        image_bytes=image_bytes,
        prompt=prompt,
        temperature=0.7,
        max_tokens=2048
    )
    
    if not result.get("success"):
        logger.error(f"Error en análisis de Groq: {result.get('error')}")
        return {
            "diagnosis": "Error al analizar la imagen. Por favor intenta nuevamente.",
            "confidence": 0.0,
            "severity": "unknown",
            "disease_name": None,
            "recommendations": [],
            "weekly_plan": []
        }
    
    # Parsear respuesta JSON
    try:
        diagnosis_data = json.loads(result["content"])
        
        # Extraer información clave
        species = diagnosis_data.get("species", {})
        health_score = diagnosis_data.get("health_score", 50)
        status = diagnosis_data.get("status", "warning")
        issues = diagnosis_data.get("issues", [])
        immediate_actions = diagnosis_data.get("immediate_actions", [])
        long_term_care = diagnosis_data.get("long_term_care", {})
        summary = diagnosis_data.get("summary", "No se pudo generar resumen")
        
        # Determinar severidad basado en health_score
        if health_score >= 70:
            severity = "healthy"
        elif health_score >= 50:
            severity = "warning"
        elif health_score >= 30:
            severity = "medium"
        else:
            severity = "critical"
        
        # Obtener nombre de enfermedad principal (si existe)
        disease_name = None
        if issues:
            # Buscar el issue más severo
            severe_issues = [i for i in issues if i.get("severity") == "high"]
            if severe_issues:
                disease_name = severe_issues[0].get("name")
            else:
                disease_name = issues[0].get("name") if issues else None
        
        # Construir recomendaciones en formato simple
        recommendations = []
        
        # Agregar acciones inmediatas
        for action in sorted(immediate_actions, key=lambda x: x.get("priority", 99)):
            recommendations.append(action.get("action", ""))
        
        # Agregar cuidados a largo plazo
        if long_term_care:
            if long_term_care.get("watering"):
                recommendations.append(f"Riego: {long_term_care['watering']}")
            if long_term_care.get("light"):
                recommendations.append(f"Luz: {long_term_care['light']}")
            if long_term_care.get("fertilizer"):
                recommendations.append(f"Fertilizante: {long_term_care['fertilizer']}")
        
        # Generar plan semanal
        weekly_plan = generate_weekly_plan_from_diagnosis(diagnosis_data)
        
        # Construir respuesta final
        final_diagnosis = {
            "diagnosis": summary,
            "confidence": species.get("confidence", 0.7),
            "severity": severity,
            "disease_name": disease_name,
            "recommendations": recommendations,
            "weekly_plan": weekly_plan,
            "health_score": health_score,
            "species": species.get("name", "Desconocida"),
            "scientific_name": species.get("scientific_name", ""),
            "issues": issues,
            "immediate_actions": immediate_actions,
            "long_term_care": long_term_care,
            "empathetic_message": diagnosis_data.get("empathetic_message", "¡Sigue cuidando tu planta!")
        }
        
        logger.info(f"Diagnóstico completado: {severity}, health: {health_score}%")
        return final_diagnosis
        
    except json.JSONDecodeError as e:
        logger.error(f"Error parseando JSON de diagnóstico: {e}")
        logger.error(f"Contenido recibido: {result['content'][:500]}")
        
        # Fallback: usar el texto como diagnóstico
        return {
            "diagnosis": result["content"][:500],
            "confidence": 0.5,
            "severity": "warning",
            "disease_name": None,
            "recommendations": ["No se pudo generar recomendaciones específicas. Consulta a un experto."],
            "weekly_plan": []
        }


def generate_weekly_plan_from_diagnosis(diagnosis_data: Dict[str, Any]) -> list:
    """
    Genera un plan semanal accionable basado en el diagnóstico.
    
    Args:
        diagnosis_data: Datos del diagnóstico completo
    
    Returns:
        Lista de tareas semanales con días y prioridades
    """
    plan = []
    status = diagnosis_data.get("status", "warning")
    health_score = diagnosis_data.get("health_score", 50)
    immediate_actions = diagnosis_data.get("immediate_actions", [])
    long_term_care = diagnosis_data.get("long_term_care", {})
    
    # Plan según severidad
    if status == "critical" or health_score < 30:
        # Plan urgente
        plan = [
            {
                "day": "Hoy",
                "task": immediate_actions[0].get("action") if immediate_actions else "Aplicar tratamiento urgente",
                "priority": "high",
                "completed": False
            },
            {
                "day": "Mañana",
                "task": "Revisar progreso y síntomas",
                "priority": "high",
                "completed": False
            },
            {
                "day": "En 3 días",
                "task": immediate_actions[1].get("action") if len(immediate_actions) > 1 else "Segunda aplicación de tratamiento",
                "priority": "high",
                "completed": False
            },
            {
                "day": "En 5 días",
                "task": "Evaluar efectividad del tratamiento",
                "priority": "medium",
                "completed": False
            },
            {
                "day": "En 7 días",
                "task": "Fotografiar para comparar progreso",
                "priority": "medium",
                "completed": False
            }
        ]
    elif status == "warning" or health_score < 70:
        # Plan moderado
        plan = [
            {
                "day": "Hoy",
                "task": immediate_actions[0].get("action") if immediate_actions else "Iniciar tratamiento recomendado",
                "priority": "medium",
                "completed": False
            },
            {
                "day": "En 2 días",
                "task": long_term_care.get("watering", "Regar según lo recomendado"),
                "priority": "medium",
                "completed": False
            },
            {
                "day": "En 4 días",
                "task": "Aplicar fertilizante si es necesario",
                "priority": "low",
                "completed": False
            },
            {
                "day": "En 7 días",
                "task": "Monitoreo de síntomas y progreso",
                "priority": "low",
                "completed": False
            }
        ]
    else:
        # Plan de mantenimiento (planta sana)
        plan = [
            {
                "day": "Lunes",
                "task": long_term_care.get("watering", "Riego regular"),
                "priority": "low",
                "completed": False
            },
            {
                "day": "Miércoles",
                "task": "Revisar hojas y tallo",
                "priority": "low",
                "completed": False
            },
            {
                "day": "Viernes",
                "task": long_term_care.get("fertilizer", "Fertilizar si corresponde"),
                "priority": "low",
                "completed": False
            },
            {
                "day": "Domingo",
                "task": "Inspección general y limpieza de hojas",
                "priority": "low",
                "completed": False
            }
        ]
    
    return plan
