# ❌ ERROR: "La conexión tardó demasiado"

## 🎯 SOLUCIÓN RÁPIDA

### ⚡ Opción Más Rápida (5 minutos)

1. **Ejecuta `OBTENER_IP.bat`** (doble click en este proyecto)
   - Te mostrará tu IP (ej: 192.168.1.105)

2. **Edita `app/src/main/java/com/jardin/inteligente/network/ApiConfig.kt`**
   ```kotlin
   private const val USE_EMULATOR = false  // ← CAMBIAR A false
   private const val LOCAL_IP = "192.168.1.105"  // ← TU IP AQUÍ
   ```

3. **Desactiva Firewall temporalmente** (solo para probar)
   - Panel de Control → Firewall → Desactivar (red privada)

4. **Verifica que el backend esté corriendo:**
   ```bash
   cd backend
   .venv\Scripts\activate
   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```

5. **Rebuild la app** (Shift + F10 en Android Studio)

6. **Prueba en tu teléfono**

---

## ✅ CHECKLIST

Marca cada item cuando lo completes:

- [ ] Backend está corriendo (ves mensajes de `Uvicorn running`)
- [ ] Ejecutaste `OBTENER_IP.bat` y obtuviste tu IP
- [ ] Cambiaste `USE_EMULATOR = false` en `ApiConfig.kt`
- [ ] Pusiste tu IP en `LOCAL_IP` en `ApiConfig.kt`
- [ ] PC y teléfono están en la MISMA red WiFi
- [ ] Firewall está desactivado o puerto 8000 permitido
- [ ] Hiciste Rebuild de la app
- [ ] Instalaste la app actualizada en el teléfono

---

## 🔍 ¿Cómo Verificar si Funciona?

### En el Backend (PC):
Cuando captures una foto, deberías ver algo como:
```
INFO: 192.168.1.X:XXXXX - "POST /api/diagnosis/capture-guidance HTTP/1.1" 200 OK
2025-11-12 11:20:15 - app.routes.diagnosis - INFO - Validando imagen capturada (2456789 bytes)
```

### En la App (Teléfono):
1. Foto se captura ✅
2. Aparece "Analizando con IA..." con spinner ✅
3. Después de 3-8 segundos aparece:
   - Card verde: "✅ Foto aprobada" + mensaje de IA
   - Card amarilla: "⚠️ Necesita ajustes" + mensaje de IA

---

## 🆘 Si No Funciona

### Problema 1: Backend no recibe nada
**Causa**: No están en la misma red WiFi
**Solución**: 
- Verifica WiFi del teléfono: Configuración → WiFi
- Verifica WiFi de PC: Panel de Control → Red
- Deben ser la MISMA red

### Problema 2: "Connection refused"
**Causa**: Backend no está corriendo
**Solución**: Inicia el backend con:
```bash
cd backend
.venv\Scripts\activate
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### Problema 3: "Unknown host"
**Causa**: IP incorrecta
**Solución**: 
- Ejecuta `OBTENER_IP.bat` de nuevo
- Copia la IP correcta a `ApiConfig.kt`

### Problema 4: Firewall bloqueando
**Solución**:
1. Panel de Control
2. Firewall de Windows Defender
3. Desactivar (solo red privada)
4. Probar app
5. **Importante**: Reactivar después

---

## 🌐 Alternativa: Usar ngrok (Si nada funciona)

Si absolutamente nada funciona, puedes exponer el backend a internet:

1. Descarga ngrok: https://ngrok.com/download
2. Extrae el archivo `ngrok.exe`
3. Abre CMD en esa carpeta y ejecuta:
   ```bash
   ngrok http 8000
   ```
4. Copia la URL que te da (ej: `https://abc123.ngrok-free.app`)
5. En `ApiConfig.kt`:
   ```kotlin
   val BASE_URL = "https://abc123.ngrok-free.app/"
   ```
6. Rebuild la app

**Ventaja**: Funciona desde cualquier red (incluso datos móviles)
**Desventaja**: URL temporal, cambia cada vez

---

## 📱 IP Local Común por Red

Tu IP normalmente será una de estas:
- Router TP-Link: `192.168.0.X`
- Router Movistar: `192.168.1.X`
- Router Telmex: `192.168.1.X`
- Otros: `192.168.X.X` o `10.0.0.X`

La X final es única para tu dispositivo (ej: 105, 102, etc.)

---

## 🎯 Configuración Final Correcta

Tu `ApiConfig.kt` debe quedar así:

```kotlin
object ApiConfig {
    private const val USE_EMULATOR = false  // false para dispositivo físico
    private const val LOCAL_IP = "192.168.1.105"  // TU IP aquí
    
    val BASE_URL: String = when {
        USE_EMULATOR -> "http://10.0.2.2:8000/"
        else -> "http://$LOCAL_IP:8000/"
    }
    
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    const val ENABLE_LOGGING = true
}
```

Con `USE_EMULATOR = false`, la URL será: `http://192.168.1.105:8000/`

---

## ✨ Resultado Esperado

Una vez todo configurado correctamente:

1. **Abres la app** → Pestaña "Captura"
2. **Presionas "Tomar foto"** → Se abre cámara
3. **Capturas la planta** → Foto aparece en la app
4. **Esperas 3-8 segundos** → Spinner girando
5. **Mensaje de IA aparece**:
   - ✅ "Perfecto, la planta está bien encuadrada y enfocada"
   - ⚠️ "Acércate más a la planta y mejora la iluminación"
6. **Guía por voz** lee el mensaje
7. **Vibración** confirma resultado

---

## 📞 ¿Necesitas Más Ayuda?

Si después de seguir todos los pasos sigue sin funcionar:

1. Verifica que puedas abrir en el navegador de tu PC:
   - http://localhost:8000/docs ✅
   - http://TU_IP:8000/docs ✅

2. Desde el navegador de tu TELÉFONO:
   - http://TU_IP:8000/docs ✅

Si el paso 1 funciona pero el 2 no → Problema de Firewall
Si ni el 1 ni el 2 funcionan → Backend no está corriendo

---

🎉 **¡Buena suerte!** Una vez configurado, funcionará perfectamente.
