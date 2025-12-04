# 📋 Seguimiento de Casos de Uso - Jardín Inteligente Conversacional

**Proyecto:** CC451 - Interacción Humano Computador - Práctica Calificada 02  
**Ciclo:** 2025 – II  
**Profesor:** Ciro Javier Nuñez Iturri  
**Equipo:**
- Kevin Condor Chavez
- Cesar Sanchez Malaspina
- Herlees Barrientos Porras

---

## 📊 Resumen de Avance

| Estado | Cantidad | Porcentaje |
|--------|----------|------------|
| ✅ Completado | 0 | 0% |
| 🔄 En Progreso | 0 | 0% |
| ⏳ Pendiente | 14 | 100% |
| **Total** | **14** | - |

**Última actualización:** $(date)

---

## 🎯 User Personas

### P1: Alicia – Principiante ocupada
- **Contexto:** 27 años, departamento con luz media, 5 plantas comunes. Poco tiempo.
- **Metas:** Diagnóstico rápido, instrucciones simples, recordatorios.
- **Dolores:** No sabe si riega de más; fotos oscuras; se frustra con tecnicismos.

### P2: Bruno – Aficionado en progreso
- **Contexto:** 33 años, 15 plantas, le gusta experimentar.
- **Metas:** Diagnósticos más finos, aprender "por qué", comparar progreso.
- **Dolores:** Quiere evidencia (confianza/explicabilidad), historial y métricas.

### P3: Carla – Moderadora/experta comunitaria
- **Contexto:** 40 años, jardinera amateur respetada, disfruta ayudando.
- **Metas:** Responder casos difíciles, ver datos clave de la planta, evitar mitos.
- **Dolores:** Falta de contexto en preguntas, ruido/spam, repetición de dudas.

### P4: Diego – Educador urbano
- **Contexto:** Dicta talleres, usa la app en grupo.
- **Metas:** Cuentas de equipo/aula, rutas de aprendizaje, tableros comparativos.
- **Dolores:** Estandarizar capturas/fotos, medir mejoras por cohorte.

### P5: Elena – Usuaria ciega
- **Contexto:** 29 años, ceguera total; vive en un departamento con balcón y varias macetas. Utiliza un iPhone con VoiceOver y audífonos.
- **Metas:** Autonomía total para diagnosticar y cuidar sus plantas sin depender de ayuda visual; recibir instrucciones claras por voz.
- **Dolores:** Muchas apps no son compatibles con lectores de pantalla; botones pequeños o sin etiquetas.

---

## 📝 Requerimientos por Usuario

### P1 (Alicia) - Funcionales
| ID | Descripción | CU Relacionado | Estado |
|----|-------------|----------------|--------|
| F1 | Subir foto por voz ("toma una foto ahora") y recibir diagnóstico + 2-3 pasos claros | CU-01, CU-02 | ⏳ |
| F2 | Confianza/umbral visible y plan B (pedir otra foto si <0.6) | CU-01, CU-02 | ⏳ |
| F3 | Recordatorios y checklist personalizado (riego/luz) | CU-03, CU-04, CU-06 | ⏳ |
| F4 | Gamificación: rachas y medallas básicas | CU-06 | ⏳ |

### P1 (Alicia) - No Funcionales
| ID | Descripción | Estado |
|----|-------------|--------|
| N1 | Latencia percepción <1.5 s; lenguaje simple; accesible en móvil | ⏳ |

### P2 (Bruno) - Funcionales
| ID | Descripción | CU Relacionado | Estado |
|----|-------------|----------------|--------|
| F5 | Modo "avanzado": explicación breve, enlaces a recursos | CU-01, CU-02, CU-03 | ⏳ |
| F6 | Historial de diagnósticos y tendencias por planta | CU-05, CU-12 | ⏳ |
| F7 | Comparador antes/después (dos fotos) | CU-05 | ⏳ |

