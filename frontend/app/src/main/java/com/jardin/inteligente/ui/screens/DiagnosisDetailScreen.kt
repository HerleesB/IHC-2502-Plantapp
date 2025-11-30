package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import com.jardin.inteligente.model.DiagnosisResponse
import com.jardin.inteligente.model.WeeklyTask
import com.jardin.inteligente.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisDetailScreen(
    diagnosis: DiagnosisResponse,
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Disease/Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (diagnosis.severity) {
                        "critical" -> RedError.copy(alpha = 0.1f)
                        "warning" -> YellowWarning.copy(alpha = 0.1f)
                        else -> GreenPrimary.copy(alpha = 0.1f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (diagnosis.severity) {
                                "critical" -> Icons.Default.Error
                                "warning" -> Icons.Default.Warning
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = when (diagnosis.severity) {
                                "critical" -> RedError
                                "warning" -> YellowWarning
                                else -> GreenPrimary
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = diagnosis.diseaseName ?: "Diagnóstico Completo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(diagnosis.confidence * 100).toInt()}% de confianza",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Diagnosis Text
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Diagnóstico",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = diagnosis.diagnosisText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Recommendations
            if (diagnosis.recommendations.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, null, tint = YellowWarning)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recomendaciones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        diagnosis.recommendations.forEach { recommendation ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = recommendation,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // Weekly Plan
            if (diagnosis.weeklyPlan.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = GreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Plan Semanal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        diagnosis.weeklyPlan.forEach { task ->
                            WeeklyTaskItem(task)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTaskItem(task: WeeklyTask) {
    val priorityColor = when (task.priority) {
        "high" -> RedError
        "medium" -> YellowWarning
        else -> GreenPrimary
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(48.dp),
            colors = CardDefaults.cardColors(containerColor = priorityColor.copy(alpha = 0.1f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.EventNote,
                    null,
                    tint = priorityColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.day,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = task.task,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = priorityColor.copy(alpha = 0.1f))
        ) {
            Text(
                text = when (task.priority) {
                    "high" -> "Alta"
                    "medium" -> "Media"
                    else -> "Baja"
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = priorityColor
            )
        }
    }
}
