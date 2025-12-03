package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.jardin.inteligente.model.DiagnosisResponse
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.AddPlantViewModel
import com.jardin.inteligente.viewmodel.AddPlantViewModelFactory
import com.jardin.inteligente.viewmodel.AddPlantState

/**
 * CU-20: Pantalla para agregar planta desde diagnóstico completado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantFromDiagnosisScreen(
    diagnosis: DiagnosisResponse?,
    onPlantAdded: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AddPlantViewModel = viewModel(
        factory = AddPlantViewModelFactory(context)
    )
    val addPlantState by viewModel.addPlantState.collectAsState()
    
    var plantName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Pre-llenar especie si el diagnóstico la detectó
    LaunchedEffect(diagnosis) {
        diagnosis?.diseaseName?.let {
            // Si hay nombre de enfermedad, podríamos inferir la especie
        }
    }
    
    // Handle state
    LaunchedEffect(addPlantState) {
        when (addPlantState) {
            is AddPlantState.Success -> {
                showSuccessDialog = true
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar a Mi Jardín") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
            // Resumen del diagnóstico
            if (diagnosis != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GreenLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Diagnóstico Completado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        diagnosis.diseaseName?.let { disease ->
                            Row {
                                Text(
                                    "Problema detectado: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    disease,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Row {
                            Text(
                                "Confianza: ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                "${(diagnosis.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = GreenPrimary
                            )
                        }
                        
                        Row {
                            Text(
                                "Severidad: ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            val severityColor = when (diagnosis.severity.lowercase()) {
                                "high", "alta" -> RedError
                                "medium", "media" -> YellowWarning
                                else -> GreenPrimary
                            }
                            Text(
                                diagnosis.severity,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = severityColor
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider()
            
            Text(
                text = "Información de la Planta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Nombre de la planta
            OutlinedTextField(
                value = plantName,
                onValueChange = { plantName = it },
                label = { Text("Nombre de la planta *") },
                placeholder = { Text("Ej: Mi pothos, Ficus del salón...") },
                leadingIcon = { Icon(Icons.Default.LocalFlorist, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Especie
            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Especie (opcional)") },
                placeholder = { Text("Ej: Pothos, Monstera deliciosa...") },
                leadingIcon = { Icon(Icons.Default.Grass, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Ubicación
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación (opcional)") },
                placeholder = { Text("Ej: Sala de estar, Balcón...") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Ubicaciones sugeridas
            Text(
                "Ubicaciones comunes:",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Interior", "Balcón", "Jardín", "Terraza").forEach { loc ->
                    FilterChip(
                        selected = location == loc,
                        onClick = { location = loc },
                        label = { Text(loc) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Error message
            if (addPlantState is AddPlantState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = RedError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            (addPlantState as AddPlantState.Error).message,
                            color = RedError
                        )
                    }
                }
            }
            
            // Botón guardar
            Button(
                onClick = {
                    viewModel.addPlant(
                        name = plantName,
                        species = species.ifBlank { null },
                        location = location.ifBlank { null },
                        diagnosisId = diagnosis?.diagnosisId
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = plantName.isNotBlank() && addPlantState !is AddPlantState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                if (addPlantState is AddPlantState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar en Mi Jardín")
                }
            }
            
            // Info
            Card(
                colors = CardDefaults.cardColors(containerColor = BlueInfo.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = BlueInfo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Al guardar, la planta se agregará a tu jardín con el diagnóstico inicial. Podrás hacer seguimiento de su progreso y recibir recordatorios personalizados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
    
    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onPlantAdded()
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("¡Planta Agregada!") },
            text = { 
                Text("\"$plantName\" ha sido agregada a tu jardín. Ahora podrás hacer seguimiento de su salud y recibir recordatorios.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPlantAdded()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Ver Mi Jardín")
                }
            }
        )
    }
}
