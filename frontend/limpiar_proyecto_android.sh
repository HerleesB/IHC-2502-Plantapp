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

cd "$(dirname "$0")"

echo
echo "Eliminando archivos pesados..."
echo "------------------------------"

find . -name "*.hprof" -type f -delete
find . -name "*.log" -type f -delete
find . -name "*.iml" -type f -delete
find . -name "*.jfr" -type f -delete
find . -name "hs_err_*.log" -type f -delete
find . -name "*.kotlin_module" -type f -delete

for dir in .gradle .idea .kotlin app/build build; do
  if [ -d "$dir" ]; then
    echo "Eliminando $dir ..."
    rm -rf "$dir"
    echo "[OK] $dir eliminado"
  fi
done

if [ -f "local.properties" ]; then
  echo "Eliminando local.properties..."
  rm -f local.properties
  echo "[OK] local.properties eliminado"
fi

echo
echo "===================================="
echo "  LIMPIEZA COMPLETADA!"
echo "===================================="
du -sh .
echo
read -p "Presiona ENTER para salir..."
