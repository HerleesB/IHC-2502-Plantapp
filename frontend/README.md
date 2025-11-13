# Jardín Inteligente - Aplicación Android

## Descripción
Aplicación móvil Android desarrollada en Kotlin con Jetpack Compose para el cuidado inteligente de plantas. 
Basada en el proyecto web React convertido a una experiencia móvil nativa.

## Características Principales

### 1. **Mi Jardín** 🌱
- Visualización de todas tus plantas registradas
- Indicadores de salud con progreso visual
- Racha de cuidado diario
- Próximas acciones programadas
- Estadísticas semanales de progreso

### 2. **Logros y Gamificación** 🏆
- Sistema de niveles y puntos de experiencia (XP)
- Misiones semanales con recompensas
- Colección de insignias desbloqueables
- Historial de logros conseguidos
- Racha de días consecutivos

### 3. **Captura Accesible** 📸
- Guía por voz paso a paso
- Retroalimentación háptica (vibración)
- Control por comandos de voz
- Análisis automático de:
  - Iluminación
  - Enfoque
  - Distancia óptima
- Modo completamente accesible para personas con discapacidad visual

### 4. **Comunidad** 👥
- Compartir casos de diagnóstico
- Publicaciones anónimas opcionales
- Sistema de reputación y puntos
- Casos resueltos y en progreso
- Top colaboradores del mes
- Diagnósticos asistidos por IA

## Tecnologías Persuasivas Implementadas

Según el documento PDF proporcionado, la aplicación implementa:

### Motivacionales
- ✅ Refuerzos positivos y mensajes empáticos
- ✅ Sistema de recompensas (insignias, puntos)
- ✅ Visualización de progreso
- ✅ Reconocimiento social en la comunidad

### Desencadenantes
- ✅ Recordatorios contextuales
- ✅ Notificaciones personalizadas
- ✅ Sugerencias automáticas para compartir logros

### Habilitadores
- ✅ Captura accesible con guía háptica
- ✅ Retroalimentación inmediata
- ✅ Confirmaciones auditivas

## Requisitos del Sistema

- **Android SDK:** 26 (Android 8.0) o superior
- **Target SDK:** 34 (Android 14)
- **Kotlin:** 1.9.20
- **Jetpack Compose:** BOM 2023.10.01

## Dependencias Principales

```gradle
// Core Android
- androidx.core:core-ktx:1.12.0
- androidx.lifecycle:lifecycle-runtime-ktx:2.6.2
- androidx.activity:activity-compose:1.8.1

// Compose
- androidx.compose.ui
- androidx.compose.material3
- androidx.compose.material:material-icons-extended

// Navigation
- androidx.navigation:navigation-compose:2.7.5

// CameraX (para captura de fotos)
- androidx.camera:camera-camera2
- androidx.camera:camera-lifecycle
- androidx.camera:camera-view

// Image Loading
- io.coil-kt:coil-compose:2.5.0

// Permissions
- com.google.accompanist:accompanist-permissions:0.32.0
```

## Estructura del Proyecto

```
app/
├── src/
│   └── main/
│       ├── java/com/jardin/inteligente/
│       │   ├── MainActivity.kt              # Actividad principal
│       │   ├── model/
│       │   │   └── Models.kt               # Modelos de datos
│       │   └── ui/
│       │       ├── MainScreen.kt           # Pantalla principal con navegación
│       │       ├── theme/                  # Tema de la app
│       │       │   ├── Color.kt
│       │       │   ├── Theme.kt
│       │       │   └── Type.kt
│       │       └── screens/                # Pantallas de la app
│       │           ├── MyGardenScreen.kt
│       │           ├── GamificationScreen.kt
│       │           ├── AccessibleCaptureScreen.kt
│       │           └── CommunityScreen.kt
│       ├── res/
│       │   └── values/
│       │       └── strings.xml
│       └── AndroidManifest.xml
└── build.gradle.kts
```

## Instalación y Configuración

### 1. Clonar o ubicar el proyecto
El proyecto se encuentra en: `C:\Users\user\Desktop\JardinInteligenApp`

### 2. Abrir en Android Studio
1. Abre Android Studio
2. Selecciona "Open an Existing Project"
3. Navega a `C:\Users\user\Desktop\JardinInteligenApp`

### 3. Sincronizar Gradle
- Android Studio sincronizará automáticamente las dependencias
- Si no lo hace, haz clic en "Sync Now" en la barra superior

### 4. Ejecutar la aplicación
1. Conecta un dispositivo Android o inicia un emulador
2. Haz clic en el botón "Run" (▶️) en Android Studio
3. Selecciona el dispositivo de destino

## Permisos Requeridos

