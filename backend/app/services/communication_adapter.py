"""
MEJORA #2: Adaptador de Comunicación Inteligente
Adapta las respuestas según el nivel de experiencia del usuario
"""
import logging
from typing import Dict, Any, List
from enum import Enum

logger = logging.getLogger(__name__)


class UserLevel(str, Enum):
    """Niveles de experiencia del usuario"""
    BEGINNER = "beginner"      # Principiante: 0-2 diagnósticos
    INTERMEDIATE = "intermediate"  # Intermedio: 3-10 diagnósticos
    EXPERT = "expert"          # Experto: 11+ diagnósticos


class CommunicationAdapter:
    """Adapta respuestas técnicas según nivel de usuario"""
    
    # Diccionario de términos técnicos con explicaciones
    TECHNICAL_TERMS = {
        "clorosis": {
            "beginner": "hojas amarillas (falta de nutrientes)",
            "intermediate": "clorosis (hojas amarillas por falta de nutrientes)",
            "expert": "clorosis"
        },
        "necrosis": {
            "beginner": "manchas marrones o negras (tejido muerto)",
            "intermediate": "necrosis (muerte del tejido de la hoja)",
            "expert": "necrosis"
        },
        "antracnosis": {
            "beginner": "manchas oscuras en hojas (enfermedad por hongos)",
            "intermediate": "antracnosis (infección fúngica)",
            "expert": "antracnosis"
        },
        "mildiu": {
            "beginner": "polvo blanco en hojas (hongos por humedad)",
            "intermediate": "mildiu (hongo causado por alta humedad)",
            "expert": "mildiu"
        },
        "pH del suelo": {
            "beginner": "acidez de la tierra",
            "intermediate": "pH del suelo (nivel de acidez)",
            "expert": "pH del suelo"
        },
        "fertilizante NPK": {
            "beginner": "abono con nitrógeno, fósforo y potasio",
            "intermediate": "fertilizante NPK (nitrógeno, fósforo, potasio)",
            "expert": "fertilizante NPK"
        },
        "poda apical": {
            "beginner": "cortar la punta de la planta",
            "intermediate": "poda apical (corte de la punta principal)",
            "expert": "poda apical"
        },
        "fotosíntesis": {
            "beginner": "proceso de producción de alimento de la planta",
            "intermediate": "fotosíntesis (proceso de producción de energía)",
            "expert": "fotosíntesis"
        }
    }
    
    @staticmethod
    def detect_user_level(diagnosis_count: int) -> UserLevel:
        """
        Detecta automáticamente el nivel del usuario basado en historial.
        
        Args:
            diagnosis_count: Número de diagnósticos realizados
            
        Returns:
            Nivel de experiencia detectado
        """
        if diagnosis_count <= 2:
            return UserLevel.BEGINNER
        elif diagnosis_count <= 10:
            return UserLevel.INTERMEDIATE
        else:
            return UserLevel.EXPERT
    
    @staticmethod
    def adapt_diagnosis(diagnosis: str, user_level: UserLevel) -> str:
        """
        Adapta el texto del diagnóstico según el nivel del usuario.
        
        Args:
            diagnosis: Texto del diagnóstico original
            user_level: Nivel de experiencia del usuario
            
        Returns:
            Diagnóstico adaptado
        """
        adapted_text = diagnosis
        
        # Reemplazar términos técnicos según nivel
        for term, translations in CommunicationAdapter.TECHNICAL_TERMS.items():
            if term.lower() in adapted_text.lower():
                replacement = translations.get(user_level.value, term)
                # Reemplazar manteniendo mayúsculas/minúsculas originales
                import re
                pattern = re.compile(re.escape(term), re.IGNORECASE)
                adapted_text = pattern.sub(replacement, adapted_text)
        
        # Agregar contexto adicional para principiantes
        if user_level == UserLevel.BEGINNER:
            adapted_text = f"💡 Explicación simple: {adapted_text}"
        
        logger.info(f"Diagnóstico adaptado para nivel {user_level.value}")
        return adapted_text
    
    @staticmethod
    def adapt_recommendations(
        recommendations: List[str], 
        user_level: UserLevel
    ) -> List[str]:
        """
        Adapta las recomendaciones según el nivel del usuario.
        
        Args:
            recommendations: Lista de recomendaciones originales
            user_level: Nivel de experiencia del usuario
            
        Returns:
            Lista de recomendaciones adaptadas
        """
        adapted = []
        
        for rec in recommendations:
            # Adaptar términos técnicos
            adapted_rec = rec
            for term, translations in CommunicationAdapter.TECHNICAL_TERMS.items():
                if term.lower() in adapted_rec.lower():
                    replacement = translations.get(user_level.value, term)
                    import re
                    pattern = re.compile(re.escape(term), re.IGNORECASE)
                    adapted_rec = pattern.sub(replacement, adapted_rec)
            
            # Agregar emojis y simplificación para principiantes
            if user_level == UserLevel.BEGINNER:
                if "regar" in adapted_rec.lower() or "agua" in adapted_rec.lower():
                    adapted_rec = f"💧 {adapted_rec}"
                elif "luz" in adapted_rec.lower() or "sol" in adapted_rec.lower():
                    adapted_rec = f"☀️ {adapted_rec}"
                elif "fertiliz" in adapted_rec.lower() or "abono" in adapted_rec.lower():
                    adapted_rec = f"🌱 {adapted_rec}"
                elif "poda" in adapted_rec.lower() or "cortar" in adapted_rec.lower():
                    adapted_rec = f"✂️ {adapted_rec}"
            
            adapted.append(adapted_rec)
        
        return adapted
    
    @staticmethod
    def get_level_badge(user_level: UserLevel) -> str:
        """
        Retorna un badge visual para el nivel del usuario.
        
        Args:
            user_level: Nivel de experiencia
            
        Returns:
            Emoji/badge representativo
        """
        badges = {
            UserLevel.BEGINNER: "🌱 Principiante",
            UserLevel.INTERMEDIATE: "🌿 Intermedio",
            UserLevel.EXPERT: "🌳 Experto"
        }
        return badges.get(user_level, "🌱")
    
    @staticmethod
    def add_educational_tips(
        diagnosis_data: Dict[str, Any], 
        user_level: UserLevel
    ) -> Dict[str, Any]:
        """
        Agrega tips educacionales basados en el nivel del usuario.
        
        Args:
            diagnosis_data: Datos del diagnóstico
            user_level: Nivel de experiencia
            
        Returns:
            Datos con tips educacionales agregados
        """
        tips = []
        
        if user_level == UserLevel.BEGINNER:
            tips = [
                "💡 Tip: Toma fotos con luz natural para mejores diagnósticos",
                "📚 Aprende: El riego depende del tipo de planta y clima",
                "🌡️ Importante: La temperatura afecta el crecimiento de las plantas"
            ]
        elif user_level == UserLevel.INTERMEDIATE:
            tips = [
                "💡 Tip: Observa el envés de las hojas para detectar plagas",
                "📊 Dato: El pH ideal varía entre 6.0-7.0 para la mayoría de plantas",
                "🔄 Recuerda: Rota tus plantas cada semana para crecimiento uniforme"
            ]
        else:  # EXPERT
            tips = [
                "🔬 Avanzado: Considera análisis de suelo para diagnóstico preciso",
                "📈 Dato: Lleva registro de fertilización para optimizar nutrición",
                "🌐 Recurso: Consulta índices especializados de plagas en tu región"
            ]
        
        # Agregar 1-2 tips aleatorios relevantes
        import random
        diagnosis_data["educational_tips"] = random.sample(tips, min(2, len(tips)))
        
        return diagnosis_data


