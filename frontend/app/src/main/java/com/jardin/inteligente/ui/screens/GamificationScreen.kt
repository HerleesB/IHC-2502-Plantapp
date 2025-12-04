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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jardin.inteligente.model.AchievementResponse
import com.jardin.inteligente.model.MissionResponse
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.GamificationViewModel
import com.jardin.inteligente.viewmodel.GamificationViewModelFactory

/**
 * CU-06, CU-17: Pantalla de Logros y Misiones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamificationScreen() {
    val context = LocalContext.current
    val viewModel: GamificationViewModel = viewModel(
        factory = GamificationViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logros y Misiones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, "Actualizar", tint = Color.White)
                    }
                }
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
                        Icon(
                            Icons.Default.Error, 
                            null, 
                            modifier = Modifier.size(48.dp),
                            tint = RedError
                        )
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
                            PlayerStatsCard(
                                level = uiState.achievements?.level ?: 1,
                                xp = uiState.achievements?.xp ?: 0,
                                nextLevelXp = uiState.achievements?.nextLevelXp ?: 100,
                                totalPoints = uiState.achievements?.totalPoints ?: 0,
                                streakDays = uiState.missions?.streakDays ?: 0
                            )
                        }
                        
                        // Daily missions
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Misiones Diarias",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                val completedDaily = uiState.missions?.daily?.count { it.completed } ?: 0
                                val totalDaily = uiState.missions?.daily?.size ?: 0
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (completedDaily == totalDaily && totalDaily > 0)
                                            GreenLight else Color(0xFFF5F5F5)
                                    )
                                ) {
                                    Text(
                                        "$completedDaily/$totalDaily",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (completedDaily == totalDaily && totalDaily > 0)
                                            GreenPrimary else Color.Gray
                                    )
                                }
                            }
                        }
                        
                        uiState.missions?.daily?.let { dailyMissions ->
                            if (dailyMissions.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.EmojiEvents,
                                                null,
                                                modifier = Modifier.size(40.dp),
                                                tint = GreenPrimary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("¡Todas las misiones completadas!")
                                        }
                                    }
                                }
                            } else {
                                items(dailyMissions) { mission ->
                                    MissionCard(mission)
                                }
                            }
                        }
                        
                        // Weekly missions
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Misiones Semanales",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                val completedWeekly = uiState.missions?.weekly?.count { it.completed } ?: 0
                                val totalWeekly = uiState.missions?.weekly?.size ?: 0
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = BlueInfo.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Text(
                                        "$completedWeekly/$totalWeekly",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = BlueInfo
                                    )
                                }
                            }
                        }
                        
                        uiState.missions?.weekly?.let { weeklyMissions ->
                            items(weeklyMissions) { mission ->
                                MissionCard(mission, isWeekly = true)
                            }
                        }
                        
                        // Unlocked Achievements
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Logros Desbloqueados",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                val unlockedCount = uiState.achievements?.unlocked?.size ?: 0
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = GreenLight)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.EmojiEvents,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = GreenPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "$unlockedCount",
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary
                                        )
                                    }
                                }
                            }
                        }
                        
                        uiState.achievements?.unlocked?.let { unlocked ->
                            if (unlocked.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Lock,
                                                null,
                                                modifier = Modifier.size(40.dp),
                                                tint = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Aún no has desbloqueado logros", color = Color.Gray)
                                            Text("¡Completa misiones para desbloquearlos!", 
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(unlocked) { achievement ->
                                    AchievementCard(achievement, true)
                                }
                            }
                        }
                        
                        // Locked Achievements
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Próximos Logros",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                        
                        uiState.achievements?.locked?.let { locked ->
                            items(locked.take(5)) { achievement ->  // Mostrar solo los primeros 5
                                AchievementCard(achievement, false)
                            }
                            
                            if (locked.size > 5) {
                                item {
                                    TextButton(
                                        onClick = { /* TODO: Mostrar todos */ },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ver ${locked.size - 5} logros más")
                                    }
                                }
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
                        Text(
                            "Nivel $level", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$totalPoints puntos totales", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                
                // Streak
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (streakDays > 0) YellowWarning.copy(alpha = 0.2f) else Color(0xFFF5F5F5)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment, 
                            null, 
                            tint = if (streakDays > 0) YellowWarning else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "$streakDays", 
                            fontWeight = FontWeight.Bold,
                            color = if (streakDays > 0) YellowWarning else Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // XP Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "XP: $xp / $nextLevelXp", 
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        "${((xp.toFloat() / nextLevelXp) * 100).toInt()}%", 
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { xp.toFloat() / nextLevelXp },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = GreenPrimary,
                    trackColor = Color.White
                )
            }
        }
    }
}

@Composable
fun MissionCard(mission: MissionResponse, isWeekly: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.completed) GreenLight else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (mission.completed) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de estado
            Icon(
                if (mission.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                null,
                tint = if (mission.completed) GreenPrimary else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contenido
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(mission.title, fontWeight = FontWeight.Bold)
                    if (isWeekly) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BlueInfo.copy(alpha = 0.1f))
                        ) {
                            Text(
                                "Semanal",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = BlueInfo
                            )
                        }
                    }
                }
                Text(
                    mission.description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.Gray
                )
                
                // Barra de progreso si no está completada
                if (!mission.completed && mission.target > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { mission.progress.toFloat() / mission.target },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = GreenPrimary,
                            trackColor = Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${mission.progress}/${mission.target}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // XP reward
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (mission.completed) GreenPrimary.copy(alpha = 0.1f) 
                                    else YellowWarning.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    "+${mission.xp} XP",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (mission.completed) GreenPrimary else YellowWarning,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
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
            containerColor = if (unlocked) GreenLight else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(if (unlocked) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del logro
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
            
            // Info del logro
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
                
                // Progreso si está bloqueado
                if (!unlocked && achievement.progressMax > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { achievement.progress.toFloat() / achievement.progressMax },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = GreenPrimary,
                            trackColor = Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${achievement.progress}/${achievement.progressMax}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Puntos
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "+${achievement.points}",
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) GreenPrimary else Color.Gray
                )
                Text(
                    "pts",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