### P2 (Bruno) - No Funcionales
| ID | Descripción | Estado |
|----|-------------|--------|
| N2 | Transparencia: mostrar versión de modelo y confianza calibrada | ⏳ |

### P3 (Carla) - Funcionales
| ID | Descripción | CU Relacionado | Estado |
|----|-------------|----------------|--------|
| F8 | Vista "caso comunitario" con resumen LLM + foto + metadatos (especie, luz) | CU-07 | ⏳ |
| F9 | Responder y marcar "necesita más datos", plantillas de respuesta | CU-09 | ⏳ |
| F10 | Moderación asistida (LLM sugiere etiqueta/tema, detecta tono riesgoso) | CU-09 | ⏳ |

### P3 (Carla) - No Funcionales
| ID | Descripción | Estado |
|----|-------------|--------|
| N3 | Herramientas anti-spam, reportes, reputación | ⏳ |

### P4 (Diego) - Funcionales
| ID | Descripción | CU Relacionado | Estado |
|----|-------------|----------------|--------|
| F11 | Espacios de grupo (aula) con tablero de progreso y retos | CU-04, CU-06, CU-07 | ⏳ |
| F12 | Rutas de aprendizaje por especie/problema | CU-10 | ⏳ |

### P4 (Diego) - No Funcionales
| ID | Descripción | Estado |
|----|-------------|--------|
| N4 | Exportar resultados agregados (CSV) y privacidad por grupo | ⏳ |

### P5 (Elena) - Funcionales
| ID | Descripción | CU Relacionado | Estado |
|----|-------------|----------------|--------|
| F13 | Captura accesible de foto con guía por voz y vibración | CU-14 | ⏳ |
| F14 | Comandos por voz simples ("tomar foto", "repetir", "cancelar") | CU-14 | ⏳ |
| F15 | Confirmaciones auditivas breves luego de cada acción | CU-14 | ⏳ |

### P5 (Elena) - No Funcionales
| ID | Descripción | Estado |
|----|-------------|--------|
| N5 | Interfaz accesible compatible con VoiceOver/TalkBack, botones grandes y etiquetas ARIA | ⏳ |
| N6 | Latencia perceptible baja (≤1 s entre acción y respuesta) | ⏳ |

---

## 🔧 Casos de Uso del MVP (6-8 semanas)

### CU-01: Captura guiada de foto
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Asegurar imágenes útiles para el diagnóstico, garantizando buena iluminación, enfoque y encuadre |
| **Actores** | Usuario, LLM (guía), Cámara del dispositivo |
| **Precondición** | Sesión iniciada o modo invitado con permisos de cámara habilitados |
| **Requerimientos** | P1:F1, F2 \| P2:F5 |

**Flujo Principal:**
1. El usuario activa la función "Analizar planta" por voz o toque
2. El LLM evalúa condiciones de luz y sugiere ajustes ("La luz parece baja, ¿puedes acercarte a la ventana?")
3. El usuario toma la foto
4. El sistema valida nitidez y exposición
5. Si la foto no cumple criterios mínimos, se sugieren correcciones; de lo contrario, se pasa al diagnóstico (CU-02)

**Postcondición:** Se guarda la imagen validada junto con sus metadatos

**Análisis Crítico:**
- Riesgo de falsos negativos/positivos de calidad; frustración si reintenta mucho
- Sesgos por cámaras con HDR agresivo

**Aporte Personal:**
- Check de calidad multi-umbral (nitidez, exposición, relleno de encuadre) con mensajes amables y concretos
- Modo rápido para saltar guía si el usuario ya domina

**Tareas de Implementación:**
- [ ] Frontend: Implementar pantalla de captura con preview
- [ ] Frontend: Integrar validación de calidad de imagen
- [ ] Backend: Endpoint de validación de calidad
- [ ] Frontend: Feedback visual/auditivo de guía

---

