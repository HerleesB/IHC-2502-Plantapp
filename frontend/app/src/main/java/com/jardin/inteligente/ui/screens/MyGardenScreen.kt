package com.jardin.inteligente.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jardin.inteligente.model.PlantResponse
import com.jardin.inteligente.viewmodel.MyGardenViewModel
import com.jardin.inteligente.viewmodel.MyGardenViewModelFactory
import com.jardin.inteligente.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * CU-04, CU-16: Pantalla Mi Jardín - Gestión de plantas y visualización dinámica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGardenScreen(
    onNavigateToDiagnosis: (Int) -> Unit = {},
    onNavigateToPlantDetail: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: MyGardenViewModel = viewModel(
        factory = MyGardenViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showAddPlantDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var plantToDelete by remember { mutableStateOf<PlantResponse?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    
    // TTS para accesibilidad
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
                ttsReady = true
            }
        }
        onDispose {
            tts?.shutdown()
        }
    }
    
    fun speak(text: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Jardín") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadPlants() }) {
                        Icon(Icons.Default.Refresh, "Actualizar", tint = Color.White)
                    }
                    IconButton(onClick = { showAddPlantDialog = true }) {
                        Icon(Icons.Default.Add, "Agregar planta", tint = Color.White)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        Icon(
                            Icons.Default.Error, 
                            null, 
                            modifier = Modifier.size(48.dp),
                            tint = RedError
                        )
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
                        Icon(
                            Icons.Default.Yard, 
                            null, 
                            modifier = Modifier.size(80.dp), 
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tu jardín está vacío", 
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Agrega tu primera planta para comenzar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showAddPlantDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
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
                                onCardClick = { onNavigateToPlantDetail(plant.id) },
                                onWaterClick = { viewModel.waterPlant(plant.id) },
                                onDiagnosisClick = { onNavigateToDiagnosis(plant.id) },
                                onDeleteClick = {
                                    plantToDelete = plant
                                    showDeleteDialog = true
                                    // Reproducir alerta de voz
                                    speak("¿Estás seguro que deseas eliminar la planta ${plant.name}?")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo para agregar planta
    if (showAddPlantDialog) {
        AddPlantDialog(
            onDismiss = { showAddPlantDialog = false },
            onConfirm = { name, species, location ->
                viewModel.createPlant(name, species, location)
                showAddPlantDialog = false
            }
        )
    }
    
    // Diálogo de confirmación para eliminar planta
    if (showDeleteDialog && plantToDelete != null) {
        DeletePlantConfirmationDialog(
            plantName = plantToDelete!!.name,
            isDeleting = isDeleting,
            onConfirm = {
                scope.launch {
                    isDeleting = true
                    speak("Eliminando planta ${plantToDelete!!.name}")
                    
                    val success = viewModel.deletePlant(plantToDelete!!.id)
                    
                    isDeleting = false
                    showDeleteDialog = false
                    
                    if (success) {
                        speak("Planta eliminada correctamente")
                        snackbarHostState.showSnackbar(
                            message = "Planta \"${plantToDelete!!.name}\" eliminada correctamente",
                            duration = SnackbarDuration.Short
                        )
                    } else {
                        speak("Error al eliminar la planta")
                        snackbarHostState.showSnackbar(
                            message = "Error al eliminar la planta",
                            duration = SnackbarDuration.Short
                        )
                    }
                    plantToDelete = null
                }
            },
            onDismiss = {
                if (!isDeleting) {
                    showDeleteDialog = false
                    plantToDelete = null
                    speak("Cancelado")
                }
            }
        )
    }
}

/**
 * Diálogo de confirmación para eliminar planta con diseño robusto
 */
@Composable
fun DeletePlantConfirmationDialog(
    plantName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = RedError,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "¿Eliminar planta?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Estás a punto de eliminar:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                // Nombre de la planta destacado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = RedError.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "\"$plantName\"",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = RedError
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Advertencia
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = YellowWarning.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = YellowWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Esta acción eliminará permanentemente la planta y todo su historial de diagnósticos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                if (isDeleting) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = RedError
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedError
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminando...")
                } else {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sí, eliminar planta")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(2.dp, GreenPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GreenPrimary
                )
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun StatsCard(stats: com.jardin.inteligente.model.ProgressStatsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GreenLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Resumen del Jardín", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(Icons.Default.Grass, "Plantas", stats.totalPlants.toString())
                StatItem(Icons.Default.Favorite, "Saludables", stats.healthyPlants.toString())
                StatItem(Icons.Default.LocalFireDepartment, "Racha", "${stats.streakDays} días")
                StatItem(Icons.Default.Star, "Nivel", stats.level.toString())
            }
            
            // Progress bar to next level
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "XP: ${stats.xp}/${stats.nextLevelXp}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        "${((stats.xp.toFloat() / stats.nextLevelXp) * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { stats.xp.toFloat() / stats.nextLevelXp },
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
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold
        )
        Text(
            label, 
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun PlantCard(
    plant: PlantResponse,
    onCardClick: () -> Unit,
    onWaterClick: () -> Unit,
    onDiagnosisClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Image with delete button overlay
            Box {
                if (plant.imageUrl != null) {
                    AsyncImage(
                        model = plant.imageUrl,
                        contentDescription = plant.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = GreenLight,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.LocalFlorist,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = GreenPrimary
                                )
                            }
                        }
                    }
                }
                
                // Botón de eliminar en la esquina superior derecha
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar planta",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            plant.name, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        )
                        plant.species?.let {
                            Text(
                                it, 
                                style = MaterialTheme.typography.bodyMedium, 
                                color = Color.Gray
                            )
                        }
                    }
                    
                    // Health badge
                    val (bgColor, textColor, text) = when {
                        plant.healthScore >= 70 -> Triple(GreenLight, GreenPrimary, "Saludable")
                        plant.healthScore >= 40 -> Triple(YellowWarning.copy(alpha = 0.2f), YellowWarning, "Atención")
                        else -> Triple(RedError.copy(alpha = 0.2f), RedError, "Crítico")
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = bgColor)
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Health indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (color, icon) = when {
                        plant.healthScore >= 70 -> Pair(GreenPrimary, Icons.Default.CheckCircle)
                        plant.healthScore >= 40 -> Pair(YellowWarning, Icons.Default.Warning)
                        else -> Pair(RedError, Icons.Default.Error)
                    }
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Progress bar
                    LinearProgressIndicator(
                        progress = { plant.healthScore / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = color,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${plant.healthScore}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Last watered
                plant.lastWatered?.let { lastWatered ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.WaterDrop,
                            null,
                            tint = BlueInfo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Último riego: ${lastWatered.take(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onWaterClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.WaterDrop, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regar")
                    }
                    Button(
                        onClick = onDiagnosisClick, 
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
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
    onConfirm: (String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.LocalFlorist,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Agregar Nueva Planta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    placeholder = { Text("Ej: Mi pothos favorito") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Especie (opcional)") },
                    placeholder = { Text("Ej: Pothos, Monstera...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación (opcional)") },
                    placeholder = { Text("Ej: Sala de estar") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(
                        name, 
                        species.ifBlank { null },
                        location.ifBlank { null }
                    ) 
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
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
