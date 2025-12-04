package com.jardin.inteligente.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jardin.inteligente.model.DiagnosisResponse
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.CaptureViewModel
import com.jardin.inteligente.viewmodel.CaptureViewModelFactory
import com.jardin.inteligente.viewmodel.ValidationState
import com.jardin.inteligente.viewmodel.DiagnosisState
import java.io.File
import java.util.Locale

/**
 * CU-01, CU-02, CU-14, CU-20: Pantalla de Captura Accesible con flujo completo
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AccessibleCaptureScreen(
    plantId: Int = 0,
    isGuestMode: Boolean = false,
    onDiagnosisComplete: (DiagnosisResponse) -> Unit = {},
    onNavigateToAddPlant: (DiagnosisResponse) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: CaptureViewModel = viewModel(factory = CaptureViewModelFactory(context))
    
    val validationState by viewModel.validationState.collectAsState()
    val diagnosisState by viewModel.diagnosisState.collectAsState()
    val capturedImageUri by viewModel.capturedImageUri.collectAsState()
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDiagnosisResult by remember { mutableStateOf(false) }
    var currentDiagnosis by remember { mutableStateOf<DiagnosisResponse?>(null) }
    
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    
    // TTS para accesibilidad (CU-14)
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
    
    fun vibrateSuccess() {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }
    
    fun vibrateError() {
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
    }
    
    val permissionsState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.CAMERA)
    )
    
    val tempImageFile = remember {
        File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg").apply {
            createNewFile()
        }
    }
    
    val tempImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempImageFile
        )
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempImageUri
            viewModel.setCapturedImage(tempImageUri)
            vibrateSuccess()
            speak("Foto capturada correctamente")
        } else {
            vibrateError()
            speak("No se pudo capturar la foto")
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            viewModel.setCapturedImage(it)
            vibrateSuccess()
            speak("Imagen seleccionada")
        }
    }
    
    // Handle diagnosis completion
    LaunchedEffect(diagnosisState) {
        if (diagnosisState is DiagnosisState.Success) {
            val response = (diagnosisState as DiagnosisState.Success).response
            currentDiagnosis = response
            showDiagnosisResult = true
            vibrateSuccess()
            speak("Diagnóstico completado. ${response.diseaseName ?: "Análisis"} detectado con ${(response.confidence * 100).toInt()} por ciento de confianza.")
        }
    }
    
    // Handle validation state for accessibility
    LaunchedEffect(validationState) {
        when (validationState) {
            is ValidationState.Success -> {
                val response = (validationState as ValidationState.Success).response
                if (response.success) {
                    vibrateSuccess()
                    speak(response.message)
                } else {
                    vibrateError()
                    speak(response.guidance)
                }
            }
            is ValidationState.Error -> {
                vibrateError()
                speak("Error al validar la foto")
            }
            else -> {}
        }
    }
    
    if (showDiagnosisResult && currentDiagnosis != null) {
        // Mostrar pantalla de resultado
        DiagnosisResultContent(
            diagnosis = currentDiagnosis!!,
            isGuestMode = isGuestMode,
            onNavigateBack = { 
                showDiagnosisResult = false
                imageUri = null
                viewModel.resetStates()
            },
            onAddToGarden = {
                onNavigateToAddPlant(currentDiagnosis!!)
            },
            onViewDetails = {
                onDiagnosisComplete(currentDiagnosis!!)
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Captura y Diagnóstico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (plantId > 0) "Diagnosticando planta #$plantId" else "Diagnóstico rápido",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            // Image preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Foto capturada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = GreenLight,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = GreenPrimary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Toma una foto de tu planta",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "o selecciona de la galería",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            
            // Tips para mejor foto (CU-01)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlueInfo.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = BlueInfo
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Consejos para mejor diagnóstico",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Buena iluminación natural\n• Enfoca las hojas afectadas\n• Evita sombras y reflejos\n• Incluye varias hojas si es posible",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de cámara grande para accesibilidad (CU-14)
                Button(
                    onClick = {
                        if (permissionsState.allPermissionsGranted) {
                            speak("Abriendo cámara")
                            cameraLauncher.launch(tempImageUri)
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cámara")
                }
                
                OutlinedButton(
                    onClick = { 
                        speak("Abriendo galería")
                        galleryLauncher.launch("image/*") 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galería")
                }
            }
            
            // Validation section
            if (imageUri != null) {
                when (validationState) {
                    is ValidationState.Idle -> {
                        Button(
                            onClick = { 
                                speak("Validando calidad de la foto")
                                imageUri?.let { viewModel.validatePhoto(it) } 
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueInfo)
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Validar Foto")
                        }
                    }
                    is ValidationState.Loading -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Text("Validando foto con IA...")
                            }
                        }
                    }
                    is ValidationState.Success -> {
                        val response = (validationState as ValidationState.Success).response
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (response.success) GreenLight else RedError.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (response.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (response.success) GreenPrimary else RedError,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = response.message,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = response.guidance,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                if (response.success) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Botón Continuar -> para diagnóstico (CU-20)
                                    Button(
                                        onClick = { 
                                            speak("Iniciando diagnóstico con inteligencia artificial")
                                            imageUri?.let { 
                                                viewModel.analyzePlant(it, if (plantId > 0) plantId else 0) 
                                            } 
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                                    ) {
                                        Text("Continuar", style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.ArrowForward, null)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = {
                                            imageUri = null
                                            viewModel.resetStates()
                                            speak("Toma otra foto")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Refresh, null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Tomar otra foto")
                                    }
                                }
                            }
                        }
                    }
                    is ValidationState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, null, tint = RedError)
                                Column {
                                    Text("Error de validación", fontWeight = FontWeight.Bold)
                                    Text((validationState as ValidationState.Error).message)
                                }
                            }
                        }
                    }
                }
            }
            
            // Diagnosis result
            when (diagnosisState) {
                is DiagnosisState.Loading -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = GreenPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Analizando planta con IA...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Esto puede tomar 10-30 segundos",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                is DiagnosisState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = RedError)
                            Column {
                                Text(
                                    "Error en diagnóstico",
                                    fontWeight = FontWeight.Bold
                                )
                                Text((diagnosisState as DiagnosisState.Error).message)
                            }
                        }
                    }
                }
                else -> {}
            }
            
            // Info para modo invitado
            if (isGuestMode && imageUri == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = YellowWarning.copy(alpha = 0.1f))
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
                            "Como invitado puedes diagnosticar plantas, pero no guardarlas en tu jardín. Crea una cuenta para guardar el historial.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/**
 * Contenido del resultado del diagnóstico
 */