### CU-02: Diagnóstico automático + explicación LLM
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Clasificar el problema de la planta mediante IA de visión y ofrecer una explicación comprensible |
| **Actores** | Modelo de visión, LLM |
| **Precondición** | Foto válida (CU-01) |
| **Requerimientos** | P1:F1, F2 \| P2:F5 \| N2 |

**Flujo Principal:**
1. Servicio de visión infiere labels + scores (p.ej., clorosis 0.86)
2. LLM redacta explicación corta ("amarilleo entre venas sugiere clorosis férrica")
3. Si confianza < 0.6, LLM activa plan B: solicitar nueva foto/ángulo o derivar a comunidad (CU-07)

**Postcondición:** Se guarda el diagnóstico, explicación y nivel de confianza

**Análisis Crítico:**
- Alucinaciones del LLM si no delimitamos contexto
- Over-trust del usuario ante un score alto en casos raros

**Aporte Personal:**
- Plantillas controladas para LLM (few-shot) y listas de chequeo por label
- Mostrar confianza calibrada (temperature scaling) y "Qué mirar para confirmar"

**Tareas de Implementación:**
- [ ] Backend: Endpoint de análisis con Groq Vision
- [ ] Backend: Generación de explicación con LLM
- [ ] Frontend: Pantalla de resultados del diagnóstico
- [ ] Frontend: Mostrar nivel de confianza
- [ ] Frontend: Botón "Continuar ->" funcional después de captura

---

### CU-03: Recomendaciones accionables y plan semanal
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Traducir el diagnóstico en un conjunto de acciones simples y personalizadas |
| **Actores** | LLM, módulo de recordatorios |
| **Precondición** | Diagnóstico válido generado en CU-02 |
| **Requerimientos** | P1:F1, F3 \| P2:F5 |

**Flujo Principal:**
1. Mapear label→playbook (p. ej., clorosis: quelato hierro, ajustar pH)
2. LLM personaliza a especie/ambiente ("interior, luz media")
3. Crear plan de 7 días con recordatorios (voz/notificación), enlaces y verificación al final

**Postcondición:** Plan de cuidado registrado y recordatorios activos

**Análisis Crítico:**
- Las recomendaciones pueden ser excesivas o peligrosas si el diagnóstico es dudoso

**Aporte Personal:**
- Guardrails: si confianza media, ofrecer alternativa no invasiva primero; exigir confirmación antes de químicos
- Mostrar la justificación de cada recomendación ("Por qué: déficit de hierro causa amarilleo")

**Tareas de Implementación:**
- [ ] Backend: Generación de plan semanal con LLM
- [ ] Backend: Sistema de recordatorios programables
- [ ] Frontend: Pantalla de plan de cuidado
- [ ] Frontend: Integración con notificaciones

---

### CU-04: Gestión de plantas y perfiles
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Permitir al usuario registrar, editar y administrar la información de sus plantas |
| **Actores** | Usuario, BD |
| **Precondición** | Usuario autenticado o con perfil activo |
| **Requerimientos** | P1:F3 \| P2:F6 \| P4:F11 |

**Flujo Principal:**
1. Crear/editar planta (especie, ubicación, maceta, fecha riego)
2. Asociar diagnósticos/planes a cada planta

**Postcondición:** Información de las plantas guardada y lista para consulta o actualización

**Análisis Crítico:**
- Posible fricción al ingresar datos manualmente

**Aporte Personal:**
- Autocompletar por foto (especie probable) y valores por defecto por especie/ubicación

**Tareas de Implementación:**
- [ ] Backend: CRUD completo de plantas
- [ ] Frontend: Pantalla "Mi Jardín" dinámica según usuario
- [ ] Frontend: Formulario de agregar/editar planta
- [ ] Backend: Asociar diagnósticos a plantas

---

### CU-06: Recordatorios + gamificación
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Motivar la constancia del usuario mediante recordatorios y recompensas simbólicas |
| **Actores** | Programador de tareas, LLM (mensajes empáticos) |
| **Precondición** | Plantas registradas en el sistema |
| **Requerimientos** | P1:F3, F4 \| P4:F11 |

