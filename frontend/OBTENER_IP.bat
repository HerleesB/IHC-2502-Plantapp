@echo off
echo ========================================
echo   OBTENER IP LOCAL PARA ANDROID APP
echo ========================================
echo.
echo Buscando tu direccion IP local...
echo.

ipconfig | findstr /i "IPv4"

echo.
echo ========================================
echo.
echo INSTRUCCIONES:
echo 1. Copia la IP que aparece arriba (ejemplo: 192.168.1.105)
echo 2. Abre el archivo: app\src\main\java\com\jardin\inteligente\network\ApiConfig.kt
echo 3. Cambia estas lineas:
echo.
echo    private const val USE_EMULATOR = false
echo    private const val LOCAL_IP = "TU_IP_AQUI"
echo.
echo 4. Rebuild la app en Android Studio
echo.
echo ========================================
echo.
pause
