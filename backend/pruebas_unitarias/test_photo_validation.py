"""
Script de prueba para validación de fotos con IA
"""
import asyncio
import sys
from pathlib import Path

# Agregar el directorio padre al path
sys.path.insert(0, str(Path(__file__).parent))

from app.services.groq_service import validate_photo_quality
from app.config import get_settings

async def test_validation():
    """Prueba la validación de foto con una imagen de ejemplo"""
    
    print("🧪 Iniciando prueba de validación de foto con IA...")
    print("=" * 60)
    
    settings = get_settings()
    print(f"✓ Configuración cargada")
    print(f"  - Modelo: {settings.GROQ_MODEL}")
    print(f"  - API Key configurada: {'✓' if settings.GROQ_API_KEY else '✗'}")
    print()
    
    # Buscar una imagen de prueba en uploads
    uploads_dir = Path(__file__).parent / "uploads"
    
    if not uploads_dir.exists():
        print("❌ No existe el directorio 'uploads'")
        print("   Crea una carpeta 'uploads' y coloca una imagen de planta para probar")
        return
    
    # Buscar cualquier imagen en uploads
    image_files = list(uploads_dir.glob("*.jpg")) + list(uploads_dir.glob("*.jpeg")) + list(uploads_dir.glob("*.png"))
    
    if not image_files:
        print("❌ No hay imágenes en el directorio 'uploads'")
        print("   Coloca una imagen de planta (JPG o PNG) en la carpeta 'uploads'")
        return
    
    test_image = image_files[0]
    print(f"📸 Imagen de prueba: {test_image.name}")
    print(f"   Tamaño: {test_image.stat().st_size / 1024:.2f} KB")
    print()
    
    # Leer imagen
    with open(test_image, "rb") as f:
        image_bytes = f.read()
    
    print("🤖 Enviando imagen a Groq AI para validación...")
    print("   (Esto puede tardar 5-10 segundos)")
    print()
    
    try:
        result = await validate_photo_quality(image_bytes)
        
        print("=" * 60)
        print("📊 RESULTADO DE LA VALIDACIÓN")
        print("=" * 60)
        print()
        
        success = result.get("success", False)
        guidance = result.get("guidance", "N/A")
        details = result.get("details", {})
        
        # Mostrar resultado principal
        if success:
            print("✅ FOTO APROBADA")
        else:
            print("⚠️  FOTO NECESITA AJUSTES")
        
        print()
        print(f"💬 Mensaje de la IA:")
        print(f"   '{guidance}'")
        print()
        
        # Mostrar detalles técnicos
        print("📈 Scores de calidad:")
        print(f"   • Iluminación:  {details.get('lighting', 0):.2f} / 1.00")
        print(f"   • Enfoque:      {details.get('focus', 0):.2f} / 1.00")
        print(f"   • Distancia:    {details.get('distance', 0):.2f} / 1.00")
        print(f"   • General:      {details.get('overall', 0):.2f} / 1.00")
        print()
        
        # Mostrar detalles adicionales si existen
        if 'is_centered' in details:
            print("🎯 Análisis detallado:")
            print(f"   • Planta centrada:  {'Sí' if details.get('is_centered') else 'No'}")
            print(f"   • Planta detectada: {'Sí' if details.get('plant_detected') else 'No'}")
            
            issues = details.get('issues', [])
            if issues:
                print(f"   • Problemas: {', '.join(issues)}")
            
            recommendations = details.get('recommendations', {})
            if recommendations:
                print("   • Recomendaciones:")
                for key, value in recommendations.items():
                    if value != 'ok':
                        print(f"     - {key}: {value}")
        
        print()
        print("=" * 60)
        print("✓ Prueba completada exitosamente")
        
    except Exception as e:
        print()
        print("=" * 60)
        print("❌ ERROR EN LA VALIDACIÓN")
        print("=" * 60)
        print(f"Error: {str(e)}")
        print()
        print("Posibles causas:")
        print("  1. API Key de Groq no configurada correctamente")
        print("  2. Problemas de conexión a internet")
        print("  3. Límite de rate de la API alcanzado")
        print()
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    print()
    print("╔════════════════════════════════════════════════════════════╗")
    print("║     PRUEBA DE VALIDACIÓN DE FOTOS CON GROQ AI             ║")
    print("╚════════════════════════════════════════════════════════════╝")
    print()
    
    asyncio.run(test_validation())