**Flujo Principal:**
1. Crear recordatorios de riego/luz y retos (p. ej., "7 días sin sobre-riego")
2. Asignar insignias por hitos (recuperación, diagnóstico correcto a la primera)

**Postcondición:** Notificaciones activas y progreso gamificado del usuario

**Análisis Crítico:**
- Gamificación mal calibrada puede estresar o incentivar acciones de más

**Aporte Personal:**
- Rachas suaves (no punitivas), recordatorios adaptativos (si humedad alta, posponer riego)

**Tareas de Implementación:**
- [ ] Backend: Sistema de logros y XP
- [ ] Frontend: Pantalla "Logros" dinámica según usuario
- [ ] Backend: Sistema de rachas
- [ ] Frontend: Visualización de progreso

---

### CU-07: Publicar caso a la comunidad
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Permitir que los usuarios compartan casos y soliciten ayuda de la comunidad |
| **Actores** | Usuario, LLM (resumen), Comunidad |
| **Precondición** | Diagnóstico o foto disponible |
| **Requerimientos** | P3:F8 \| P4:F11 |

**Flujo Principal:**
1. Con un toque, se publica resumen del caso (foto, especie, luz, síntomas)
2. Opción de anonimizar datos y ocultar ubicación

**Postcondición:** Caso publicado en la comunidad

**Análisis Crítico:**
- Riesgo de exposición de datos personales o consejos incorrectos

**Aporte Personal:**
- Resumen estandarizado con etiquetas temáticas

**Tareas de Implementación:**
- [ ] Backend: Endpoint para publicar con imagen
- [ ] Frontend: Pantalla "Comunidad/Compartir" con subida de imagen
- [ ] Backend: Opción de publicación anónima
- [ ] Frontend: Vista de posts de otros usuarios
- [ ] **Requiere login para publicar**

---

### CU-08: Inventario y progreso de plantas
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Permitir al usuario visualizar y registrar el progreso de cada planta en su perfil o inventario |
| **Actores** | Usuario, Base de datos, Módulo de visión, LLM (para generación de resúmenes) |
| **Precondición** | El usuario tiene una o más plantas registradas (CU-04 completado) |
| **Requerimientos** | N2, N3 |

**Flujo Principal:**
1. El usuario accede a su perfil o sección "Mi jardín"
2. El sistema muestra una lista o galería de sus plantas con foto actual, nombre y estado
3. Al seleccionar una planta, se despliega su historial: diagnósticos, fotos anteriores y un gráfico de progreso
4. El usuario puede añadir una nueva foto o nota de seguimiento
5. El LLM genera un breve resumen automático del progreso

**Postcondición:** El sistema guarda el registro actualizado y muestra el progreso acumulado

**Análisis Crítico:**
- Riesgo de sobrecarga visual o confusión si se muestran demasiados datos
- Dependencia del usuario para mantener fotos actualizadas (riesgo de abandono)

**Aporte Personal:**
- Integrar notificaciones suaves tipo "¿Quieres registrar una nueva foto de tu ficus hoy?"
- Representar el progreso con íconos sencillos (hojas verdes, floración, etc.)
- Opción de compartir progreso con la comunidad (vinculado a CU-07)

**Tareas de Implementación:**
- [ ] Backend: Historial de diagnósticos por planta
- [ ] Frontend: Vista de detalle de planta con historial
- [ ] Backend: Generación de resumen de progreso con LLM
- [ ] Frontend: Gráfico/indicadores de progreso

---

### CU-09: Respuesta y moderación asistida
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Apoyar a moderadores y usuarios expertos en la revisión y respuesta de casos |
| **Actores** | Moderadores/usuarios expertos, LLM (asistente) |
| **Precondición** | Caso publicado en la comunidad (CU-07) |
| **Requerimientos** | P3:F9, F10 \| N3 |

