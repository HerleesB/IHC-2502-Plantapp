package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jardin.inteligente.model.DiagnosisHistoryItem
import com.jardin.inteligente.model.PlantResponse
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.PlantDetailViewModel
import com.jardin.inteligente.viewmodel.PlantDetailViewModelFactory

/**
 * CU-08: Pantalla de detalle de planta con historial y progreso
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: Int,
    onNavigateBack: () -> Unit = {},
    onNavigateToDiagnosis: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: PlantDetailViewModel = viewModel(
        factory = PlantDetailViewModelFactory(context, plantId)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plant?.name ?: "Detalle de Planta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, "Actualizar", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = RedError
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.error ?: "Error desconocido")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshData() }) {
                            Text("Reintentar")
                        }
                    }
                }
                uiState.plant != null -> {
                    PlantDetailContent(
                        plant = uiState.plant!!,
                        diagnoses = uiState.diagnoses,
                        progressSummary = uiState.progressSummary,
                        onWaterPlant = { viewModel.waterPlant() },
                        onDiagnose = onNavigateToDiagnosis
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantDetailContent(
    plant: PlantResponse,
    diagnoses: List<DiagnosisHistoryItem>,
    progressSummary: String?,
    onWaterPlant: () -> Unit,
    onDiagnose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Imagen principal
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                if (plant.imageUrl != null) {
                    AsyncImage(
                        model = plant.imageUrl,
                        contentDescription = plant.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LocalFlorist,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = GreenPrimary
                            )
                            Text("Sin imagen", color = Color.Gray)
                        }
                    }
                }
            }
        }
        
        // Información básica
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = plant.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    plant.species?.let { species ->
                        Text(
                            text = species,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Estado de salud
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estado de Salud",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (color, text) = getHealthStatus(plant.healthScore)
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                        
                        // Barra de progreso circular
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { plant.healthScore / 100f },
                                modifier = Modifier.size(60.dp),
                                color = getHealthColor(plant.healthScore),
                                strokeWidth = 6.dp,
                                trackColor = Color.LightGray.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "${plant.healthScore}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Último riego
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = BlueInfo
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Último riego",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = plant.lastWatered?.take(10) ?: "Sin registrar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Grass,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Último fertilizado",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = plant.lastFertilized?.take(10) ?: "Sin registrar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Botones de acción
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onWaterPlant,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.WaterDrop, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regar")
                }
                
                Button(
                    onClick = onDiagnose,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Icon(Icons.Default.Search, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Diagnosticar")
                }
            }
        }
        
        // Resumen de progreso (generado por LLM)
        if (progressSummary != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GreenLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Resumen de Progreso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = progressSummary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Historial de diagnósticos
        item {
            Text(
                text = "Historial de Diagnósticos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (diagnoses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sin diagnósticos aún",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Text(
                            "Realiza tu primer diagnóstico",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(diagnoses) { diagnosis ->
                DiagnosisHistoryCard(diagnosis = diagnosis)
            }
        }
    }
}

@Composable
private fun DiagnosisHistoryCard(diagnosis: DiagnosisHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen thumbnail
            Card(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = diagnosis.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = diagnosis.diseaseName ?: "Diagnóstico",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Severidad badge
                val severityColor = when (diagnosis.severity.lowercase()) {
                    "high", "alta" -> RedError
                    "medium", "media" -> YellowWarning
                    else -> GreenPrimary
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = severityColor.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = diagnosis.severity,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor
                    )
                }
                
                Text(
                    text = diagnosis.createdAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            // Confianza
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(diagnosis.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
                Text(
                    text = "confianza",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun getHealthStatus(score: Int): Pair<Color, String> {
    return when {
        score >= 70 -> Pair(GreenPrimary, "Saludable")
        score >= 40 -> Pair(YellowWarning, "Atención")
        else -> Pair(RedError, "Crítico")
    }
}

private fun getHealthColor(score: Int): Color {
    return when {
        score >= 70 -> GreenPrimary
        score >= 40 -> YellowWarning
        else -> RedError
    }
}
