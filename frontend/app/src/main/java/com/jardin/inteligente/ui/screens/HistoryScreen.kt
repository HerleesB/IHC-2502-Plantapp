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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jardin.inteligente.model.DiagnosisHistoryItem
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.DiagnosisHistoryViewModel
import com.jardin.inteligente.viewmodel.DiagnosisHistoryViewModelFactory

/**
 * CU-08: Pantalla de historial de diagnósticos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit = {},
    onDiagnosisClick: (DiagnosisHistoryItem) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: DiagnosisHistoryViewModel = viewModel(
        factory = DiagnosisHistoryViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Diagnósticos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadHistory() }) {
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
                        Button(onClick = { viewModel.loadHistory() }) {
                            Text("Reintentar")
                        }
                    }
                }
                uiState.diagnoses.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.History, 
                            null, 
                            modifier = Modifier.size(80.dp), 
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Sin historial de diagnósticos",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Realiza tu primer diagnóstico para verlo aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Stats header
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = GreenLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${uiState.diagnoses.size}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary
                                        )
                                        Text(
                                            "Total",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    
                                    VerticalDivider(modifier = Modifier.height(40.dp))
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        val highSeverity = uiState.diagnoses.count { 
                                            it.severity.lowercase() in listOf("high", "alta", "critical") 
                                        }
                                        Text(
                                            "$highSeverity",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (highSeverity > 0) RedError else GreenPrimary
                                        )
                                        Text(
                                            "Críticos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    
                                    VerticalDivider(modifier = Modifier.height(40.dp))
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        val avgConfidence = if (uiState.diagnoses.isNotEmpty()) {
                                            (uiState.diagnoses.map { it.confidence }.average() * 100).toInt()
                                        } else 0
                                        Text(
                                            "$avgConfidence%",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary
                                        )
                                        Text(
                                            "Confianza",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                        
                        items(uiState.diagnoses) { diagnosis ->
                            DiagnosisHistoryCard(
                                diagnosis = diagnosis,
                                onClick = { onDiagnosisClick(diagnosis) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisHistoryCard(
    diagnosis: DiagnosisHistoryItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Image
            Card(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = diagnosis.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = diagnosis.plantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Severity badge
                    val severityColor = when (diagnosis.severity.lowercase()) {
                        "high", "alta", "critical" -> RedError
                        "medium", "media", "warning" -> YellowWarning
                        else -> GreenPrimary
                    }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = severityColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = diagnosis.severity,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = severityColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                diagnosis.diseaseName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when (diagnosis.severity.lowercase()) {
                            "high", "alta", "critical" -> RedError
                            "medium", "media", "warning" -> YellowWarning
                            else -> GreenPrimary
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = diagnosis.diagnosisText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(diagnosis.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = diagnosis.createdAt.take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.ChevronRight, 
                null, 
                tint = Color.Gray,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}