**Flujo Principal:**
1. LLM sugiere temas/duplicados y plantillas de respuesta respetuosa
2. Señala riesgo (químicos mal usados) y pide confirmaciones

**Postcondición:** Respuestas publicadas con control de calidad y tono respetuoso

**Análisis Crítico:**
- Falsos positivos de moderación pueden desincentivar la participación

**Aporte Personal:**
- Controles manuales finales; explicar por qué se marcó un riesgo

**Tareas de Implementación:**
- [ ] Backend: Sistema de comentarios en posts
- [ ] Backend: Detección de contenido riesgoso con LLM
- [ ] Frontend: Interfaz de respuesta a posts
- [ ] Backend: Plantillas de respuesta sugeridas

---

### CU-12: Feedback/corrección del diagnóstico
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Permitir al usuario corregir o validar diagnósticos para mejorar el modelo |
| **Actores** | Usuario, Servicio de datos/ML |
| **Precondición** | Diagnóstico previo disponible |
| **Requerimientos** | P2:F5, F6 \| P3:F9 |

**Flujo Principal:**
1. Usuario marca "no era hongo, era falta de luz"
2. Se guarda ejemplo para re-entrenar; el LLM agradece y ajusta recomendaciones futuras

**Postcondición:** Ejemplo corregido almacenado con metadatos de usuario

**Análisis Crítico:**
- Riesgo de correcciones erróneas o inconsistentes

**Aporte Personal:**
- Confianza ponderada (más peso a usuarios con buena reputación/mods) y revisión por muestreo

**Tareas de Implementación:**
- [ ] Backend: Endpoint para feedback de diagnóstico
- [ ] Backend: Almacenamiento de correcciones
- [ ] Frontend: Interfaz para marcar diagnóstico como incorrecto
- [ ] Backend: Sistema de reputación de usuario

---

### CU-14: Captura accesible de foto (voz + hápticos + auto-disparo)
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Permitir que una persona ciega capture una foto diagnóstica válida usando guía por voz y vibración |
| **Actores** | Usuario (persona ciega), Asistente conversacional (LLM o módulo de voz), Cámara del dispositivo, Motor TTS/STT, Sistema háptico básico |
| **Precondición** | Modo accesible activado con permisos de cámara, micrófono y vibración |
| **Requerimientos** | P5:F13, F14, F15 \| N5, N6 |

**Flujo Principal:**
1. El usuario dice "Tomar foto de planta" o presiona un botón grande central
2. El sistema verifica niveles básicos de luz y enfoque
3. El asistente da retroalimentación mínima por voz y vibración:
   - Voz: "Acércate un poco" / "Hay poca luz" / "Perfecto, no te muevas."
   - Vibración corta → error (fuera de foco o contraluz)
   - Vibración larga → listo para capturar
4. Cuando las condiciones mínimas se cumplen, el sistema toma la foto automáticamente o tras confirmar con voz
5. El sistema confirma con un mensaje corto: "Foto tomada correctamente."
6. Se pasa al diagnóstico automático (CU-02)

**Postcondición:** Foto válida guardada con metadatos simples (luz, nitidez) y accesible para diagnóstico

**Análisis Crítico:**
- Latencia menor a 1 s entre comando y respuesta es esencial
- Evitar exceso de mensajes que interrumpan la fluidez

**Aporte Personal:**
- Mantener solo 3 patrones hápticos básicos (error, listo, confirmación) para simplicidad
- Mensajes TTS breves, precargados localmente
- Interfaz con un solo botón grande y etiquetas accesibles (ARIA)

**Tareas de Implementación:**
- [ ] Frontend: Modo accesible con TTS
- [ ] Frontend: Patrones hápticos configurados
- [ ] Frontend: Auto-disparo basado en condiciones
- [ ] Frontend: Botón grande y etiquetas ARIA

---

