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
            "recommendations": []
        }
    
    # Obtener prompt de diagnóstico
    prompt = DiagnosisPrompts.get_diagnosis_prompt()
    if symptoms:
        prompt += f"\n\nSÍNTOMAS ADICIONALES REPORTADOS: {symptoms}"
    
    # Analizar con Groq
    result = await service.analyze_image_with_prompt(
        image_bytes=image_bytes,
        prompt=prompt,
        temperature=0.7,
        max_tokens=2048
    )
    
    if not result.get("success"):
        logger.error(f"Fallo en análisis Groq: {result.get('error')}")
        return {
            "diagnosis": "Error al procesar diagnóstico con IA",
            "confidence": 0.0,
            "severity": "unknown",
            "disease_name": None,
            "recommendations": [
                "Intenta tomar otra foto con mejor iluminación",
                "Asegúrate de que la planta esté en foco"
            ]
        }
    
    # Parsear respuesta JSON de Groq
    try:
        # Limpiar respuesta (Groq a veces incluye markdown)
        content = result["content"]
        if "```json" in content:
            content = content.split("```json")[1].split("```")[0]
        elif "```" in content:
            content = content.split("```")[1].split("```")[0]
        
        diagnosis_json = json.loads(content.strip())
        
        # Extraer información estructurada
        issues = diagnosis_json.get("issues", [])
        primary_issue = issues[0] if issues else {}
        
        immediate_actions = diagnosis_json.get("immediate_actions", [])
        recommendations = [
            action.get("action", "") 
            for action in immediate_actions[:5]  # Top 5
        ]
        
        return {
            "diagnosis": diagnosis_json.get("summary", "Diagnóstico completado"),
            "confidence": diagnosis_json.get("species", {}).get("confidence", 0.75),
            "severity": diagnosis_json.get("status", "warning"),
            "disease_name": primary_issue.get("name", None),
            "recommendations": recommendations if recommendations else [
                "Monitorear la planta diariamente",
                "Mantener buena iluminación indirecta",
                "Verificar frecuencia de riego"
            ]
        }
        
    except json.JSONDecodeError as e:
        logger.warning(f"Respuesta de Groq no es JSON válido: {e}")
        # Si no es JSON válido, extraer info de texto plano
        content = result["content"]
        return {
            "diagnosis": content[:500] if len(content) > 500 else content,
            "confidence": 0.7,
            "severity": "medium",
            "disease_name": "Diagnóstico general",
            "recommendations": [
                "Revisa el diagnóstico completo",
                "Monitorea la planta regularmente",
                "Asegura condiciones óptimas de luz y agua"
            ]
        }
    except Exception as e:
        logger.error(f"Error procesando respuesta de Groq: {e}")
        return {
            "diagnosis": "Error al procesar respuesta del análisis",
            "confidence": 0.0,
            "severity": "unknown",
            "disease_name": None,
            "recommendations": []
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
