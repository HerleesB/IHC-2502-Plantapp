# FASE 1 COMPLETADA ✅
## Backend - API Endpoint de Validación de Fotos con IA

### 📋 Cambios Realizados

#### 1. **Función de Validación en groq_service.py**
- ✅ Agregada función `validate_photo_quality(image_bytes: bytes)`
- ✅ Usa el prompt especializado de `DiagnosisPrompts.get_centering_validation_prompt()`
- ✅ Analiza con Groq AI (temperatura 0.3 para consistencia)
- ✅ Retorna estructura con:
  - `success`: boolean (true si foto es aceptable)
  - `guidance`: string (mensaje personalizado de la IA)
  - `details`: objeto con scores de lighting, focus, distance, overall

#### 2. **Endpoint Actualizado en diagnosis.py**
- ✅ Ruta: `POST /api/diagnosis/capture-guidance`
- ✅ Acepta: `UploadFile` (imagen)
- ✅ Validaciones implementadas:
  - Formato de imagen correcto
  - Tamaño máximo 10MB
  - Manejo de errores robusto
- ✅ Retorna: Schema `CaptureGuidance`

#### 3. **Script de Prueba**
- ✅ Creado `test_photo_validation.py`
- ✅ Permite probar la validación con imágenes del directorio `uploads/`

---

### 🔌 API Endpoint

#### **POST** `/api/diagnosis/capture-guidance`

**Request:**
```http
POST /api/diagnosis/capture-guidance HTTP/1.1
Content-Type: multipart/form-data

image: [archivo de imagen]
```

**Response (Success - Foto Aprobada):**
```json
{
  "step": "validation",
  "message": "✅ ¡Excelente! Tu foto está lista para el diagnóstico",
  "success": true,
  "guidance": "Perfecto, la planta está bien encuadrada y enfocada",
  "audio_url": null
}
```

**Response (Warning - Foto Necesita Ajustes):**
```json
{
  "step": "validation",
  "message": "⚠️ La foto necesita algunos ajustes",
  "success": false,
  "guidance": "Acércate más a la planta y mejora la iluminación",
  "audio_url": null
}
```

**Response (Error):**
```json
{
  "detail": "El archivo debe ser una imagen"
}
```

---

### 🧪 Cómo Probar

#### Opción 1: Script de Prueba (Recomendado)

1. Coloca una imagen de planta en `backend/uploads/`
2. Ejecuta:
```bash
cd backend
python test_photo_validation.py
```

3. Verás el resultado completo del análisis de IA

#### Opción 2: Con cURL

```bash
curl -X POST "http://localhost:8000/api/diagnosis/capture-guidance" \
  -H "accept: application/json" \
  -H "Content-Type: multipart/form-data" \
  -F "image=@/ruta/a/tu/imagen.jpg"
```

#### Opción 3: Con FastAPI Docs

1. Inicia el servidor: `python -m app.main`
2. Ve a: http://localhost:8000/docs
3. Busca el endpoint `/api/diagnosis/capture-guidance`
4. Click en "Try it out"
5. Sube una imagen y ejecuta

---

### 🎯 Lógica de Validación

La IA analiza la foto según estos criterios:

1. **Centrado** (60% del área central)
2. **Visibilidad** (planta completa sin partes cortadas)
3. **Enfoque** (planta en foco, no el fondo)
4. **Iluminación** (ni sobreexpuesta ni subexpuesta)
5. **Distancia** (ni muy cerca ni muy lejos)

**Score General >= 0.7** → Foto APROBADA ✅
**Score General < 0.7** → Foto NECESITA AJUSTES ⚠️

---

### 📊 Estructura de Respuesta Detallada

```python
{
    "success": bool,           # True si foto aprobada
    "guidance": str,           # Mensaje de la IA (max 15 palabras)
    "details": {
        "lighting": float,     # 0.0 - 1.0
        "focus": float,        # 0.0 - 1.0
        "distance": float,     # 0.0 - 1.0
        "overall": float,      # 0.0 - 1.0
        "is_centered": bool,
        "plant_detected": bool,
        "issues": [str],
        "recommendations": {
            "direction": "center/up/down/left/right",
            "distance": "closer/farther/ok",
            "lighting": "more_light/less_light/ok",
            "focus": "refocus/ok"
        }
    }
}
```

---

### ⚠️ Consideraciones Importantes

1. **API Key de Groq**: Debe estar configurada en `.env`
   ```bash
   GROQ_API_KEY=tu_api_key_aqui
   ```

2. **Modelo usado**: `llama-3.2-90b-vision-preview` (configurado en settings)

3. **Timeout**: 30 segundos por defecto para el análisis

4. **Rate Limits**: Groq tiene límites de requests por minuto según tu plan

5. **Tokens consumidos**: Aproximadamente 300-500 tokens por validación

---

### 🔄 Próximos Pasos (Fase 2)

Ya completado en esta fase, pero para referencia:
- ✅ Servicio de Groq AI con función de validación
- ✅ Endpoint actualizado y funcional
- ⏭️ **Siguiente**: Fase 3 - Android App (Capa de Red)

---

### 🐛 Troubleshooting

**Error: "API Key no configurada"**
- Solución: Agrega `GROQ_API_KEY` en `backend/.env`

**Error: "Rate limit exceeded"**
- Solución: Espera unos minutos o verifica tu plan de Groq

**Error: "Timeout"**
- Solución: Verifica tu conexión a internet o aumenta el timeout en config

**Respuesta no es JSON válido**
- Solución: Ya manejado con fallback - revisa logs para más detalles

---

### 📝 Logs

Los logs de validación se encuentran en la consola con formato:
```
INFO - Validando imagen capturada (1234567 bytes)
INFO - Validación exitosa: Perfecto, la planta está bien encuadrada
```

---

## ✅ Estado: COMPLETADO

La Fase 1 está 100% funcional y lista para integrarse con la app Android.