## 🆕 Casos de Uso Adicionales (Nuevos Requerimientos)

### CU-15: Sistema de Autenticación (Login/Registro/Invitado)
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Permitir a los usuarios registrarse, iniciar sesión o continuar como invitado |
| **Actores** | Usuario, Sistema de autenticación |
| **Precondición** | App instalada y con conexión a internet |
| **Requerimientos** | Nuevo - Necesario para CU-04, CU-06, CU-07, CU-08 |

**Flujo Principal - Login:**
1. Usuario ingresa username y contraseña
2. Sistema valida credenciales
3. Sistema genera token JWT y lo almacena de forma segura
4. Usuario accede a todas las funcionalidades

**Flujo Alternativo - Registro:**
1. Usuario ingresa datos de registro (username, email, contraseña)
2. Sistema valida datos únicos
3. Sistema crea cuenta y genera token JWT
4. Usuario accede a todas las funcionalidades

**Flujo Alternativo - Invitado:**
1. Usuario selecciona "Continuar como invitado"
2. Sistema crea sesión temporal sin persistencia
3. Usuario accede a funcionalidades limitadas (solo captura y diagnóstico)
4. No puede acceder a "Mi Jardín", "Logros" ni "Comunidad/Compartir"

**Postcondición:** Usuario autenticado o en modo invitado con acceso apropiado

**Tareas de Implementación:**
- [ ] Backend: Endpoints de login/registro (ya existentes - verificar)
- [ ] Frontend: Pantalla de Login
- [ ] Frontend: Pantalla de Registro
- [ ] Frontend: Opción "Continuar como invitado"
- [ ] Frontend: Persistencia de sesión con EncryptedSharedPreferences
- [ ] Frontend: Navegación condicional según estado de autenticación
- [ ] Frontend: Restricción de pestañas según modo (invitado vs autenticado)

---

### CU-16: Visualización Dinámica de "Mi Jardín"
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Mostrar el jardín personal del usuario con sus plantas y estados |
| **Actores** | Usuario autenticado, Sistema |
| **Precondición** | Usuario ha iniciado sesión (no modo invitado) |
| **Requerimientos** | Extensión de CU-04, CU-08 |

**Flujo Principal:**
1. Usuario navega a pestaña "Mi Jardín"
2. Sistema carga plantas del usuario desde el backend
3. Se muestra lista/galería con: foto, nombre, estado de salud, último riego, último diagnóstico
4. Usuario puede seleccionar una planta para ver detalles
5. Usuario puede agregar nueva planta

**Postcondición:** Usuario visualiza su inventario de plantas actualizado

**Tareas de Implementación:**
- [ ] Backend: Endpoint GET /api/plants/ con filtro por usuario
- [ ] Frontend: MyGardenScreen con carga dinámica
- [ ] Frontend: Cards de plantas con información resumida
- [ ] Frontend: Navegación a detalle de planta
- [ ] Frontend: Botón "Agregar planta" funcional

---

### CU-17: Visualización Dinámica de "Logros"
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Mostrar los logros, XP y nivel del usuario |
| **Actores** | Usuario autenticado, Sistema |
| **Precondición** | Usuario ha iniciado sesión (no modo invitado) |
| **Requerimientos** | Extensión de CU-06 |

**Flujo Principal:**
1. Usuario navega a pestaña "Logros"
2. Sistema carga datos de gamificación del usuario
3. Se muestra: nivel actual, XP, progreso al siguiente nivel, lista de logros (desbloqueados y bloqueados)
4. Usuario puede ver detalles de cada logro

**Postcondición:** Usuario visualiza su progreso en el sistema de gamificación

**Tareas de Implementación:**
- [ ] Backend: Endpoint GET /api/gamification/user-stats
- [ ] Frontend: GamificationScreen con carga dinámica
- [ ] Frontend: Barra de progreso de nivel
- [ ] Frontend: Lista de logros con estado

---