@Composable
private fun DiagnosisResultContent(
    diagnosis: DiagnosisResponse,
    isGuestMode: Boolean,
    onNavigateBack: () -> Unit,
    onAddToGarden: () -> Unit,
    onViewDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con resultado
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (diagnosis.severity.lowercase()) {
                    "high", "alta" -> RedError.copy(alpha = 0.1f)
                    "medium", "media" -> YellowWarning.copy(alpha = 0.1f)
                    else -> GreenLight
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Diagnóstico Completado",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                diagnosis.diseaseName?.let { disease ->
                    Text(
                        text = disease,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Confianza y severidad
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${(diagnosis.confidence * 100).toInt()}%",
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
                    
                    VerticalDivider(modifier = Modifier.height(50.dp))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val severityColor = when (diagnosis.severity.lowercase()) {
                            "high", "alta" -> RedError
                            "medium", "media" -> YellowWarning
                            else -> GreenPrimary
                        }
                        Text(
                            diagnosis.severity,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                        Text(
                            "Severidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Explicación
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, null, tint = GreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Explicación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    diagnosis.diagnosisText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Recomendaciones
        if (diagnosis.recommendations.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, null, tint = YellowWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Recomendaciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    diagnosis.recommendations.forEachIndexed { index, rec ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                "${index + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                rec,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Botones de acción
        if (!isGuestMode) {
            Button(
                onClick = onAddToGarden,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar a Mi Jardín")
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
                        "Inicia sesión para guardar esta planta en tu jardín",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        OutlinedButton(
            onClick = onViewDetails,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Visibility, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Detalles Completos")
        }
        
        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nuevo Diagnóstico")
        }
    }
}
