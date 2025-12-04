@echo off
REM ====================================================================
REM SCRIPT DE LIMPIEZA AUTOMÁTICA - Jardín Inteligente
REM ====================================================================

echo.
echo ====================================================================
echo   LIMPIEZA Y OPTIMIZACION DE CODIGO
echo ====================================================================
echo.

REM Verificar que estamos en el directorio correcto
if not exist "backend\app\services" (
    echo ERROR: No se encuentra el directorio backend\app\services
    echo Por favor ejecuta este script desde la raíz del proyecto
    pause
    exit /b 1
)

echo [PASO 1/6] Verificando archivos duplicados...
timeout /t 2 >nul

cd backend\app\services

if exist "groq_service_complete.py" (
    echo ENCONTRADO: groq_service_complete.py [DUPLICADO]
)
if exist "groq_service_fixed.py" (
    echo ENCONTRADO: groq_service_fixed.py [DUPLICADO]
)
if exist "groq_service_no_vision.py" (
    echo ENCONTRADO: groq_service_no_vision.py [DUPLICADO]
)
if exist "groq_service_updated.py" (
    echo ENCONTRADO: groq_service_updated.py [VERSION TEMPORAL]
)
echo.

echo [PASO 2/6] Creando backup del archivo actual...
if exist "groq_service.py" (
    set backup_date=%date:~-4,4%%date:~-7,2%%date:~-10,2%
    copy "groq_service.py" "groq_service_BACKUP_%backup_date%.py" >nul
    echo OK - Backup creado: groq_service_BACKUP_%backup_date%.py
) else (
    echo ADVERTENCIA: groq_service.py no encontrado
)
echo.

echo [PASO 3/6] Aplicando version limpia...
if exist "groq_service_CLEAN.py" (
    copy /Y "groq_service_CLEAN.py" "groq_service.py" >nul
    echo OK - Version limpia aplicada
) else (
    echo ERROR: groq_service_CLEAN.py no encontrado
    echo Por favor ejecuta primero el script de instalacion de mejoras
    cd ..\..\..
    pause
    exit /b 1
)
echo.

echo [PASO 4/6] Deseas eliminar archivos duplicados?
echo.
echo Los siguientes archivos seran movidos a carpeta _deprecated:
if exist "groq_service_complete.py" echo   - groq_service_complete.py
if exist "groq_service_fixed.py" echo   - groq_service_fixed.py
if exist "groq_service_no_vision.py" echo   - groq_service_no_vision.py
if exist "groq_service_updated.py" echo   - groq_service_updated.py
echo.
choice /C SN /M "Mover archivos a _deprecated (S/N)"
if errorlevel 2 goto skip_deprecated
if errorlevel 1 goto move_deprecated

:move_deprecated
if not exist "_deprecated" mkdir _deprecated
if exist "groq_service_complete.py" move "groq_service_complete.py" "_deprecated\" >nul
if exist "groq_service_fixed.py" move "groq_service_fixed.py" "_deprecated\" >nul
if exist "groq_service_no_vision.py" move "groq_service_no_vision.py" "_deprecated\" >nul
if exist "groq_service_updated.py" move "groq_service_updated.py" "_deprecated\" >nul
echo OK - Archivos movidos a _deprecated\
goto after_deprecated

:skip_deprecated
echo SALTADO - Archivos duplicados mantenidos

:after_deprecated
echo.

cd ..\..\..

echo [PASO 5/6] Verificando estructura final...
timeout /t 2 >nul

if exist "backend\app\services\groq_service.py" (
    echo OK - groq_service.py existe
) else (
    echo ERROR: groq_service.py no encontrado
)

if exist "backend\app\services\_deprecated" (
    echo OK - Carpeta _deprecated creada
) else (
    echo INFO - No se creó carpeta _deprecated
)
echo.

echo [PASO 6/6] Resumen de limpieza
echo.
echo ==============================
echo   LIMPIEZA COMPLETADA
echo ==============================
echo.
echo [OK] Backup creado
echo [OK] Version limpia aplicada
if exist "backend\app\services\_deprecated" (
    echo [OK] Archivos deprecados movidos
) else (
    echo [--] Archivos deprecados mantenidos
)
echo.
echo ==============================
echo   ARCHIVOS FINALES
echo ==============================
echo.
echo Archivos en backend\app\services\:
dir /B backend\app\services\*.py 2>nul | findstr /V "__pycache__" | findstr /V "_deprecated"
echo.

echo ==============================
echo   SIGUIENTE PASO
echo ==============================
echo.
echo 1. Reinicia el backend:
echo    cd backend
echo    python -m app.main
echo.
echo 2. Prueba el endpoint de validacion rapida:
echo    curl -X POST http://localhost:8000/api/diagnosis/validate-fast ^
echo      -F "image=@test.jpg"
echo.
echo 3. Verifica logs para errores:
echo    tail -f backend/app.log
echo.

pause
