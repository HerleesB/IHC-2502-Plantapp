"""Servicio para interactuar con Groq AI - VERSIÓN CORREGIDA"""
import base64
import logging
from typing import Dict, Any
import json

from groq import Groq
from app.config import get_settings
from app.utils.prompts import DiagnosisPrompts

logger = logging.getLogger(__name__)
settings = get_settings()


class GroqService:
    """Servicio para interactuar con la API de Groq AI."""

    def __init__(self):
        """Inicializa el cliente de Groq"""
        self.client = Groq(api_key=settings.GROQ_API_KEY)
        self.model = settings.GROQ_MODEL
        self.timeout = getattr(settings, "GROQ_TIMEOUT", 30)

    @staticmethod
    def encode_image(image_bytes: bytes) -> str:
        """Codifica la imagen en base64 para enviarla a Groq."""
        return base64.b64encode(image_bytes).decode("utf-8")

    async def analyze_image_with_prompt(
        self,
        image_bytes: bytes,
        prompt: str,
        temperature: float = 0.7,
        max_tokens: int = 2048,
    ) -> Dict[str, Any]:
        """Analiza una imagen con un prompt específico usando modelo multimodal de Groq."""
        try:
            base64_image = self.encode_image(image_bytes)

            messages = [
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{base64_image}"
                            },
                        },
                    ],
                }
            ]

            logger.info(f"Enviando análisis a Groq con modelo {self.model}")
            response = self.client.chat.completions.create(
                model=self.model,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
                timeout=self.timeout,
            )

            content = response.choices[0].message.content
            usage = {
                "prompt_tokens": getattr(response.usage, "prompt_tokens", None),
                "completion_tokens": getattr(response.usage, "completion_tokens", None),
                "total_tokens": getattr(response.usage, "total_tokens", None),
            }

            logger.info(f"Análisis completado. Tokens usados: {usage['total_tokens']}")
            return {"success": True, "content": content, "usage": usage, "model": self.model}

        except Exception as e:
            logger.error(f"Error al analizar imagen con Groq: {e}")
            return {"success": False, "error": str(e), "content": None, "usage": None}

    async def analyze_text_only(
        self,
        prompt: str,
        temperature: float = 0.7,
        max_tokens: int = 512,
    ) -> Dict[str, Any]:
        """Analiza texto sin imagen."""
        try:
            model = getattr(settings, "GROQ_TEXT_MODEL", self.model)
            response = self.client.chat.completions.create(
                model=model,
                messages=[{"role": "user", "content": prompt}],
                temperature=temperature,
                max_tokens=max_tokens,
                timeout=self.timeout,
            )
            usage = {
                "prompt_tokens": getattr(response.usage, "prompt_tokens", None),
                "completion_tokens": getattr(response.usage, "completion_tokens", None),
                "total_tokens": getattr(response.usage, "total_tokens", None),
            }
            return {
                "success": True,
                "content": response.choices[0].message.content,
                "usage": usage
            }
        except Exception as e:
            logger.error(f"Error en análisis de texto: {e}")
            return {"success": False, "error": str(e)}


# ========== FUNCIONES DE ALTO NIVEL ==========