### CU-18: Publicación en Comunidad con Imagen
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Permitir a usuarios autenticados publicar casos con imágenes en la comunidad |
| **Actores** | Usuario autenticado, Sistema, Comunidad |
| **Precondición** | Usuario ha iniciado sesión (no modo invitado) |
| **Requerimientos** | Extensión de CU-07 |

**Flujo Principal:**
1. Usuario navega a "Comunidad" > "Compartir"
2. Usuario selecciona/captura imagen de planta
3. Usuario escribe descripción del caso
4. Usuario elige publicar con nombre o anónimamente
5. Sistema sube imagen y crea post
6. Post aparece en el feed de la comunidad

**Postcondición:** Post publicado visible para otros usuarios

**Tareas de Implementación:**
- [ ] Backend: Endpoint POST /api/community/posts con multipart (imagen)
- [ ] Frontend: Formulario de publicación con selector de imagen
- [ ] Frontend: Toggle para publicación anónima
- [ ] Frontend: Preview de imagen antes de publicar
- [ ] Frontend: Feedback de éxito/error

---

### CU-19: Feed de Comunidad
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Mostrar posts de la comunidad a todos los usuarios |
| **Actores** | Usuario (autenticado o invitado), Comunidad |
| **Precondición** | Ninguna (lectura pública) |
| **Requerimientos** | Extensión de CU-07 |

**Flujo Principal:**
1. Usuario navega a pestaña "Comunidad"
2. Sistema carga lista de posts recientes
3. Se muestra: imagen, autor (o "Anónimo"), descripción, likes, comentarios
4. Usuario puede dar like (si está autenticado)
5. Usuario puede comentar (si está autenticado)

**Postcondición:** Usuario visualiza el feed de la comunidad

**Tareas de Implementación:**
- [ ] Backend: Endpoint GET /api/community/posts
- [ ] Frontend: CommunityScreen con feed de posts
- [ ] Frontend: Card de post con imagen y datos
- [ ] Frontend: Botones de like/comentar (condicionales)

---

### CU-20: Flujo Completo de Diagnóstico (Captura → Análisis → Guardar)
**Estado:** ⏳ Pendiente

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Completar el flujo desde la captura de foto hasta guardar la planta en "Mi Jardín" |
| **Actores** | Usuario, Sistema de captura, IA de diagnóstico |
| **Precondición** | Usuario en pantalla de captura con foto lista |
| **Requerimientos** | Integración de CU-01, CU-02, CU-03, CU-04 |

**Flujo Principal:**
1. Usuario captura foto (CU-01)
2. Usuario presiona "Continuar ->"
3. Sistema analiza imagen con IA (CU-02)
4. Sistema muestra diagnóstico: problema detectado, confianza, explicación
5. Sistema genera recomendaciones (CU-03)
6. Usuario puede "Guardar en Mi Jardín"
7. Sistema pide datos adicionales (nombre de planta, ubicación)
8. Planta se guarda asociada al diagnóstico

**Postcondición:** Nueva planta en "Mi Jardín" con diagnóstico inicial

**Tareas de Implementación:**
- [ ] Frontend: Navegación de Captura → Diagnóstico
- [ ] Frontend: Pantalla de resultados de diagnóstico
- [ ] Frontend: Botón "Guardar en Mi Jardín"
- [ ] Frontend: Modal/pantalla para datos de planta
- [ ] Backend: Endpoint para crear planta con diagnóstico asociado

---

## 📅 Casos de Uso Fuera del MVP (Siguiente Iteración)

### CU-05: Historial, tendencias y comparador antes/después
**Estado:** 📋 Planeado (Post-MVP)

**Descripción:** Visualización de historial de diagnósticos y comparación de fotos para ver evolución de la planta.

**Requerimientos:** P2:F6, F7

---

### CU-10: Rutas de aprendizaje y micro-lecciones
**Estado:** 📋 Planeado (Post-MVP)

