# **CC451 \- Interacción Humano Computador**

# **Lectura 06**

Ciclo: 2025 – II

Profesor: CIRO JAVIER NUÑEZ ITURRI

Alumnos: 

* Kevin Condor Chavez  
* Cesar Sanchez Malaspina  
* Herlees Barrientos Porras

Tema: Jardín inteligente conversacional

Contenido  
[Tema del proyecto	2](#tema-del-proyecto)

[User Personas (con contexto y metas)	2](#user-personas-\(con-contexto-y-metas\))

[Requerimientos por usuario (funcionales \+ no funcionales)	3](#requerimientos-por-usuario-\(funcionales-+-no-funcionales\))

[Casos de uso (CU) y cobertura de requerimientos	4](#casos-de-uso-\(cu\)-y-cobertura-de-requerimientos)

[CU a implementar en el MVP (6–8 semanas)	4](#cu-a-implementar-en-el-mvp-\(6–8-semanas\))

[Detalle de CU seleccionados (con análisis crítico y aporte personal)	5](#detalle-de-cu-seleccionados-\(con-análisis-crítico-y-aporte-personal\))

[CU-01: Captura guiada de foto	5](#cu-01:-captura-guiada-de-foto)

[CU-02: Diagnóstico automático \+ explicación LLM	5](#cu-02:-diagnóstico-automático-+-explicación-llm)

[CU-03: Recomendaciones accionables y plan semanal	6](#cu-03:-recomendaciones-accionables-y-plan-semanal)

[CU-04: Gestión de plantas y perfiles	7](#cu-04:-gestión-de-plantas-y-perfiles)

[CU-06: Recordatorios \+ gamificación	7](#cu-06:-recordatorios-+-gamificación)

[CU-07: Publicar caso a la comunidad	8](#cu-07:-publicar-caso-a-la-comunidad)

[CU-08: Inventario y progreso de plantas	8](#cu-08:-inventario-y-progreso-de-plantas)

[CU-09: Respuesta y moderación asistida	9](#cu-09:-respuesta-y-moderación-asistida)

[CU-12: Feedback/corrección del diagnóstico	9](#cu-12:-feedback/corrección-del-diagnóstico)

[CU-14: Captura accesible de foto (voz \+ hápticos \+ auto-disparo)	10](#cu-14:-captura-accesible-de-foto-\(voz-+-hápticos-+-auto-disparo\))

[Diagrama de Gantt	11](#diagrama-de-gantt)

[Prototipos:	12](#prototipos)

# Tema del proyecto {#tema-del-proyecto}

**Jardín inteligente conversacional**: una app web/móvil con interfaz **voz-first** que, a partir de una **foto** y diálogo, **detecta problemas** en plantas (clorosis, hongos, plagas, riego/luz inadecuados) y entrega **recomendaciones accionables**. Usa **IA de Visión** (modelo propio o API) y **LLM** para explicar en lenguaje natural, guiar pasos y sostener una conversación empática.

**Módulos obligatorios (≥4/5)**

1. **Personalización continua**: perfil por planta (especie, ubicación, historial), preferencias del usuario, aprendizaje con feedback.

2. **Interactividad entre usuarios**: comunidad ligera (compartir diagnósticos, pedir segundas opiniones, responder).

3. **Gamificación**: rachas de cuidado, insignias (“rescate exitoso”), retos semanales.

4. **Ayuda contextual**: guías paso a paso, checklists de buena foto, tooltips por voz.  
   *(Hápticos opcional: vibración en recordatorios.)*

# User Personas (con contexto y metas) {#user-personas-(con-contexto-y-metas)}

**P1: Alicia – Principiante ocupada**

* **Contexto**: 27 años, depto con luz media, 5 plantas comunes. Poco tiempo.

* **Metas**: diagnóstico rápido, instrucciones simples, recordatorios.

* **Dolores**: no sabe si riega de más; fotos oscuras; se frustra con tecnicismos.

**P2: Bruno – Aficionado en progreso**

* **Contexto**: 33 años, 15 plantas, le gusta experimentar.

* **Metas**: diagnósticos más finos, aprender “por qué”, comparar progreso.

* **Dolores**: quiere evidencia (confianza/explicabilidad), historial y métricas.

**P3: Carla – Moderadora/experta comunitaria**

* **Contexto**: 40 años, jardinera amateur respetada, disfruta ayudando.

* **Metas**: responder casos difíciles, ver datos clave de la planta, evitar mitos.

* **Dolores**: falta de contexto en preguntas, ruido/spam, repetición de dudas.

**P4: Diego – Educador urbano**

* **Contexto**: dicta talleres, usa la app en grupo.

* **Metas**: cuentas de equipo/aula, rutas de aprendizaje, tableros comparativos.

* **Dolores**: estandarizar capturas/fotos, medir mejoras por cohorte.

**P5: Elena – Usuaria ciega** 

* **Contexto:** 29 años, ceguera total; vive en un departamento con balcón y varias macetas. Utiliza un iPhone con VoiceOver y audífonos. Prefiere comandos de voz y respuestas habladas, además de guías hápticas.  
* **Metas:** autonomía total para diagnosticar y cuidar sus plantas sin depender de ayuda visual; recibir instrucciones claras por voz, confirmaciones auditivas y recordatorios con vibración.  
* **Dolores:** muchas apps no son compatibles con lectores de pantalla; botones pequeños o sin etiquetas; mensajes ambiguos sobre luz o enfoque; dificultad para saber si la foto fue tomada correctamente.

# Requerimientos por usuario (funcionales \+ no funcionales) {#requerimientos-por-usuario-(funcionales-+-no-funcionales)}

**P1 (Alicia)**

* **F1** Subir foto por voz (“toma una foto ahora”) y recibir **diagnóstico** \+ **2-3 pasos** claros.

* **F2** **Confianza/umbral** visible y plan B (pedir otra foto si \<0.6).

* **F3** **Recordatorios** y checklist personalizado (riego/luz).

* **F4** **Gamificación**: rachas y medallas básicas.

* **N1** Latencia percepción \<1.5 s; lenguaje simple; accesible en móvil.

**P2 (Bruno)**

* **F5** Modo “avanzado”: explicación breve, enlaces a recursos.

* **F6** Historial de diagnósticos y **tendencias** por planta.

* **F7** **Comparador antes/después** (dos fotos).

* **N2** Transparencia: mostrar versión de modelo y **confianza calibrada**.

**P3 (Carla)**

* **F8** Vista “caso comunitario” con **resumen LLM** \+ foto \+ metadatos (especie, luz).

* **F9** **Responder** y marcar “necesita más datos”, plantillas de respuesta.

* **F10** **Moderación asistida** (LLM sugiere etiqueta/tema, detecta tono riesgoso).

* **N3** Herramientas anti-spam, reportes, reputación.

**P4 (Diego)**

* **F11** **Espacios de grupo** (aula) con tablero de progreso y retos.

* **F12** **Rutas de aprendizaje** por especie/problema.

* **N4** Exportar resultados agregados (CSV) y privacidad por grupo.

**P5 (Elena)**

* **F13 Captura accesible de foto** con guía por **voz y vibración**, validando condiciones básicas de luz y enfoque antes del disparo automático.  
* **F14 Comandos por voz simples**, como “tomar foto”, “repetir” o “cancelar”, sin depender de interfaz visual.  
* **F15 Confirmaciones auditivas** breves luego de cada acción (“Foto tomada correctamente”, “Demasiado oscuro”).  
* **N5 Interfaz accesible** compatible con **VoiceOver/TalkBack**, con botones grandes y etiquetas ARIA.  
* **N6 Latencia perceptible baja** (≤1 s entre acción y respuesta) y mensajes TTS pregrabados para funcionar sin conexión a internet.

# Casos de uso (CU) y cobertura de requerimientos {#casos-de-uso-(cu)-y-cobertura-de-requerimientos}

**CU-01**: Captura guiada de foto (voz \+ checklist) → P1:F1, F2 | P2:F5  
**CU-02**: Diagnóstico automático (visión) \+ explicación LLM → P1:F1, F2 | P2:F5 | N2  
**CU-03**: Recomendaciones accionables y plan semanal → P1:F1, F3 | P2:F5  
**CU-04**: Gestión de plantas y perfiles (especie, ubicación) → P1:F3 | P2:F6 | P4:F11  
**CU-05**: Historial, tendencias y comparador antes/después → P2:F6, F7  
**CU-06**: Recordatorios \+ gamificación (rachas, insignias) → P1:F3, F4 | P4:F11  
**CU-07**: Publicar caso a la comunidad (con anonimización opcional) → P3:F8 | P4:F11  
**CU-08**:Inventario y progreso de plantas → N2, N3  
**CU-09**: Respuesta y moderación asistida (LLM) → P3:F9, F10 | N3  
**CU-10**: Rutas de aprendizaje y micro-lecciones → P4:F12 | P2:F5  
**CU-11**: Export/analítica para grupos (CSV, tablero) → P4:N4  
**CU-12**: Feedback/corrección del diagnóstico (active learning) → P2:F5, F6 | P3:F9  
**CU-13**: Gestión de versiones de modelo y transparencia → N2, N3  
**CU-14:** Captura accesible de foto (voz \+ vibración \+ auto-disparo) → P5:F13, F14, F15 | N5, N6

**Cobertura**: los CU 1–13 cubren todos los F/N listados para P1–P4.

# CU a implementar en el MVP (6–8 semanas) {#cu-a-implementar-en-el-mvp-(6–8-semanas)}

**Elegidos**: **CU-01, CU-02, CU-03, CU-04, CU-06, CU-07, CU-08, CU-09, CU-12 y CU-14.**  
(Dejamos para siguiente iteración: CU-05 comparador, CU-10 rutas avanzadas, CU-11 export/analítica, CU-13 gobernanza de versiones UI completa.)

# Detalle de CU seleccionados (con análisis crítico y aporte personal) {#detalle-de-cu-seleccionados-(con-análisis-crítico-y-aporte-personal)}

## CU-01: Captura guiada de foto {#cu-01:-captura-guiada-de-foto}

**Objetivo**: Asegurar imágenes útiles para el diagnóstico, garantizando buena iluminación, enfoque y encuadre.  
**Actores**: Usuario, LLM (guía), Cámara del dispositivo.  
**Precondición:**  
 Sesión iniciada o modo invitado con permisos de cámara habilitados.

**Flujo principal:**

1. El usuario activa la función “Analizar planta” por voz o toque.  
2. El LLM evalúa condiciones de luz y sugiere ajustes (“La luz parece baja, ¿puedes acercarte a la ventana?”).  
3. El usuario toma la foto.  
4. El sistema valida nitidez y exposición.  
5. Si la foto no cumple criterios mínimos, se sugieren correcciones; de lo contrario, se pasa al diagnóstico (CU-02).

**Postcondición:**

Se guarda la imagen validada junto con sus metadatos.

**Análisis crítico (respuestas del sistema)**

* Riesgo de **falsos negativos/positivos** de calidad; frustración si reintenta mucho.

* Sesgos por cámaras con HDR agresivo.  
  **Aporte personal**

* **Check de calidad multi-umbral** (nitidez, exposición, relleno de encuadre) con mensajes **amables y concretos** (“acerca 10 cm y evita contraluz”).

* **Modo rápido** para saltar guía si el usuario ya domina.

## CU-02: Diagnóstico automático \+ explicación LLM {#cu-02:-diagnóstico-automático-+-explicación-llm}

**Objetivo**:Clasificar el problema de la planta mediante IA de visión y ofrecer una explicación comprensible.  
**Actores**: Modelo de visión, LLM.  
**Precondición**: foto válida (CU-01).  
**Flujo**

1. Servicio de visión infiere **labels \+ scores** (p.ej., clorosis 0.86).

2. LLM redacta **explicación corta** (“amarilleo entre venas sugiere clorosis férrica”).

3. Si **confianza \< 0.6**, LLM activa plan B: solicitar nueva foto/ángulo o derivar a comunidad (CU-07).  
   **Post**: diagnóstico \+ explicación \+ confianza.

**Postcondición:**

Se guarda el diagnóstico, explicación y nivel de confianza.

**Análisis crítico**

* **Alucinaciones** del LLM si no delimitamos contexto.

* **Over-trust** del usuario ante un score alto en casos raros.  
  **Aporte personal**

* **Plantillas controladas** para LLM (few-shot) y **listas de chequeo** por label.

* Mostrar **confianza calibrada** (temperature scaling) y “**Qué mirar para confirmar**”.

## CU-03: Recomendaciones accionables y plan semanal {#cu-03:-recomendaciones-accionables-y-plan-semanal}

**Objetivo**: Traducir el diagnóstico en un conjunto de acciones simples y personalizadas.  
**Actores**: LLM, módulo de recordatorios.

**Precondición:**

Diagnóstico válido generado en CU-02.  
**Flujo**

1. Mapear label→**playbook** (p. ej., clorosis: quelato hierro, ajustar pH).

2. LLM personaliza a especie/ambiente (“interior, luz media”).

3. Crear **plan de 7 días** con recordatorios (voz/notificación), enlaces y verificación al final.

**Postcondición:**

Plan de cuidado registrado y recordatorios activos.

**Análisis crítico**

* Las recomendaciones pueden ser **excesivas o peligrosas** si el diagnóstico es dudoso.  
  **Aporte personal**

* **Guardrails**: si confianza media, ofrecer alternativa **no invasiva** primero; exigir confirmación antes de químicos.

* Mostrar la justificación de cada recomendación (“Por qué: déficit de hierro causa amarilleo”).

## CU-04: Gestión de plantas y perfiles {#cu-04:-gestión-de-plantas-y-perfiles}

**Objetivo**: Permitir al usuario registrar, editar y administrar la información de sus plantas.  
**Actores**: Usuario, BD.

**Precondición:**

Usuario autenticado o con perfil activo.  
**Flujo**

1. Crear/editar planta (especie, ubicación, maceta, fecha riego).

2. Asociar diagnósticos/planes a cada planta.

**Postcondición:**

Información de las plantas guardada y lista para consulta o actualización

**Análisis crítico**

* Posible fricción al ingresar datos manualmente.

  **Aporte personal**

**Autocompletar por foto** (especie probable) y **valores por defecto** por especie/ubicación.

## CU-06: Recordatorios \+ gamificación {#cu-06:-recordatorios-+-gamificación}

**Objetivo**: Motivar la constancia del usuario mediante recordatorios y recompensas simbólicas.  
**Actores**: Programador de tareas, LLM (mensajes empáticos).

**Precondición:**

Plantas registradas en el sistema.  
**Flujo**

1. Crear recordatorios de riego/luz y **retos** (p. ej., “7 días sin sobre-riego”).

2. Asignar **insignias** por hitos (recuperación, diagnóstico correcto a la primera).

**Postcondición:**

Notificaciones activas y progreso gamificado del usuario.

**Análisis crítico**

* Gamificación mal calibrada puede **estresar** o incentivar acciones de más.  
  **Aporte personal**

* **Rachas suaves** (no punitivas), **recordatorios adaptativos** (si humedad alta, posponer riego).

## CU-07: Publicar caso a la comunidad {#cu-07:-publicar-caso-a-la-comunidad}

**Objetivo**: Permitir que los usuarios compartan casos y soliciten ayuda de la comunidad.  
**Actores**: Usuario, LLM (resumen), Comunidad.

**Precondición:**

Diagnóstico o foto disponible.  
**Flujo**

1. Con un toque, se publica **resumen del caso** (foto, especie, luz, síntomas).

2. Opción de **anonimizar** datos y ocultar ubicación.

**Postcondición:**  
 Caso publicado en la comunidad.

**Análisis crítico:**

* Riesgo de exposición de datos personales o consejos incorrectos.

**Aporte personal:**

* Resumen estandarizado con etiquetas temáticas.

## CU-08: Inventario y progreso de plantas {#cu-08:-inventario-y-progreso-de-plantas}

**Objetivo:** Permitir al usuario visualizar y registrar el progreso de cada planta en su perfil o inventario, mostrando métricas, fotos históricas y evolución del estado (salud, crecimiento, diagnósticos pasados).

**Actores:** Usuario, Base de datos, Módulo de visión, LLM (para generación de resúmenes).

**Precondición:** El usuario tiene una o más plantas registradas (CU-04 completado).

**Flujo principal:**

1. El usuario accede a su perfil o sección “Mi jardín”.  
2. El sistema muestra una lista o galería de sus plantas con foto actual, nombre y estado (“en recuperación”, “saludable”, etc.).  
3. Al seleccionar una planta, se despliega su historial: diagnósticos, fotos anteriores y un gráfico de progreso (por IA o autoevaluación).  
4. El usuario puede añadir una nueva foto o nota de seguimiento (“nuevas hojas”, “menos manchas”).  
5. El LLM genera un breve resumen automático del progreso (“Tu planta ha mejorado un 20% en color y densidad en 2 semanas”).

**Postcondición:**  
 El sistema guarda el registro actualizado y muestra el progreso acumulado de cada planta.

**Análisis crítico:**

* Riesgo de sobrecarga visual o confusión si se muestran demasiados datos o gráficos.  
* Dependencia del usuario para mantener fotos actualizadas (riesgo de abandono).

**Aporte personal:**

* Integrar notificaciones suaves tipo “¿Quieres registrar una nueva foto de tu ficus hoy?” basadas en la última actividad.  
* Representar el progreso con íconos sencillos (hojas verdes, floración, etc.) más que con métricas técnicas.  
* Opción de compartir progreso con la comunidad (vinculado a CU-07).

## CU-09: Respuesta y moderación asistida {#cu-09:-respuesta-y-moderación-asistida}

**Objetivo**: Apoyar a moderadores y usuarios expertos en la revisión y respuesta de casos.  
**Actores**: Moderadores/usuarios expertos, LLM (asistente).

**Precondición:**

Caso publicado en la comunidad (CU-07).  
**Flujo**

1. LLM sugiere **temas/duplicados** y **plantillas** de respuesta respetuosa.

2. Señala **riesgo** (químicos mal usados) y pide confirmaciones.

**Postcondición:**

Respuestas publicadas con control de calidad y tono respetuoso.

**Análisis crítico**

* Falsos positivos de moderación pueden **desincentivar la participación**.  
  **Aporte personal**

* Controles manuales finales; explicar por qué se marcó un riesgo.

## CU-12: Feedback/corrección del diagnóstico {#cu-12:-feedback/corrección-del-diagnóstico}

**Objetivo**: Permitir al usuario corregir o validar diagnósticos para mejorar el modelo.  
**Actores**: Usuario, Servicio de datos/ML.

**Precondición:** Diagnóstico previo disponible.  
**Flujo**

1. Usuario marca “no era hongo, era falta de luz”.

2. Se guarda ejemplo para **re-entrenar**; el LLM agradece y ajusta recomendaciones futuras.

**Postcondición:**

Ejemplo corregido almacenado con metadatos de usuario.  
	**Análisis crítico**

* Riesgo de **erróneas o inconsistentes.**  
  **Aporte personal**

* **Confianza ponderada** (más peso a usuarios con buena reputación/mods) y **revisión por muestreo**.

### 

## CU-14: Captura accesible de foto (voz \+ hápticos \+ auto-disparo) {#cu-14:-captura-accesible-de-foto-(voz-+-hápticos-+-auto-disparo)}

**Objetivo:**  
Permitir que una persona ciega capture una foto diagnóstica válida usando guía por voz y vibración, de forma sencilla y rápida, sin depender de asistencia visual.

**Actores:**  
Usuario (persona ciega), Asistente conversacional (LLM o módulo de voz), Cámara del dispositivo, Motor TTS/STT, Sistema háptico básico.

**Precondición:**  
Modo accesible activado con permisos de cámara, micrófono y vibración.

**Flujo**

1. El usuario dice **“Tomar foto de planta”** o presiona un botón grande central.  
2. El sistema verifica **niveles básicos de luz y enfoque**.  
3. El asistente da **retroalimentación mínima por voz y vibración**:  
   * Voz: “Acércate un poco” / “Hay poca luz” / “Perfecto, no te muevas.”  
   * Vibración corta → error (fuera de foco o contraluz).  
   * Vibración larga → listo para capturar.  
4. Cuando las condiciones mínimas se cumplen, el sistema **toma la foto automáticamente** (auto-disparo) o tras confirmar con la voz del usuario (“Listo”).  
5. El sistema confirma con un mensaje corto: **“Foto tomada correctamente.”**  
6. Se pasa al diagnóstico automático (CU-02).

**Postcondición:**  
Foto válida guardada con metadatos simples (luz, nitidez) y accesible para diagnóstico.

**Análisis crítico**

* Latencia menor a 1 s entre comando y respuesta es esencial para buena experiencia.  
* Evitar exceso de mensajes que interrumpan la fluidez del proceso.

**Aporte personal**

* Mantener solo **3 patrones hápticos básicos** (error, listo, confirmación) para simplicidad.  
* Mensajes TTS breves, precargados localmente para evitar dependencia de red.  
* Interfaz con **un solo botón grande y etiquetas accesibles (ARIA)** para reducir errores.

# Diagrama de Gantt {#diagrama-de-gantt}

<img width="682" height="654" alt="image" src="https://github.com/user-attachments/assets/e2a1a63c-44d5-45c2-9462-1d1cb082f98c" />


# Prototipos {#prototipos}

1.-[https://symbol-web-17146116.figma.site/](https://symbol-web-17146116.figma.site/) 
<img width="612" height="412" alt="image" src="https://github.com/user-attachments/assets/ddc6627f-5cc8-4134-a8e9-5b618e464f48" />

	
2\.[https://jade-clasp-80742332.figma.site/](https://jade-clasp-80742332.figma.site/)
<img width="618" height="425" alt="image" src="https://github.com/user-attachments/assets/0c287132-bd3e-4944-af53-b85f9403e3de" />


3\.[https://hurry-seam-03940975.figma.site/](https://hurry-seam-03940975.figma.site/)
<img width="618" height="425" alt="image" src="https://github.com/user-attachments/assets/cdf89481-19b7-4fff-96f1-47809e233b17" />