async def get_plant_diagnosis(image_path: str, symptoms: str | None = None) -> Dict[str, Any]:
    """
    CU-02: Obtener diagnóstico completo de planta con análisis de imagen.
    
    Args:
        image_path: Ruta al archivo de imagen
        symptoms: Síntomas adicionales reportados por el usuario
    
    Returns:
        Diccionario con diagnóstico completo incluyendo weekly_plan
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
    logger.info(f"Iniciando diagnóstico completo desde {image_path}")
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
            "recommendations": ["Intenta tomar otra foto con mejor iluminación"],
            "weekly_plan": []
        }
    
    # Parsear respuesta JSON
    try:
        content = result["content"]
        if "```json" in content:
            content = content.split("```json")[1].split("```")[0]
        elif "```" in content:
            content = content.split("```")[1].split("```")[0]
        
        diagnosis_data = json.loads(content.strip())
        
        # Extraer información clave
        species = diagnosis_data.get("species", {})
        health_score = diagnosis_data.get("health_score", 50)
        status = diagnosis_data.get("status", "warning")
        issues = diagnosis_data.get("issues", [])
        immediate_actions = diagnosis_data.get("immediate_actions", [])
        summary = diagnosis_data.get("summary", "Diagnóstico completado")
        
        # Determinar severidad
        if health_score >= 70:
            severity = "healthy"
        elif health_score >= 50:
            severity = "warning"
        else:
            severity = "critical"
        
        # Obtener nombre de enfermedad principal
        disease_name = None
        if issues:
            severe_issues = [i for i in issues if i.get("severity") == "high"]
            disease_name = severe_issues[0].get("name") if severe_issues else issues[0].get("name")
        
        # Construir recomendaciones
        recommendations = [action.get("action", "") for action in immediate_actions[:5]]
        
        # Generar plan semanal
        weekly_plan = generate_weekly_plan(status, health_score, immediate_actions)
        
        logger.info(f"Diagnóstico completado: {severity}, health: {health_score}%")
        
        return {
            "diagnosis": summary,
            "confidence": species.get("confidence", 0.7),
            "severity": severity,
            "disease_name": disease_name,
            "recommendations": recommendations if recommendations else ["Monitorear planta diariamente"],
            "weekly_plan": weekly_plan
        }
        
    except json.JSONDecodeError as e:
        logger.error(f"Error parseando JSON: {e}")
        content = result["content"]
        return {
            "diagnosis": content[:500],
            "confidence": 0.5,
            "severity": "warning",
            "disease_name": None,
            "recommendations": ["Consulta diagnóstico completo"],
            "weekly_plan": []
        }


def generate_weekly_plan(status: str, health_score: int, immediate_actions: list) -> list:
    """
    CU-03: Generar plan semanal accionable basado en severidad.
    """
    if status == "critical" or health_score < 30:
        return [
            {"day": "Hoy", "task": immediate_actions[0].get("action") if immediate_actions else "Aplicar tratamiento urgente", "priority": "high"},
            {"day": "Mañana", "task": "Revisar progreso y síntomas", "priority": "high"},
            {"day": "En 3 días", "task": "Segunda aplicación de tratamiento", "priority": "high"},
            {"day": "En 5 días", "task": "Evaluar efectividad", "priority": "medium"},
            {"day": "En 7 días", "task": "Fotografiar para comparar", "priority": "medium"}
        ]
    elif status == "warning" or health_score < 70:
        return [
            {"day": "Hoy", "task": immediate_actions[0].get("action") if immediate_actions else "Iniciar tratamiento", "priority": "medium"},
            {"day": "En 2 días", "task": "Regar según recomendado", "priority": "medium"},
            {"day": "En 4 días", "task": "Aplicar fertilizante", "priority": "low"},
            {"day": "En 7 días", "task": "Monitoreo de progreso", "priority": "low"}
        ]
    else:
        return [
            {"day": "Lunes", "task": "Riego regular", "priority": "low"},
            {"day": "Miércoles", "task": "Revisar hojas y tallo", "priority": "low"},
            {"day": "Viernes", "task": "Fertilizar si corresponde", "priority": "low"},
            {"day": "Domingo", "task": "Inspección general", "priority": "low"}
        ]


async def validate_photo_quality(image_bytes: bytes) -> Dict[str, Any]:
    """
    CU-01: Validar calidad y encuadre de foto antes del diagnóstico.
    
    Args:
        image_bytes: Bytes de la imagen a validar
    
    Returns:
        Diccionario con resultado de validación y mensaje de guía
    """
    service = GroqService()
    
    # Obtener prompt de validación de encuadre
    prompt = DiagnosisPrompts.get_centering_validation_prompt()
    
    # Analizar con Groq
    result = await service.analyze_image_with_prompt(
        image_bytes=image_bytes,
        prompt=prompt,
        temperature=0.3,  # Baja temperatura para respuestas consistentes
        max_tokens=512
    )
    
    if not result.get("success"):
        logger.error(f"Error en validación de foto: {result.get('error')}")
        return {
            "success": False,
            "guidance": "Error al analizar la foto. Por favor intenta de nuevo.",
            "details": {
                "lighting": 0.0,
                "focus": 0.0,
                "distance": 0.0,
                "overall": 0.0
            }
        }
    
    # Parsear respuesta JSON de Groq
    try:
        # Limpiar respuesta (eliminar markdown si existe)
        content = result["content"]
        if "```json" in content:
            content = content.split("```json")[1].split("```")[0]
        elif "```" in content:
            content = content.split("```")[1].split("```")[0]
        
        validation_json = json.loads(content.strip())
        
        # Extraer información estructurada
        is_centered = validation_json.get("is_centered", False)
        plant_detected = validation_json.get("plant_detected", False)
        confidence = validation_json.get("confidence", 0.0)
        voice_guidance = validation_json.get("voice_guidance", "No se pudo analizar la imagen")
        recommendations = validation_json.get("recommendations", {})
        issues = validation_json.get("issues", [])
        
        # Calcular scores específicos basados en recomendaciones
        lighting_ok = recommendations.get("lighting", "ok") == "ok"
        focus_ok = recommendations.get("focus", "ok") == "ok"
        distance_ok = recommendations.get("distance", "ok") == "ok"
        
        lighting_score = 0.9 if lighting_ok else 0.4
        focus_score = 0.9 if focus_ok else 0.5
        distance_score = 0.9 if distance_ok else 0.5
        
        # Score general: debe estar centrada, con planta detectada y sin problemas mayores
        overall_score = confidence if (is_centered and plant_detected and len(issues) == 0) else max(0.3, confidence * 0.6)
        
        # Determinar si la foto es aceptable (>= 0.7 en overall)
        success = overall_score >= 0.7
        
        logger.info(f"Validación de foto: {'✅ Aprobada' if success else '❌ Necesita ajustes'} (score: {overall_score:.2f})")
        
        return {
            "success": success,
            "guidance": voice_guidance,
            "details": {
                "lighting": round(lighting_score, 2),
                "focus": round(focus_score, 2),
                "distance": round(distance_score, 2),
                "overall": round(overall_score, 2),
                "is_centered": is_centered,
                "plant_detected": plant_detected,
                "issues": issues,
                "recommendations": recommendations
            }
        }
        
    except json.JSONDecodeError as e:
        logger.warning(f"Respuesta de Groq no es JSON válido en validación: {e}")
        # Fallback: extraer guidance del texto plano si es posible
        content = result["content"]
        guidance_text = content[:100] if len(content) > 100 else content
        
        return {
            "success": False,
            "guidance": "La imagen necesita ajustes. Asegúrate de que la planta esté bien centrada y enfocada.",
            "details": {
                "lighting": 0.5,
                "focus": 0.5,
                "distance": 0.5,
                "overall": 0.5
            }
        }
    except Exception as e:
        logger.error(f"Error procesando validación de foto: {e}")
        return {
            "success": False,
            "guidance": "Error al validar la foto. Por favor intenta nuevamente.",
            "details": {
                "lighting": 0.0,
                "focus": 0.0,
                "distance": 0.0,
                "overall": 0.0
            }
        }


async def moderate_content(text: str) -> bool:
    """
    CU-09: Moderar contenido con IA para comunidad.
    
    Args:
        text: Texto a moderar
    
    Returns:
        True si el contenido es apropiado, False si no
    """
    service = GroqService()
    
    prompt = f"""Eres un moderador de contenido para una comunidad de jardinería.
Analiza si el siguiente texto es apropiado (sin spam, insultos, contenido ofensivo, o información peligrosa).

TEXTO A MODERAR:
{text}

Responde SOLO con una palabra: APROPIADO o INAPROPIADO"""
    
    try:
        result = await service.analyze_text_only(
            prompt=prompt,
            temperature=0.2,  # Baja temperatura para respuesta consistente
            max_tokens=20
        )
        
        if not result.get("success"):
            logger.warning("Moderación falló, permitiendo contenido por defecto")
            return True  # Default: permitir si falla
        
        content = result["content"].strip().upper()
        is_appropriate = "APROPIADO" in content
        
        logger.info(f"Moderación: {content} -> {'✅ Apropiado' if is_appropriate else '❌ Inapropiado'}")
        return is_appropriate
        
    except Exception as e:
        logger.error(f"Error en moderación: {e}")
        return True  # Default: permitir si hay error
