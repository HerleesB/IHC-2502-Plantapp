package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jardin.inteligente.model.Badge
import com.jardin.inteligente.model.Mission
import com.jardin.inteligente.model.Achievement
import com.jardin.inteligente.ui.theme.*

@Composable
fun GamificationScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Misiones", "Insignias", "Historial")

    val userPoints = 1250
    val currentLevel = 5
    val streak = 14
    val maxPoints = (currentLevel + 1) * 500

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card with level and points
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Purple600
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nivel $currentLevel",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$userPoints / $maxPoints XP",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Yellow400,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = (userPoints % 500) / 500f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Orange500,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "$streak días de racha",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Yellow400,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "4 insignias",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Green600
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Content based on selected tab
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        MissionsContent()
                    }
                }
                1 -> {
                    item {
                        BadgesContent()
                    }
                }
                2 -> {
                    item {
                        HistoryContent()
                    }
                }
            }
        }
    }
}

@Composable
fun MissionsContent() {
    val missions = listOf(
        Mission(1, "Completa 3 diagnósticos", "Escanea y diagnostica 3 plantas diferentes", 2, 3, 100, false),
        Mission(2, "Mantén tu racha", "Cuida tus plantas por 7 días consecutivos", 5, 7, 150, false),
        Mission(3, "Ayuda a la comunidad", "Responde 2 preguntas de otros usuarios", 1, 2, 80, false)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = Green600
                    )
                    Text(
                        text = "Misiones Semanales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Completa misiones para ganar puntos y desbloquear insignias",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        missions.forEach { mission ->
            MissionCard(mission)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Green50
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Green600
                )
                Column {
                    Text(
                        text = "¡Excelente progreso!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Green800
                    )
                    Text(
                        text = "Estás a solo 1 diagnóstico de completar tu primera misión semanal. ¡Sigue así y ganarás 100 XP!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green700
                    )
                }
            }
        }
    }
}

@Composable
fun MissionCard(mission: Mission) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = mission.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                AssistChip(
                    onClick = { },
                    label = { Text("+${mission.points} XP") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Green50,
                        labelColor = Green700
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progreso: ${mission.progress}/${mission.total}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${(mission.progress * 100 / mission.total)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green600,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = mission.progress.toFloat() / mission.total,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.Gray.copy(alpha = 0.1f), CircleShape),
                    color = Green600
                )
            }

            if (mission.progress >= mission.total) {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reclamar recompensa")
                }
            } else {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar misión")
                }
            }
        }
    }
}

@Composable
fun BadgesContent() {
    val badges = listOf(
        Badge(1, "Primer Diagnóstico", "Completaste tu primer diagnóstico", true, "2025-10-01", "blue"),
        Badge(2, "Racha de Fuego", "7 días consecutivos cuidando tus plantas", true, "2025-10-08", "orange"),
        Badge(3, "Experto Compartidor", "Compartiste 5 casos en la comunidad", true, "2025-10-15", "purple"),
        Badge(4, "Maestro Jardinero", "Todas tus plantas están saludables", true, "2025-10-20", "yellow"),
        Badge(5, "Racha Legendaria", "30 días consecutivos", false, null, "gray", 14, 30),
        Badge(6, "Sanador de Plantas", "Recupera 10 plantas enfermas", false, null, "gray", 6, 10)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Yellow400
                    )
                    Text(
                        text = "Colección de Insignias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${badges.count { it.earned }} de ${badges.size} desbloqueadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        badges.chunked(2).forEach { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowBadges.forEach { badge ->
                    BadgeCard(
                        badge = badge,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowBadges.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun BadgeCard(badge: Badge, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.earned) Color.White else Color.Gray.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = when (badge.color) {
                    "blue" -> Blue600
                    "orange" -> Orange500
                    "purple" -> Purple600
                    "yellow" -> Yellow400
                    else -> Color.Gray
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = when (badge.name) {
                        "Primer Diagnóstico" -> Icons.Default.CameraAlt
                        "Racha de Fuego" -> Icons.Default.LocalFireDepartment
                        "Experto Compartidor" -> Icons.Default.Groups
                        "Maestro Jardinero" -> Icons.Default.EmojiEvents
                        "Racha Legendaria" -> Icons.Default.Star
                        else -> Icons.Default.Favorite
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Text(
                text = badge.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2
            )

            if (badge.earned && badge.date != null) {
                AssistChip(
                    onClick = { },
                    label = { Text(badge.date, style = MaterialTheme.typography.labelSmall) }
                )
            } else if (!badge.earned && badge.progress != null && badge.total != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = badge.progress.toFloat() / badge.total,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = Green600
                    )
                    Text(
                        text = "${badge.progress}/${badge.total}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryContent() {
    val achievements = listOf(
        Achievement("2025-10-22", "Completaste el plan semanal", 50),
        Achievement("2025-10-21", "Tu Monstera mejoró un 20%", 30),
        Achievement("2025-10-20", "Ganaste: Maestro Jardinero", 100),
        Achievement("2025-10-19", "Racha de 14 días", 75)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Blue600
                    )
                    Text(
                        text = "Historial de Logros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tus logros recientes y puntos ganados",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        achievements.forEach { achievement ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Gray50
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Green50,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Green600,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Column {
                            Text(
                                text = achievement.action,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = achievement.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    AssistChip(
                        onClick = { },
                        label = { Text("+${achievement.points} XP") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Yellow400.copy(alpha = 0.2f),
                            labelColor = Color(0xFF92400E)
                        )
                    )
                }
            }
        }
    }
}