def adapt_full_diagnosis(
    diagnosis_data: Dict[str, Any],
    user_level: UserLevel
) -> Dict[str, Any]:
    """
    Función principal que adapta todo el diagnóstico al nivel del usuario.
    
    Args:
        diagnosis_data: Diccionario completo del diagnóstico
        user_level: Nivel de experiencia del usuario
        
    Returns:
        Diagnóstico completamente adaptado
    """
    adapter = CommunicationAdapter()
    
    # Adaptar diagnóstico principal
    if "diagnosis" in diagnosis_data:
        diagnosis_data["diagnosis"] = adapter.adapt_diagnosis(
            diagnosis_data["diagnosis"], 
            user_level
        )
    
    # Adaptar recomendaciones
    if "recommendations" in diagnosis_data:
        diagnosis_data["recommendations"] = adapter.adapt_recommendations(
            diagnosis_data["recommendations"],
            user_level
        )
    
    # Agregar badge de nivel
    diagnosis_data["user_level"] = user_level.value
    diagnosis_data["level_badge"] = adapter.get_level_badge(user_level)
    
    # Agregar tips educacionales
    diagnosis_data = adapter.add_educational_tips(diagnosis_data, user_level)
    
    logger.info(f"Diagnóstico completo adaptado para nivel {user_level.value}")
    
    return diagnosis_data
