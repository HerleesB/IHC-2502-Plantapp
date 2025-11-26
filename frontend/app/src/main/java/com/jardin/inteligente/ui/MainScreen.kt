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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.jardin.inteligente.model.DiagnosisResponse
import com.jardin.inteligente.ui.screens.*
import com.jardin.inteligente.ui.theme.*

sealed class Screen(val route: String) {
    object Garden : Screen("garden")
    object Gamification : Screen("gamification")
    object Capture : Screen("capture/{plantId}") {
        fun createRoute(plantId: Int) = "capture/$plantId"
    }
    object Community : Screen("community")
    object History : Screen("history")
    object DiagnosisDetail : Screen("diagnosis_detail/{diagnosisJson}") {
        fun createRoute(diagnosis: DiagnosisResponse): String {
            val json = Gson().toJson(diagnosis)
            return "diagnosis_detail/${json}"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }
    var showBottomBar by remember { mutableStateOf(true) }
    
    val bottomNavItems = listOf(
        BottomNavItem("Mi Jardín", Icons.Default.Spa, "garden"),
        BottomNavItem("Logros", Icons.Default.EmojiEvents, "gamification"),
        BottomNavItem("Captura", Icons.Default.CameraAlt, "capture/0"),
        BottomNavItem("Comunidad", Icons.Default.Groups, "community")
    )

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
                                    text = "Tu asistente de plantas",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("history") }) {
                            Icon(Icons.Default.History, "Historial")
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
                                    popUpTo("garden") { inclusive = false }
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
            startDestination = "garden",
            modifier = Modifier.padding(padding)
        ) {
            composable("garden") {
                showBottomBar = true
                MyGardenScreen(
                    onNavigateToDiagnosis = { plantId ->
                        navController.navigate("capture/$plantId")
                    }
                )
            }
            
            composable("gamification") {
                showBottomBar = true
                GamificationScreen()
            }
            
            composable(
                route = "capture/{plantId}",
                arguments = listOf(navArgument("plantId") { type = NavType.IntType })
            ) { backStackEntry ->
                showBottomBar = true
                val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
                AccessibleCaptureScreen(
                    plantId = plantId,
                    onDiagnosisComplete = { diagnosis ->
                        val route = Screen.DiagnosisDetail.createRoute(diagnosis)
                        navController.navigate(route)
                    }
                )
            }
            
            composable("community") {
                showBottomBar = true
                CommunityScreen()
            }
            
            composable("history") {
                showBottomBar = false
                HistoryScreen(
                    onDiagnosisClick = { diagnosis ->
                        // Convert to DiagnosisResponse format
                        val diagnosisResponse = DiagnosisResponse(
                            diagnosisId = diagnosis.id,
                            diagnosisText = diagnosis.diagnosisText,
                            diseaseName = diagnosis.diseaseName,
                            confidence = diagnosis.confidence,
                            severity = diagnosis.severity,
                            recommendations = diagnosis.recommendations,
                            weeklyPlan = emptyList() // TODO: Get from API
                        )
                        val route = Screen.DiagnosisDetail.createRoute(diagnosisResponse)
                        navController.navigate(route)
                    }
                )
            }
            
            composable(
                route = "diagnosis_detail/{diagnosisJson}",
                arguments = listOf(navArgument("diagnosisJson") { type = NavType.StringType })
            ) { backStackEntry ->
                showBottomBar = false
                val diagnosisJson = backStackEntry.arguments?.getString("diagnosisJson")
                val diagnosis = Gson().fromJson(diagnosisJson, DiagnosisResponse::class.java)
                
                DiagnosisDetailScreen(
                    diagnosis = diagnosis,
                    onNavigateBack = { navController.popBackStack() }
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
