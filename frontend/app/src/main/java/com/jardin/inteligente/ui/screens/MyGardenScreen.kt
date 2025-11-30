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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jardin.inteligente.viewmodel.MyGardenViewModel
import com.jardin.inteligente.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGardenScreen(
    viewModel: MyGardenViewModel = viewModel(),
    onNavigateToDiagnosis: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddPlantDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Jardín") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showAddPlantDialog = true }) {
                        Icon(Icons.Default.Add, "Agregar planta", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading && uiState.plants.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Error, null, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.error ?: "Error desconocido")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPlants() }) {
                            Text("Reintentar")
                        }
                    }
                }
                uiState.plants.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Yard, null, modifier = Modifier.size(64.dp), tint = GreenPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No tienes plantas aún", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Agrega tu primera planta")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAddPlantDialog = true }) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar Planta")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Stats card
                        item {
                            uiState.stats?.let { stats ->
                                StatsCard(stats)
                            }
                        }
                        
                        // Plants
                        items(uiState.plants) { plant ->
                            PlantCard(
                                plant = plant,
                                onWaterClick = { viewModel.waterPlant(plant.id) },
                                onDiagnosisClick = { onNavigateToDiagnosis(plant.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddPlantDialog) {
        AddPlantDialog(
            onDismiss = { showAddPlantDialog = false },
            onConfirm = { name, species ->
                viewModel.createPlant(name, species)
                showAddPlantDialog = false
            }
        )
    }
}

@Composable
fun StatsCard(stats: com.jardin.inteligente.model.ProgressStatsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GreenLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(Icons.Default.Grass, "Plantas", stats.totalPlants.toString())
                StatItem(Icons.Default.Favorite, "Saludables", stats.healthyPlants.toString())
                StatItem(Icons.Default.LocalFireDepartment, "Racha", "${stats.streakDays} días")
                StatItem(Icons.Default.Star, "Nivel", stats.level.toString())
            }
        }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = GreenPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PlantCard(
    plant: com.jardin.inteligente.model.PlantResponse,
    onWaterClick: () -> Unit,
    onDiagnosisClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Image
            if (plant.imageUrl != null) {
                AsyncImage(
                    model = plant.imageUrl,
                    contentDescription = plant.name,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            }
            
            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                Text(plant.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                plant.species?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Health indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (color, icon, text) = when {
                        plant.healthScore >= 70 -> Triple(GreenPrimary, Icons.Default.CheckCircle, "Saludable")
                        plant.healthScore >= 40 -> Triple(YellowWarning, Icons.Default.Warning, "Necesita atención")
                        else -> Triple(RedError, Icons.Default.Error, "Crítico")
                    }
                    Icon(icon, null, tint = color)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text, color = color, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${plant.healthScore}%", style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = onWaterClick) {
                        Icon(Icons.Default.WaterDrop, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regar")
                    }
                    Button(onClick = onDiagnosisClick, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                        Icon(Icons.Default.Search, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Diagnosticar")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPlantDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Planta") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Especie (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, species.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
