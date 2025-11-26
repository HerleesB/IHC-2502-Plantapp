# 🤖 MODELOS DE GROQ DISPONIBLES

## ⚠️ IMPORTANTE: Modelos con Visión

Para analizar imágenes, **DEBES usar un modelo con capacidad de visión**. No todos los modelos de Groq pueden procesar imágenes.

---

## ✅ MODELOS CON VISIÓN (Pueden Procesar Imágenes)

### 1. llama-3.2-90b-vision-preview
- **Parámetros**: 90 mil millones
- **Ventajas**: 
  - Más preciso en análisis de imágenes
  - Mejor comprensión de detalles complejos
  - Respuestas más elaboradas
- **Desventajas**:
  - Más lento (8-15 segundos por análisis)
  - Mayor consumo de tokens
- **Cuándo usar**: 
  - Diagnósticos complejos
  - Necesitas máxima precisión
  - No importa el tiempo de espera

### 2. llama-3.2-11b-vision-preview ⭐ RECOMENDADO
- **Parámetros**: 11 mil millones
- **Ventajas**:
  - Mucho más rápido (3-6 segundos por análisis)
  - Suficientemente preciso para validación de fotos
  - Menor consumo de tokens
- **Desventajas**:
  - Ligeramente menos detallado que el 90B
- **Cuándo usar**:
  - Validación de calidad de fotos (tu caso actual)
  - Respuestas rápidas
  - Experiencia de usuario fluida

---

## ❌ MODELOS SIN VISIÓN (Solo Texto)

Estos modelos **NO pueden procesar imágenes**:

- `llama-3.1-70b-versatile` (texto, recomendado para moderación)
- `llama-3.1-8b-instant` (texto, muy rápido)
- `llama-3.3-70b-versatile` (texto, versión mejorada)
- `mixtral-8x7b-32768` (texto, contexto largo)

---

## 🔧 CONFIGURACIÓN ACTUAL

Tu archivo `.env` ahora está configurado así:

```bash
# Modelo para análisis de IMÁGENES (validación de fotos)
GROQ_MODEL=llama-3.2-11b-vision-preview

# Modelo para TEXTO solamente (moderación de comentarios)
GROQ_TEXT_MODEL=llama-3.1-70b-versatile
```

**Esta es la configuración ÓPTIMA para tu app.**

---

## 📊 COMPARACIÓN DE RENDIMIENTO

| Aspecto | 11B Vision | 90B Vision |
|---------|------------|------------|
| Velocidad | ⚡⚡⚡ 3-6s | ⚡ 8-15s |
| Precisión | ⭐⭐⭐ Alta | ⭐⭐⭐⭐ Muy Alta |
| Tokens/Request | 💰 300-500 | 💰💰 500-800 |
| UX en App | ✅ Excelente | ⚠️ Aceptable |
| Recomendado para | Validación fotos | Diagnósticos complejos |

---

## 🚀 CÓMO CAMBIAR DE MODELO

### Opción 1: Editar el archivo `.env` (recomendado)

1. Abre: `backend/.env`
2. Cambia la línea:
   ```bash
   GROQ_MODEL=llama-3.2-11b-vision-preview
   ```
   Por:
   ```bash
   GROQ_MODEL=llama-3.2-90b-vision-preview
   ```
3. **Reinicia el backend** (Ctrl+C y volver a ejecutar)

### Opción 2: Variable de entorno temporal

```bash
# Windows PowerShell
$env:GROQ_MODEL="llama-3.2-90b-vision-preview"
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Windows CMD
set GROQ_MODEL=llama-3.2-90b-vision-preview
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

---

## 🧪 VERIFICAR MODELO ACTUAL

### Desde el navegador:

1. Ve a: http://localhost:8000/config
2. Busca: `"groq_model": "llama-3.2-11b-vision-preview"`

### Desde la consola del backend:

Cuando inicias el servidor, verás:
```
🤖 Modelo de Groq (Vision): llama-3.2-11b-vision-preview
📝 Modelo de Groq (Text): llama-3.1-70b-versatile
```

---

## ❓ ¿Por Qué No Usar Modelos Más Grandes?

**GPT-4 Vision, Claude 3.5 Sonnet, etc.**

Estos modelos **NO están disponibles en Groq** (al menos no en 2024-2025). Groq solo ofrece:
- Modelos de Meta (Llama)
- Modelos de Mistral
- Ningún modelo de OpenAI o Anthropic

---

## 💡 RECOMENDACIÓN PARA TU APP

Para la **validación de fotos** (tu caso actual):

✅ **USA: `llama-3.2-11b-vision-preview`**

**Razones:**
1. Suficientemente preciso para decir si una foto está bien centrada, iluminada, etc.
2. Respuesta en 3-6 segundos → buena experiencia de usuario
3. Menor costo en tokens
4. La app se siente más rápida y fluida

Para **diagnósticos completos** de enfermedades (futuro):

✅ **USA: `llama-3.2-90b-vision-preview`**

**Razones:**
1. Necesitas máxima precisión para identificar enfermedades
2. El usuario esperará más tiempo para un diagnóstico detallado
3. Vale la pena el tiempo extra por la calidad

---

## 🔄 CAMBIO DE CACHE

He actualizado `app/main.py` para que limpie el cache de configuración al iniciar:

```python
# Limpiar cache ANTES de cargar configuración
get_settings.cache_clear()
settings = get_settings()
```

Ahora los cambios en `.env` se cargarán correctamente al reiniciar el backend.

---

## ✅ TODO LISTO

Tu configuración actual es:

```
✅ Modelo con visión: llama-3.2-11b-vision-preview
✅ Modelo de texto: llama-3.1-70b-versatile
✅ Cache limpiado al inicio
✅ Configuración verificable en /config
```

**Reinicia el backend y deberías ver el modelo correcto en los logs.**

---

## 📞 Si Quieres Probar el Modelo 90B

1. Para el backend (Ctrl+C)
2. Edita `.env`:
   ```bash
   GROQ_MODEL=llama-3.2-90b-vision-preview
   ```
3. Reinicia el backend
4. Prueba la app
5. Compara el tiempo de respuesta

**Luego decide cuál prefieres** según la experiencia de usuario.

Mi recomendación: **Quédate con el 11B** para validación de fotos. Es perfecto para ese caso de uso. 🎯
