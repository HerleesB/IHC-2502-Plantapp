package com.jardin.inteligente.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.jardin.inteligente.model.DiagnosisResponse
import com.jardin.inteligente.ui.screens.*
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.AuthViewModel
import com.jardin.inteligente.viewmodel.AuthViewModelFactory
import com.jardin.inteligente.viewmodel.AuthState

sealed class Screen(val route: String) {
    // Auth screens
    object AuthWelcome : Screen("auth_welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Main screens
    object Garden : Screen("garden")
    object Gamification : Screen("gamification")
    object Capture : Screen("capture/{plantId}") {
        fun createRoute(plantId: Int) = "capture/$plantId"
    }
    object Community : Screen("community")
    object CommunityShare : Screen("community_share")
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: Int) = "post_detail/$postId"
    }
    object History : Screen("history")
    object PlantDetail : Screen("plant_detail/{plantId}") {
        fun createRoute(plantId: Int) = "plant_detail/$plantId"
    }
    object DiagnosisDetail : Screen("diagnosis_detail/{diagnosisJson}") {
        fun createRoute(diagnosis: DiagnosisResponse): String {
            val json = java.net.URLEncoder.encode(Gson().toJson(diagnosis), "UTF-8")
            return "diagnosis_detail/$json"
        }
    }
    object DiagnosisResult : Screen("diagnosis_result")
    object AddPlantFromDiagnosis : Screen("add_plant_from_diagnosis")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val authState by authViewModel.authState.collectAsState()
    
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }
    var showBottomBar by remember { mutableStateOf(false) }
    var isGuestMode by remember { mutableStateOf(false) }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Determinar qué tabs mostrar según el modo
    val bottomNavItems = remember(isGuestMode) {
        if (isGuestMode) {
            // Modo invitado: solo Captura y Comunidad (solo lectura)
            listOf(
                BottomNavItem("Captura", Icons.Default.CameraAlt, "capture/0"),
                BottomNavItem("Comunidad", Icons.Default.Groups, "community")
            )
        } else {
            // Usuario autenticado: todas las pestañas
            listOf(
                BottomNavItem("Mi Jardín", Icons.Default.Spa, "garden"),
                BottomNavItem("Logros", Icons.Default.EmojiEvents, "gamification"),
                BottomNavItem("Captura", Icons.Default.CameraAlt, "capture/0"),
                BottomNavItem("Comunidad", Icons.Default.Groups, "community")
            )
        }
    }
    
    // Actualizar showBottomBar según la ruta actual
    LaunchedEffect(currentRoute) {
        showBottomBar = currentRoute in listOf(
            "garden", "gamification", "capture/0", "capture/{plantId}", "community"
        ) || currentRoute?.startsWith("capture/") == true
    }
    
    // Determinar destino inicial según estado de auth
    val startDestination = remember(authState) {
        when (authState) {
            is AuthState.Authenticated -> "garden"
            else -> "auth_welcome"
        }
    }

    Scaffold(
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = GreenPrimary
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(8.dp).size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Jardín Inteligente",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isGuestMode) "Modo Invitado" else "Tu asistente de plantas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isGuestMode) YellowWarning else Color.Gray
                                )
                            }
                        }
                    },
                    actions = {
                        if (!isGuestMode) {
                            IconButton(onClick = { navController.navigate("history") }) {
                                Icon(Icons.Default.History, "Historial")
                            }
                        }
                        // Botón de logout/salir
                        IconButton(
                            onClick = {
                                if (isGuestMode) {
                                    isGuestMode = false
                                    navController.navigate("auth_welcome") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    authViewModel.logout()
                                    navController.navigate("auth_welcome") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Salir",
                                tint = if (isGuestMode) YellowWarning else Color.Gray
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                navController.navigate(item.route) {
                                    popUpTo(if (isGuestMode) "capture/0" else "garden") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GreenPrimary,
                                selectedTextColor = GreenPrimary,
                                indicatorColor = GreenLight
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            // ========== AUTH SCREENS ==========
            composable("auth_welcome") {
                showBottomBar = false
                AuthWelcomeScreen(
                    onLoginClick = { navController.navigate("login") },
                    onRegisterClick = { navController.navigate("register") },
                    onGuestClick = {
                        isGuestMode = true
                        selectedTab = 0
                        navController.navigate("capture/0") {
                            popUpTo("auth_welcome") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("login") {
                showBottomBar = false
                LoginScreen(
                    onLoginSuccess = {
                        isGuestMode = false
                        selectedTab = 0
                        navController.navigate("garden") {
                            popUpTo("auth_welcome") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("register") {
                showBottomBar = false
                RegisterScreen(
                    onRegisterSuccess = {
                        isGuestMode = false
                        selectedTab = 0
                        navController.navigate("garden") {
                            popUpTo("auth_welcome") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate("login") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // ========== MAIN SCREENS ==========
            composable("garden") {
                showBottomBar = true
                if (isGuestMode) {
                    // Redirigir a captura si es invitado
                    LaunchedEffect(Unit) {
                        navController.navigate("capture/0") {
                            popUpTo("garden") { inclusive = true }
                        }
                    }
                } else {
                    MyGardenScreen(
                        onNavigateToDiagnosis = { plantId ->
                            navController.navigate(Screen.Capture.createRoute(plantId))
                        },
                        onNavigateToPlantDetail = { plantId ->
                            navController.navigate(Screen.PlantDetail.createRoute(plantId))
                        }
                    )
                }
            }
            
            composable("gamification") {
                showBottomBar = true
                if (isGuestMode) {
                    GuestRestrictedScreen(
                        feature = "Logros",
                        onLoginClick = {
                            navController.navigate("login")
                        }
                    )
                } else {
                    GamificationScreen()
                }
            }
            
            composable(
                route = "capture/{plantId}",
                arguments = listOf(navArgument("plantId") { type = NavType.IntType })
            ) { backStackEntry ->
                showBottomBar = true
                val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
                AccessibleCaptureScreen(
                    plantId = plantId,
                    isGuestMode = isGuestMode,
                    onDiagnosisComplete = { diagnosis ->
                        val route = Screen.DiagnosisDetail.createRoute(diagnosis)
                        navController.navigate(route)
                    },
                    onNavigateToAddPlant = { diagnosis ->
                        // Guardar diagnóstico temporalmente y navegar
                        navController.currentBackStackEntry?.savedStateHandle?.set("diagnosis", diagnosis)
                        navController.navigate("add_plant_from_diagnosis")
                    }
                )
            }
            
            composable("community") {
                showBottomBar = true
                CommunityScreen(
                    isGuestMode = isGuestMode,
                    onNavigateToShare = {
                        if (isGuestMode) {
                            // Mostrar mensaje de que necesita login
                        } else {
                            navController.navigate("community_share")
                        }
                    },
                    onNavigateToPostDetail = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    },
                    onLoginRequired = {
                        navController.navigate("login")
                    }
                )
            }
            
            composable("community_share") {
                showBottomBar = false
                if (isGuestMode) {
                    GuestRestrictedScreen(
                        feature = "Compartir en Comunidad",
                        onLoginClick = { navController.navigate("login") }
                    )
                } else {
                    CommunityShareScreen(
                        onPostSuccess = {
                            navController.popBackStack()
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
            
            // Nueva ruta para detalle del post
            composable(
                route = "post_detail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { backStackEntry ->
                showBottomBar = false
                val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                PostDetailScreen(
                    postId = postId,
                    isGuestMode = isGuestMode,
                    onNavigateBack = { navController.popBackStack() },
                    onLoginRequired = { navController.navigate("login") }
                )
            }
            
            composable("history") {
                showBottomBar = false
                HistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onDiagnosisClick = { diagnosis ->
                        val diagnosisResponse = DiagnosisResponse(
                            diagnosisId = diagnosis.id,
                            diagnosisText = diagnosis.diagnosisText,
                            diseaseName = diagnosis.diseaseName,
                            confidence = diagnosis.confidence,
                            severity = diagnosis.severity,
                            recommendations = diagnosis.recommendations,
                            weeklyPlan = emptyList()
                        )
                        val route = Screen.DiagnosisDetail.createRoute(diagnosisResponse)
                        navController.navigate(route)
                    }
                )
            }
            
            composable(
                route = "plant_detail/{plantId}",
                arguments = listOf(navArgument("plantId") { type = NavType.IntType })
            ) { backStackEntry ->
                showBottomBar = false
                val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
                PlantDetailScreen(
                    plantId = plantId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDiagnosis = {
                        navController.navigate(Screen.Capture.createRoute(plantId))
                    }
                )
            }
            
            composable(
                route = "diagnosis_detail/{diagnosisJson}",
                arguments = listOf(navArgument("diagnosisJson") { type = NavType.StringType })
            ) { backStackEntry ->
                showBottomBar = false
                val diagnosisJson = backStackEntry.arguments?.getString("diagnosisJson")
                val decodedJson = java.net.URLDecoder.decode(diagnosisJson, "UTF-8")
                val diagnosis = Gson().fromJson(decodedJson, DiagnosisResponse::class.java)
                
                DiagnosisDetailScreen(
                    diagnosis = diagnosis,
                    isGuestMode = isGuestMode,
                    onNavigateBack = { navController.popBackStack() },
                    onAddToGarden = {
                        if (!isGuestMode) {
                            navController.currentBackStackEntry?.savedStateHandle?.set("diagnosis", diagnosis)
                            navController.navigate("add_plant_from_diagnosis")
                        }
                    },
                    onShareToCommunity = { diagnosisId ->
                        if (!isGuestMode) {
                            navController.navigate("community_share")
                        }
                    }
                )
            }
            
            composable("add_plant_from_diagnosis") {
                showBottomBar = false
                val diagnosis = navController.previousBackStackEntry?.savedStateHandle?.get<DiagnosisResponse>("diagnosis")
                AddPlantFromDiagnosisScreen(
                    diagnosis = diagnosis,
                    onPlantAdded = {
                        navController.navigate("garden") {
                            popUpTo("capture/0") { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Pantalla que se muestra cuando un invitado intenta acceder a funcionalidad restringida
 */
@Composable
fun GuestRestrictedScreen(
    feature: String,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = YellowWarning
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Función Restringida",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Para acceder a \"$feature\" necesitas crear una cuenta o iniciar sesión.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Icon(Icons.Default.Login, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Sesión")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = BlueInfo.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = BlueInfo,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Con una cuenta podrás guardar tus plantas, ver tu historial, ganar logros y participar en la comunidad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)
