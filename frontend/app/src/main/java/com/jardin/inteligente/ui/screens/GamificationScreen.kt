package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jardin.inteligente.model.AchievementResponse
import com.jardin.inteligente.model.MissionResponse
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.GamificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamificationScreen(
    viewModel: GamificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logros y Misiones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Error, null, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.error ?: "Error")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadData() }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Player stats
                        item {
                            uiState.achievements?.let { achievements ->
                                PlayerStatsCard(
                                    level = achievements.level,
                                    xp = achievements.xp,
                                    nextLevelXp = achievements.nextLevelXp,
                                    totalPoints = achievements.totalPoints,
                                    streakDays = uiState.missions?.streakDays ?: 0
                                )
                            }
                        }
                        
                        // Daily missions
                        item {
                            Text(
                                "Misiones Diarias",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        uiState.missions?.daily?.let { dailyMissions ->
                            items(dailyMissions) { mission ->
                                MissionCard(mission)
                            }
                        }
                        
                        // Achievements
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Logros Desbloqueados",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        uiState.achievements?.unlocked?.let { unlocked ->
                            items(unlocked) { achievement ->
                                AchievementCard(achievement, true)
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Logros Bloqueados",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                        uiState.achievements?.locked?.let { locked ->
                            items(locked) { achievement ->
                                AchievementCard(achievement, false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerStatsCard(
    level: Int,
    xp: Int,
    nextLevelXp: Int,
    totalPoints: Int,
    streakDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GreenLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = GreenPrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "$level",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Nivel $level", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("$totalPoints puntos totales", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, null, tint = YellowWarning)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$streakDays días", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // XP Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("XP: $xp / $nextLevelXp", style = MaterialTheme.typography.bodySmall)
                    Text("${(xp.toFloat() / nextLevelXp * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { xp.toFloat() / nextLevelXp },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = GreenPrimary
                )
            }
        }
    }
}

@Composable
fun MissionCard(mission: MissionResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.completed) GreenLight else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (mission.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                null,
                tint = if (mission.completed) GreenPrimary else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(mission.title, fontWeight = FontWeight.Bold)
                Text(mission.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = YellowWarning.copy(alpha = 0.1f))
            ) {
                Text(
                    "+${mission.xp} XP",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = YellowWarning,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: AchievementResponse, unlocked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) GreenLight else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (unlocked) GreenPrimary.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        achievement.icon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.name,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) Color.Black else Color.Gray
                )
                Text(
                    achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (unlocked) Color.Gray else Color.LightGray
                )
                if (!unlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Progreso: ${achievement.progress}/${achievement.progressMax}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Text(
                "+${achievement.points}",
                fontWeight = FontWeight.Bold,
                color = if (unlocked) GreenPrimary else Color.Gray
            )
        }
    }
}
