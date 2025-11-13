package com.jardin.inteligente.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jardin.inteligente.ui.screens.*
import com.jardin.inteligente.ui.theme.Green50
import com.jardin.inteligente.ui.theme.Green600
import com.jardin.inteligente.ui.theme.Green800

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Garden : Screen("garden", "Mi Jardín", Icons.Default.Spa)
    object Gamification : Screen("gamification", "Logros", Icons.Default.EmojiEvents)
    object Capture : Screen("capture", "Captura", Icons.Default.CameraAlt)
    object Community : Screen("community", "Comunidad", Icons.Default.Groups)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val screens = listOf(
        Screen.Garden,
        Screen.Gamification,
        Screen.Capture,
        Screen.Community
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = Green600
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
                                fontWeight = FontWeight.Bold,
                                color = Green800
                            )
                            Text(
                                text = "Tu asistente conversacional de plantas",
                                style = MaterialTheme.typography.bodySmall,
                                color = Green600
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green600,
                            selectedTextColor = Green600,
                            indicatorColor = Green50
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Green50,
                            Color.White
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> MyGardenScreen()
                1 -> GamificationScreen()
                2 -> AccessibleCaptureScreen()
                3 -> CommunityScreen()
            }
        }
    }
}
