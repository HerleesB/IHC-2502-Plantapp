#!/bin/bash

echo "===================================="
echo "  LIMPIADOR DE PROYECTO ANDROID (macOS)"
echo "===================================="
echo
echo "Este script eliminará archivos temporales"
echo "para reducir el tamaño del proyecto."
echo
echo "Los archivos se regenerarán automáticamente"
echo "al abrir el proyecto en Android Studio."
echo
read -p "Presiona ENTER para continuar..."

# Ir a la carpeta donde está el script
cd "$(dirname "$0")"

echo
echo "Eliminando archivos pesados..."
echo "------------------------------"

# Eliminar archivos .hprof
echo "Buscando archivos .hprof (volcados de memoria)..."
find . -name "*.hprof" -type f -exec rm -f {} \;
echo "[OK] Archivos .hprof eliminados"

# Eliminar archivos .log
echo
echo "Buscando archivos de log..."
find . -name "*.log" -type f -exec rm -f {} \;
echo "[OK] Archivos .log eliminados"

# Eliminar carpetas pesadas
echo
echo "Eliminando carpetas de compilación..."
for dir in .gradle .idea .kotlin app/build build; do
  if [ -d "$dir" ]; then
    echo "Eliminando $dir ..."
    rm -rf "$dir"
    echo "[OK] $dir eliminado"
  fi
done

# Eliminar local.properties
echo
if [ -f "local.properties" ]; then
  echo "Eliminando local.properties..."
  rm -f local.properties
  echo "[OK] local.properties eliminado"
fi

# Eliminar archivos .iml
echo
echo "Buscando y eliminando archivos .iml..."
find . -name "*.iml" -type f -exec rm -f {} \;
echo "[OK] Archivos .iml eliminados"

# Eliminar módulos de Kotlin y crash logs
find . -name "*.kotlin_module" -type f -delete
find . -name "hs_err_*.log" -type f -delete
find . -name "*.jfr" -type f -delete

echo
echo "===================================="
echo "  LIMPIEZA COMPLETADA!"
echo "===================================="
echo
du -sh .
echo
echo "El proyecto está listo para comprimir."
echo "Puedes usar zip, tar o cualquier compresor gráfico."
echo
read -p "Presiona ENTER para salir..."