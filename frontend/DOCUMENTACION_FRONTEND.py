"""
Script para generar documentación del frontend Android
"""
import os

base_path = r"frontend\app\src\main\java\com\jardin\inteligente"

print("=" * 80)
print("  📱 DOCUMENTACIÓN DEL FRONTEND ANDROID - KOTLIN")
print("=" * 80)

# Estructura de carpetas y su propósito
estructura = {
    "model": {
        "descripcion": "📦 MODELOS DE DATOS",
        "proposito": "Clases que representan la estructura de datos de la aplicación",
        "archivos": ["Models.kt", "ApiModels.kt"]
    },
    "network": {
        "descripcion": "🌐 CAPA DE RED",
        "proposito": "Configuración de Retrofit y servicios API",
        "archivos": ["ApiService.kt", "ApiConfig.kt"]
    },
    "repository": {
        "descripcion": "🗄️ REPOSITORIOS",
        "proposito": "Capa intermedia entre ViewModels y API (patrón Repository)",
        "archivos": ["CommunityRepository.kt"]
    },
    "viewmodel": {
        "descripcion": "🎛️ VIEW MODELS",
        "proposito": "Lógica de negocio y gestión de estado (patrón MVVM)",
        "archivos": [
            "AuthViewModel.kt",
            "CommunityViewModel.kt",
            "CaptureViewModel.kt",
            "MyGardenViewModel.kt",
            "GamificationViewModel.kt"
        ]
    },
    "ui/screens": {
        "descripcion": "🖼️ PANTALLAS (UI)",
        "proposito": "Composables de Jetpack Compose - Interfaz de usuario",
        "archivos": ["CommunityScreen.kt", "CommunityShareScreen.kt"]
    }
}

for folder, info in estructura.items():
    print(f"\n{'=' * 80}")
    print(f"{info['descripcion']}: {folder}/")
    print('=' * 80)
    print(f"📝 Propósito: {info['proposito']}")
    print(f"\n📄 Archivos principales:")
    for archivo in info['archivos']:
        print(f"   • {archivo}")

print("\n" + "=" * 80)
print("  🏗️ ARQUITECTURA MVVM")
print("=" * 80)
print("""
┌──────────────────┐
│   UI (Screen)    │ ← Usuario interactúa aquí
└────────┬─────────┘
         │ Observa LiveData/StateFlow
         ▼
┌──────────────────┐
│    ViewModel     │ ← Lógica de negocio
└────────┬─────────┘
         │ Llama métodos
         ▼
┌──────────────────┐
│   Repository     │ ← Obtiene datos
└────────┬─────────┘
         │ Hace requests HTTP
         ▼
┌──────────────────┐
│   ApiService     │ ← Retrofit (Red)
└────────┬─────────┘
         │ JSON
         ▼
┌──────────────────┐
│   Backend API    │ ← FastAPI (Python)
└──────────────────┘
""")

print("\n" + "=" * 80)
print("  📋 EXPLICACIÓN DETALLADA POR ARCHIVO")
print("=" * 80)

explicaciones = {
    "ApiModels.kt": """
    🎯 QUÉ ES: Data classes que representan la estructura de datos del API
    
    📦 CONTIENE:
       • Request models: Datos que SE ENVÍAN al backend
         - LoginRequest, PlantCreateRequest, etc.
       
       • Response models: Datos que SE RECIBEN del backend
         - UserResponse, PlantResponse, DiagnosisResponse, etc.
       
       • @SerializedName: Mapea nombres de JSON a Kotlin
         Ejemplo: @SerializedName("image_url") val imageUrl: String
    
    🔍 FUNCIÓN PRINCIPAL: Definir contratos de datos entre frontend y backend
    """,
    
    "ApiService.kt": """
    🎯 QUÉ ES: Interface de Retrofit que define todos los endpoints del API
    
    📡 CONTIENE:
       • @POST, @GET, @PUT, @DELETE: Verbos HTTP
       • @Multipart: Para subir archivos (imágenes)
       • @FormUrlEncoded: Para datos de formulario
       • Suspend functions: Para llamadas asíncronas con coroutines
    
    📝 EJEMPLO:
       @GET("api/plants/user/{user_id}")
       suspend fun getUserPlants(@Path("user_id") userId: Int): List<PlantResponse>
    
    🔍 FUNCIÓN PRINCIPAL: Definir CÓMO se comunica con el backend
    """,
    
    "ApiConfig.kt": """
    🎯 QUÉ ES: Configuración de Retrofit (cliente HTTP)
    
    ⚙️ CONTIENE:
       • BASE_URL: URL del servidor backend
       • OkHttpClient: Cliente HTTP con interceptores
       • Retrofit.Builder: Constructor del cliente Retrofit
       • Gson: Conversor JSON ↔ Objetos Kotlin
    
    🔍 FUNCIÓN PRINCIPAL: Crear instancia única de ApiService (Singleton)
    """,
    
    "CommunityViewModel.kt": """
    🎯 QUÉ ES: Gestiona la lógica y estado de la pantalla de Comunidad
    
    🎛️ CONTIENE:
       • StateFlow/LiveData: Estado reactivo observado por la UI
       • Funciones para cargar posts, dar likes, comentar
       • Manejo de errores
       • Coroutines para operaciones asíncronas
    
    📝 EJEMPLO:
       val posts = MutableStateFlow<List<Post>>(emptyList())
       
       fun loadPosts() {
           viewModelScope.launch {
               val result = repository.getPosts()
               posts.value = result
           }
       }
    
    🔍 FUNCIÓN PRINCIPAL: Separar lógica de la UI (patrón MVVM)
    """,
    
    "CommunityRepository.kt": """
    🎯 QUÉ ES: Capa intermedia entre ViewModel y ApiService
    
    🗄️ CONTIENE:
       • Métodos que llaman a ApiService
       • Manejo de errores centralizado
       • Transformación de datos si es necesario
       • Caching (opcional)
    
    📝 EJEMPLO:
       suspend fun getPosts(): Result<List<Post>> {
           return try {
               val response = apiService.getCommunityPosts()
               Result.success(response)
           } catch (e: Exception) {
               Result.failure(e)
           }
       }
    
    🔍 FUNCIÓN PRINCIPAL: Abstraer la fuente de datos del ViewModel
    """,
    
    "CommunityScreen.kt": """
    🎯 QUÉ ES: Pantalla de Comunidad usando Jetpack Compose
    
    🖼️ CONTIENE:
       • @Composable functions: Componentes de UI reutilizables
       • LazyColumn: Lista eficiente de posts
       • Estado observado del ViewModel
       • Gestión de eventos del usuario (clicks, swipes)
       • Coil/Glide: Carga de imágenes
    
    📝 EJEMPLO:
       @Composable
       fun CommunityScreen(viewModel: CommunityViewModel) {
           val posts by viewModel.posts.collectAsState()
           
           LazyColumn {
               items(posts) { post ->
                   PostItem(post)
               }
           }
       }
    
    🔍 FUNCIÓN PRINCIPAL: Renderizar la UI y reaccionar a cambios de estado
    """
}

