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

/**
 * CU-02, CU-03, CU-12: Pantalla de detalle de diagnóstico con recomendaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisDetailScreen(
    diagnosis: DiagnosisResponse,
    isGuestMode: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onAddToGarden: () -> Unit = {},
    onShareToCommunity: (Int) -> Unit = {}
) {
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico #${diagnosis.diagnosisId}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    if (!isGuestMode) {
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(Icons.Default.Share, "Compartir", tint = Color.White)
                        }
                    }
                }
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
                    containerColor = when (diagnosis.severity.lowercase()) {
                        "high", "alta", "critical" -> RedError.copy(alpha = 0.1f)
                        "medium", "media", "warning" -> YellowWarning.copy(alpha = 0.1f)
                        else -> GreenPrimary.copy(alpha = 0.1f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (diagnosis.severity.lowercase()) {
                                "high", "alta", "critical" -> Icons.Default.Error
                                "medium", "media", "warning" -> Icons.Default.Warning
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = when (diagnosis.severity.lowercase()) {
                                "high", "alta", "critical" -> RedError
                                "medium", "media", "warning" -> YellowWarning
                                else -> GreenPrimary
                            },
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = diagnosis.diseaseName ?: "Análisis Completado",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Confianza: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${(diagnosis.confidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Severity badge
                    val severityColor = when (diagnosis.severity.lowercase()) {
                        "high", "alta", "critical" -> RedError
                        "medium", "media", "warning" -> YellowWarning
                        else -> GreenPrimary
                    }
                    val severityText = when (diagnosis.severity.lowercase()) {
                        "high", "alta", "critical" -> "Severidad Alta"
                        "medium", "media", "warning" -> "Severidad Media"
                        else -> "Severidad Baja"
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = severityColor.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocalHospital,
                                null,
                                tint = severityColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = severityText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = severityColor
                            )
                        }
                    }
                }
            }
            
            // Diagnosis Text
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, null, tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Diagnóstico",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = diagnosis.diagnosisText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Recommendations (CU-03)
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
                        diagnosis.recommendations.forEachIndexed { index, recommendation ->
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = GreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = recommendation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Weekly Plan (CU-03)
            if (diagnosis.weeklyPlan.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = BlueInfo)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Plan de Cuidado Semanal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        diagnosis.weeklyPlan.forEach { task ->
                            WeeklyTaskItem(task)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
            
            // Feedback section (CU-12)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Feedback, null, tint = BlueInfo)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¿Es correcto este diagnóstico?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tu feedback nos ayuda a mejorar la precisión del diagnóstico.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showFeedbackDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ThumbDown, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Incorrecto")
                        }
                        Button(
                            onClick = { /* TODO: Send positive feedback */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Correcto")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            if (!isGuestMode) {
                Button(
                    onClick = onAddToGarden,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Planta a Mi Jardín")
                }
                
                OutlinedButton(
                    onClick = { showShareDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir en Comunidad")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = YellowWarning.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = YellowWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Inicia sesión para guardar esta planta o compartir en la comunidad",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    
    // Feedback dialog (CU-12)
    if (showFeedbackDialog) {
        var feedbackText by remember { mutableStateOf("") }
        var correctDiagnosis by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            icon = {
                Icon(Icons.Default.Feedback, null, tint = BlueInfo)
            },
            title = { Text("Corregir Diagnóstico") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "¿Cuál crees que es el diagnóstico correcto?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = correctDiagnosis,
                        onValueChange = { correctDiagnosis = it },
                        label = { Text("Diagnóstico correcto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Comentarios adicionales (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Send feedback
                        showFeedbackDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Enviar Feedback")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Share dialog
    if (showShareDialog) {
        var isAnonymous by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            icon = {
                Icon(Icons.Default.Share, null, tint = GreenPrimary)
            },
            title = { Text("Compartir en Comunidad") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "¿Deseas compartir este diagnóstico para que otros usuarios puedan ayudarte o aprender de tu caso?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isAnonymous) Icons.Default.PersonOff else Icons.Default.Person,
                                null,
                                tint = if (isAnonymous) BlueInfo else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publicar anónimamente")
                        }
                        Switch(
                            checked = isAnonymous,
                            onCheckedChange = { isAnonymous = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onShareToCommunity(diagnosis.diagnosisId)
                        showShareDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Compartir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun WeeklyTaskItem(task: WeeklyTask) {
    val priorityColor = when (task.priority.lowercase()) {
        "high", "alta" -> RedError
        "medium", "media" -> YellowWarning
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
            colors = CardDefaults.cardColors(containerColor = priorityColor.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
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
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = task.task,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = priorityColor.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = when (task.priority.lowercase()) {
                    "high", "alta" -> "Alta"
                    "medium", "media" -> "Media"
                    else -> "Baja"
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = priorityColor
            )
        }
    }
}