La aplicación solicita los siguientes permisos:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

## Funcionalidades por Implementar (Futuras Mejoras)

1. **Integración con Backend**
   - API para diagnóstico de plantas con IA
   - Sincronización de datos en la nube
   - Sistema de autenticación de usuarios

2. **Funcionalidad de Cámara Real**
   - Implementación completa de CameraX
   - Captura y procesamiento de imágenes
   - Análisis de calidad de imagen

3. **Base de Datos Local**
   - Room Database para persistencia
   - Almacenamiento de plantas y diagnósticos offline

4. **Notificaciones Push**
   - Recordatorios de riego
   - Notificaciones de la comunidad
   - Alertas de misiones

5. **Reconocimiento de Voz**
   - Integración completa de Speech Recognition
   - Comandos de voz personalizados

## Conversión de React a Kotlin

### Principales Cambios

| React | Kotlin/Compose |
|-------|----------------|
| `useState` | `remember { mutableStateOf() }` |
| Componentes funcionales | `@Composable fun` |
| Props | Parámetros de función |
| CSS/Tailwind | Modifier chains |
| `useEffect` | `LaunchedEffect` / `DisposableEffect` |
| React Router | Navigation Compose |
| Lucide Icons | Material Icons |

### Equivalencias de UI

| React (shadcn/ui) | Compose (Material 3) |
|-------------------|---------------------|
| `Card` | `Card` |
| `Button` | `Button` / `OutlinedButton` |
| `Badge` | `AssistChip` |
| `Progress` | `LinearProgressIndicator` |
| `Switch` | `Switch` |
| `Tabs` | `TabRow` + `Tab` |

## Casos de Uso Implementados

Según el PDF del proyecto:

### ✅ Implementados
- **CU-01:** Captura guiada de foto (AccessibleCaptureScreen)
- **CU-03:** Recomendaciones accionables y plan semanal (MyGardenScreen)
- **CU-04:** Gestión de plantas y perfiles (MyGardenScreen)
- **CU-06:** Recordatorios + gamificación (GamificationScreen)
- **CU-07:** Publicar caso a la comunidad (CommunityScreen)
- **CU-08:** Inventario y progreso de plantas (MyGardenScreen)
- **CU-14:** Captura accesible de foto (AccessibleCaptureScreen)

### 🔄 Parcialmente Implementados
- **CU-02:** Diagnóstico automático + explicación LLM (UI preparada, pendiente backend)
- **CU-05:** Historial y tendencias (Estructura básica)
- **CU-09:** Respuesta y moderación asistida (UI básica)
- **CU-12:** Feedback/corrección del diagnóstico (Estructura preparada)

## Requerimientos Funcionales Persuasivos Cumplidos

✅ **RF-P1:** Recordatorios personalizados (estructura implementada)
✅ **RF-P2:** Recompensas simbólicas y medallas
✅ **RF-P3:** Mensajes empáticos y refuerzo positivo
✅ **RF-P4:** Visualizaciones de progreso
✅ **RF-P5:** Publicar y compartir casos con reconocimiento
✅ **RF-P6:** Solicitud de retroalimentación (estructura)
✅ **RF-P7:** Confirmaciones auditivas y hápticas
✅ **RF-P8:** Desafíos semanales y misiones

## Requerimientos No Funcionales Cumplidos

✅ **RNF-P2:** Lenguaje empático y motivador
✅ **RNF-P3:** Consistencia estética y emocional
✅ **RNF-P4:** Accesibilidad total (modo accesible completo)
✅ **RNF-P5:** Privacidad y consentimiento (opción anónima)
✅ **RNF-P7:** Ética persuasiva (sin manipulación)

## Notas de Desarrollo

### Simulaciones Actuales
Dado que es una conversión inicial, algunas funcionalidades están simuladas:

1. **Captura de foto:** Simula el proceso con delays y actualizaciones de estado
2. **Reconocimiento de voz:** Simula la escucha de comandos
3. **Imágenes:** Usa URLs de Unsplash como placeholder
4. **Datos:** Datos hardcodeados en lugar de API

### Para Producción
Deberás implementar:
- Servicio backend con API REST
- Modelo de IA para diagnóstico de plantas
- Base de datos local con Room
- Almacenamiento de imágenes (Firebase Storage o similar)
- Sistema de autenticación
- Analytics para medir efectividad de técnicas persuasivas

## Contacto y Soporte

Desarrollado para el proyecto: **Jardín Inteligente Conversacional**
Ciclo: 2025-II
Curso: Interacción Humano Computador [CC451]

---

**Nota:** Esta es una conversión inicial funcional. Se recomienda testing extensivo y ajustes específicos 
según los requerimientos completos del proyecto.