for archivo, explicacion in explicaciones.items():
    print(f"\n{'=' * 80}")
    print(f"📄 {archivo}")
    print('=' * 80)
    print(explicacion)

print("\n" + "=" * 80)
print("  🔄 FLUJO DE DATOS COMPLETO")
print("=" * 80)
print("""
EJEMPLO: Cargar publicaciones de la comunidad

1. Usuario abre CommunityScreen
   └─ UI llama → viewModel.loadPosts()

2. CommunityViewModel.loadPosts()
   └─ ViewModel llama → repository.getPosts()

3. CommunityRepository.getPosts()
   └─ Repository llama → apiService.getCommunityPosts()

4. ApiService hace HTTP GET
   └─ Retrofit envía → GET http://192.168.18.213:8000/api/community/posts

5. Backend responde con JSON
   └─ [{id: 1, plant_name: "Rosa", ...}, {...}]

6. Gson convierte JSON → List<CommunityPostResponse>
   └─ Repository recibe datos

7. Repository devuelve datos al ViewModel
   └─ ViewModel actualiza StateFlow

8. UI (CommunityScreen) observa cambio en StateFlow
   └─ Jetpack Compose RE-RENDERIZA la lista de posts

9. Usuario VE las publicaciones en pantalla
   └─ Coil carga las imágenes de forma asíncrona
""")

print("\n" + "=" * 80)
print("  🎨 JETPACK COMPOSE (UI Declarativa)")
print("=" * 80)
print("""
En lugar de XML, usas funciones @Composable en Kotlin:

ANTES (XML):
<TextView
    android:text="Hola Mundo"
    android:textSize="20sp" />

AHORA (Compose):
@Composable
fun Greeting() {
    Text(
        text = "Hola Mundo",
        fontSize = 20.sp
    )
}

VENTAJAS:
✅ Menos código boilerplate
✅ Más fácil de mantener
✅ Reactivo por defecto
✅ Preview en Android Studio
""")

print("\n" + "=" * 80)
print("  🔧 HERRAMIENTAS Y BIBLIOTECAS CLAVE")
print("=" * 80)
print("""
📚 DEPENDENCIAS PRINCIPALES:

1. Retrofit - Cliente HTTP
   └─ Para hacer requests al backend
   └─ https://square.github.io/retrofit/

2. Gson - Serialización JSON
   └─ Convierte JSON ↔ Objetos Kotlin
   └─ Usado por Retrofit

3. Coroutines - Programación asíncrona
   └─ suspend functions, viewModelScope.launch
   └─ Evita bloquear el hilo principal

4. Jetpack Compose - UI moderna
   └─ @Composable, LazyColumn, Text, Image
   └─ UI declarativa y reactiva

5. ViewModel - Gestión de estado
   └─ Sobrevive a cambios de configuración
   └─ Patrón MVVM

6. Coil/Glide - Carga de imágenes
   └─ AsyncImage, cache, transformaciones
   └─ Carga eficiente desde URLs

7. Navigation Compose - Navegación
   └─ NavHost, NavController
   └─ Navegación entre pantallas
""")

print("\n" + "=" * 80)
print("✅ DOCUMENTACIÓN COMPLETADA")
print("=" * 80)