**Descripción:** Sistema de educación con rutas temáticas y lecciones cortas sobre cuidado de plantas.

**Requerimientos:** P4:F12 | P2:F5

---

### CU-11: Export/analítica para grupos (CSV, tablero)
**Estado:** 📋 Planeado (Post-MVP)

**Descripción:** Funcionalidad para educadores para exportar datos y ver tableros comparativos de grupos.

**Requerimientos:** P4:N4

---

### CU-13: Gestión de versiones de modelo y transparencia
**Estado:** 📋 Planeado (Post-MVP)

**Descripción:** Información sobre versión del modelo de IA usado y métricas de precisión.

**Requerimientos:** N2, N3

---

## 📊 Matriz de Cobertura CU → Requerimientos

| CU | F1 | F2 | F3 | F4 | F5 | F6 | F7 | F8 | F9 | F10 | F11 | F12 | F13 | F14 | F15 | N1 | N2 | N3 | N4 | N5 | N6 |
|----|----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|----|----|----|----|----|----|
| CU-01 | ✓ | ✓ | | | ✓ | | | | | | | | | | | ✓ | | | | | |
| CU-02 | ✓ | ✓ | | | ✓ | | | | | | | | | | | | ✓ | | | | |
| CU-03 | ✓ | | ✓ | | ✓ | | | | | | | | | | | | | | | | |
| CU-04 | | | ✓ | | | ✓ | | | | | ✓ | | | | | | | | | | |
| CU-06 | | | ✓ | ✓ | | | | | | | ✓ | | | | | | | | | | |
| CU-07 | | | | | | | | ✓ | | | ✓ | | | | | | | | | | |
| CU-08 | | | | | | | | | | | | | | | | | ✓ | ✓ | | | |
| CU-09 | | | | | | | | | ✓ | ✓ | | | | | | | | ✓ | | | |
| CU-12 | | | | | ✓ | ✓ | | | ✓ | | | | | | | | | | | | |
| CU-14 | | | | | | | | | | | | | ✓ | ✓ | ✓ | | | | | ✓ | ✓ |

---

## 🔄 Historial de Cambios

| Fecha | Versión | Cambios |
|-------|---------|---------|
| 2025-11-26 | 1.0.0 | Creación inicial del documento con todos los CU del PDF |
| 2025-11-26 | 1.1.0 | Agregados CU-15 a CU-20 (nuevos requerimientos de login, comunidad, flujo completo) |

---

## 📝 Notas de Implementación

### Prioridad de Implementación Sugerida

**Fase 1 - Core Auth & Navigation (Semana 1)**
1. CU-15: Sistema de Autenticación
2. Navegación condicional por estado de auth

**Fase 2 - Mi Jardín & Logros (Semana 2)**
3. CU-16: Mi Jardín dinámico
4. CU-17: Logros dinámicos
5. CU-04: Gestión de plantas

**Fase 3 - Captura & Diagnóstico (Semana 3-4)**
6. CU-01: Captura guiada
7. CU-02: Diagnóstico automático
8. CU-20: Flujo completo

**Fase 4 - Comunidad (Semana 5)**
9. CU-19: Feed de comunidad
10. CU-18: Publicación con imagen
11. CU-07: Publicar caso

**Fase 5 - Gamificación & Polish (Semana 6)**
12. CU-06: Recordatorios + gamificación
13. CU-03: Recomendaciones y plan semanal

**Fase 6 - Accesibilidad & Feedback (Semana 7-8)**
14. CU-14: Captura accesible
15. CU-08: Inventario y progreso
16. CU-09: Moderación asistida
17. CU-12: Feedback del diagnóstico

---

## 📂 Archivos Relacionados

- `frontend/` - Código de la aplicación Android (Kotlin/Jetpack Compose)
- `backend/` - API Backend (Python/FastAPI)
- `README.md` - Documentación general del proyecto
- `PC2.docx.md` - Documento original de la práctica calificada
