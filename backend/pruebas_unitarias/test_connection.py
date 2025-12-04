"""
Script de prueba para verificar la conexion con Groq AI
"""
import asyncio
from pathlib import Path
import sys

# Agregar el directorio padre al path
sys.path.append(str(Path(__file__).parent))

from app.services.groq_service import GroqService
from app.utils.prompts import DiagnosisPrompts
from app.config import get_settings

async def test_groq_connection():
    """
    Prueba la conexion con Groq AI usando un prompt simple
    """
    print("=" * 60)
    print("🧪 PRUEBA DE CONEXION CON GROQ AI")
    print("=" * 60)
    
    # Verificar configuracion
    settings = get_settings()
    print(f"\n📋 Configuracion:")
    print(f"   - Modelo: {settings.GROQ_MODEL}")
    print(f"   - API Key: {'✓ Configurada' if settings.GROQ_API_KEY else '✗ NO configurada'}")
    
    if not settings.GROQ_API_KEY or settings.GROQ_API_KEY == "your_groq_api_key_here":
        print("\n❌ ERROR: GROQ_API_KEY no configurada")
        print("   Por favor, configura tu API key en el archivo .env")
        return
    
    # Inicializar servicios
    groq_service = GroqService()
    prompts = DiagnosisPrompts()
    
    print("\n🔄 Realizando prueba de texto simple...")
    
    try:
        # Prueba simple sin imagen
        result = await groq_service.analyze_text_only(
            prompt="Responde en JSON: {'mensaje': 'Conexion exitosa', 'estado': 'ok'}",
            temperature=0.1,
            max_tokens=100
        )
        
        if result["success"]:
            print("\n✅ CONEXION EXITOSA")
            print(f"   Tokens usados: {result['usage']['total_tokens']}")
            print(f"   Respuesta: {result['content'][:150]}...")
        else:
            print(f"\n❌ ERROR: {result['error']}")
            
    except Exception as e:
        print(f"\n❌ ERROR INESPERADO: {str(e)}")

    print("\n" + "=" * 60)
    print("Para probar con imagenes, usa test_with_image.py")
    print("=" * 60)


if __name__ == "__main__":
    asyncio.run(test_groq_connection())
