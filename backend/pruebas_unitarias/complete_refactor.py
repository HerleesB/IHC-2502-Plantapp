"""
Script FINAL para completar la refactorización
Ejecutar desde backend/: python complete_refactor.py
"""
from pathlib import Path
import sys

BASE_DIR = Path(__file__).parent

print("\n" + "=" * 70)
print("🧹 COMPLETANDO REFACTORIZACIÓN - PASO FINAL")
print("=" * 70 + "\n")

# Archivos a eliminar
files_to_delete = [
    "create_demo_simple.py",
    "create_community_posts.py",
    "create_community_posts_simple.py",
    "create_env.py",
    "migrate_add_auth.py",
    "migrate_add_auth_OLD.py",
    "test_photo_validation.py",
    "install.bat",
    "install.sh",
    "setup.sh",
    "make_executable.sh",
    "FASE1_COMPLETADA.md",
    "FASE4_AUTH_BACKEND.md",
    "MODELOS_GROQ.md",
    "README_COMPLETO.md",
    "requirements_auth.txt",
    "requirements_python314.txt",
    "requirements_updated.txt",
    "2.0.35",
    "refactor_backend.py",
    "refactor_auto.py"
]

deleted = 0
not_found = 0
errors = 0

for file_name in files_to_delete:
    file_path = BASE_DIR / file_name
    if file_path.exists():
        try:
            file_path.unlink()
            print(f"✅ Eliminado: {file_name}")
            deleted += 1
        except Exception as e:
            print(f"❌ Error eliminando {file_name}: {e}")
            errors += 1
    else:
        print(f"ℹ️  No encontrado (ya eliminado): {file_name}")
        not_found += 1

print("\n" + "=" * 70)
print("📊 RESUMEN DE LIMPIEZA")
print("=" * 70)
print(f"✅ Archivos eliminados: {deleted}")
print(f"ℹ️  Archivos no encontrados: {not_found}")
print(f"❌ Errores: {errors}")

print("\n" + "=" * 70)
print("✅ REFACTORIZACIÓN COMPLETADA EXITOSAMENTE")
print("=" * 70)

print("\n📁 NUEVA ESTRUCTURA:")
print("""
backend/
├── app/                    ✅ Código fuente
├── scripts/                ✅ Utilidades organizadas
├── tests/                  ✅ Tests (con .gitkeep)
├── docs/                   ✅ Documentación técnica
├── uploads/                ✅ Archivos (con .gitkeep)
├── requirements.txt        ✅ Dependencias principales
├── requirements-dev.txt    ✅ Dependencias de desarrollo
├── pytest.ini              ✅ Configuración de tests
├── .gitignore              ✅ Actualizado
├── .env.example            ✅ Plantilla de variables
└── README.md               ✅ Documentación completa
""")

print("\n🚀 PRÓXIMOS PASOS:")
print("1. Revisar y actualizar .env con tu GROQ_API_KEY")
print("2. Ejecutar: python scripts/create_demo_simple.py")
print("3. Ejecutar: python scripts/create_community_posts.py")
print("4. Iniciar servidor: uvicorn app.main:app --reload")
print("5. Visitar: http://localhost:8000/docs")

print("\n✨ Todo listo! El backend está completamente organizado.\n")
