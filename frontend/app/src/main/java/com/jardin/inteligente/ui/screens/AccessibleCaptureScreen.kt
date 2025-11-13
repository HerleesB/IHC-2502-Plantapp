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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.CaptureViewModel
import com.jardin.inteligente.viewmodel.CaptureViewModelFactory
import com.jardin.inteligente.viewmodel.ValidationState
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AccessibleCaptureScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // ViewModel
    val viewModel: CaptureViewModel = viewModel(
        factory = CaptureViewModelFactory(context)
    )
    
    // Estados del ViewModel
    val validationState by viewModel.validationState.collectAsState()
    val capturedImageUri by viewModel.capturedImageUri.collectAsState()
    
    // Estados locales de UI
    var accessibleMode by remember { mutableStateOf(false) }
    var voiceGuidance by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }

    // TTS y Vibrator
    val tts = remember {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.setLanguage(Locale("es", "ES"))
            }
        }
        textToSpeech
    }

    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Permisos
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA
        )
    )

    // URI temporal para captura de foto
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para tomar foto con cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // Foto capturada exitosamente
            speak(tts, voiceGuidance, "Foto capturada. Analizando con inteligencia artificial.")
            vibratePattern(vibrator, hapticFeedback, "success")
            
            // Guardar URI y validar
            viewModel.setCapturedImage(photoUri!!)
            viewModel.validatePhoto(photoUri!!)
        } else {
            speak(tts, voiceGuidance, "No se pudo capturar la foto. Intenta de nuevo.")
            vibratePattern(vibrator, hapticFeedback, "short")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
        }
    }

    // Función para crear URI temporal para foto
    fun createImageUri(): Uri {
        val directory = File(context.cacheDir, "images")
        directory.mkdirs()
        val file = File.createTempFile(
            "camera_photo_",
            ".jpg",
            directory
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    // Función para iniciar captura real
    fun startRealCapture() {
        if (permissionsState.permissions.all { it.status.isGranted }) {
            // Crear URI para la foto
            photoUri = createImageUri()
            
            speak(tts, voiceGuidance, "Abriendo cámara. Prepárate para capturar la planta.")
            vibratePattern(vibrator, hapticFeedback, "short")
            
            // Lanzar cámara
            takePictureLauncher.launch(photoUri)
        } else {
            // Solicitar permisos
            speak(tts, voiceGuidance, "Necesitamos permiso de cámara para continuar.")
            vibratePattern(vibrator, hapticFeedback, "long")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6366F1)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Accessible,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Captura con Validación IA",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Captura fotos de tu planta y recibe retroalimentación inteligente sobre la calidad",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEDE9FE)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Cámara real",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Análisis IA",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Guía por voz",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Accessibility settings
        item {
            AccessibilitySettings(
                accessibleMode = accessibleMode,
                voiceGuidance = voiceGuidance,
                hapticFeedback = hapticFeedback,
                onAccessibleModeChange = { 
                    accessibleMode = it
                    if (it) {
                        voiceGuidance = true
                        hapticFeedback = true
                        speak(tts, true, "Modo accesible activado")
                        vibratePattern(vibrator, true, "long")
                    }
                },
                onVoiceGuidanceChange = { voiceGuidance = it },
                onHapticFeedbackChange = { hapticFeedback = it }
            )
        }

        // Solicitud de permisos si es necesario
        if (!permissionsState.permissions.all { it.status.isGranted }) {
            item {
                PermissionCard(
                    onRequestPermission = {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                )
            }
        }

        // Área de captura y resultados
        item {
            RealCaptureArea(
                capturedImageUri = capturedImageUri,
                validationState = validationState,
                onStartCapture = { startRealCapture() },
                onRetake = {
                    viewModel.resetValidation()
                    startRealCapture()
                },
                onProceedToDiagnosis = {
                    // TODO: Navegar a pantalla de diagnóstico completo
                    speak(tts, voiceGuidance, "Procediendo al diagnóstico completo")
                },
                tts = tts,
                vibrator = vibrator,
                voiceGuidance = voiceGuidance,
                hapticFeedback = hapticFeedback
            )
        }

        // Guía de uso
        item {
            UsageGuide()
        }
    }
}

// Funciones auxiliares fuera del Composable
private fun speak(tts: TextToSpeech, enabled: Boolean, message: String) {
    if (enabled) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}

private fun vibratePattern(vibrator: Vibrator, enabled: Boolean, pattern: String) {
    if (!enabled) return
    when (pattern) {
        "short" -> vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        "long" -> vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        "success" -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100, 50, 200), -1))
    }
}

@Composable
fun PermissionCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF3C7)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B)
                )
                Text(
                    text = "Permiso de cámara requerido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Necesitamos acceso a tu cámara para tomar fotos de tus plantas y analizarlas.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conceder permiso")
            }
        }
    }
}

@Composable
fun RealCaptureArea(
    capturedImageUri: Uri?,
    validationState: ValidationState,
    onStartCapture: () -> Unit,
    onRetake: () -> Unit,
    onProceedToDiagnosis: () -> Unit,
    tts: TextToSpeech,
    vibrator: Vibrator,
    voiceGuidance: Boolean,
    hapticFeedback: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null
                )
                Text(
                    text = "Captura de imagen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Área de previsualización de imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        color = if (capturedImageUri != null) Color.Transparent else Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = if (capturedImageUri != null) 2.dp else 0.dp,
                        color = when (validationState) {
                            is ValidationState.Success -> if (validationState.response.success) Green600 else Orange500
                            is ValidationState.Error -> Color.Red
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (capturedImageUri != null) {
                    // Mostrar imagen capturada
                    Image(
                        painter = rememberAsyncImagePainter(capturedImageUri),
                        contentDescription = "Foto capturada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder antes de capturar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No hay foto capturada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Estado de validación
            when (validationState) {
                is ValidationState.Loading -> {
                    AIAnalysisCard(
                        isLoading = true,
                        message = "Analizando tu foto con inteligencia artificial...",
                        icon = Icons.Default.Psychology,
                        containerColor = Color(0xFFDEEBFF)
                    )
                }
                is ValidationState.Success -> {
                    val response = validationState.response
                    
                    // Reproducir feedback
                    LaunchedEffect(response) {
                        speak(tts, voiceGuidance, response.guidance)
                        vibratePattern(vibrator, hapticFeedback, if (response.success) "success" else "long")
                    }
                    
                    AIValidationResultCard(
                        response = response,
                        onRetake = onRetake,
                        onProceed = onProceedToDiagnosis
                    )
                }
                is ValidationState.Error -> {
                    AIAnalysisCard(
                        isLoading = false,
                        message = "❌ ${validationState.message}",
                        icon = Icons.Default.Error,
                        containerColor = Color(0xFFFEE2E2)
                    )
                    
                    Button(
                        onClick = onRetake,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reintentar")
                    }
                }
                ValidationState.Idle -> {
                    // Botón para iniciar captura
                    Button(
                        onClick = onStartCapture,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar foto")
                    }
                }
            }
        }
    }
}

@Composable
fun AIAnalysisCard(
    isLoading: Boolean,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AIValidationResultCard(
    response: com.jardin.inteligente.model.CaptureGuidanceResponse,
    onRetake: () -> Unit,
    onProceed: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (response.success) Green50 else Color(0xFFFEF3C7)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Resultado principal
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (response.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (response.success) Green600 else Color(0xFFF59E0B),
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (response.success) "✅ Foto aprobada" else "⚠️ Necesita ajustes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (response.success) Green800 else Color(0xFF92400E)
                    )
                    Text(
                        text = response.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (response.success) Green700 else Color(0xFFA16207)
                    )
                }
            }

            Divider()

            // Mensaje de la IA
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = if (response.success) Green600 else Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Análisis de la IA:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = response.guidance,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!response.success) {
                    Button(
                        onClick = onRetake,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tomar otra")
                    }
                } else {
                    OutlinedButton(
                        onClick = onRetake,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Nueva foto")
                    }
                    Button(
                        onClick = onProceed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Continuar")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilitySettings(
    accessibleMode: Boolean,
    voiceGuidance: Boolean,
    hapticFeedback: Boolean,
    onAccessibleModeChange: (Boolean) -> Unit,
    onVoiceGuidanceChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Configuración de accesibilidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SettingItem(
                title = "Modo accesible completo",
                description = "Activa todas las funciones de accesibilidad",
                checked = accessibleMode,
                onCheckedChange = onAccessibleModeChange
            )

            SettingItem(
                icon = Icons.Default.RecordVoiceOver,
                title = "Guía por voz",
                description = "Instrucciones habladas en cada paso",
                checked = voiceGuidance,
                onCheckedChange = onVoiceGuidanceChange
            )

            SettingItem(
                icon = Icons.Default.Vibration,
                title = "Retroalimentación háptica",
                description = "Vibraciones para confirmar acciones",
                checked = hapticFeedback,
                onCheckedChange = onHapticFeedbackChange
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Gray50
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun UsageGuide() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E8FF)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Purple600
                )
                Text(
                    text = "Cómo usar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            GuideItem(
                number = "1",
                title = "Prepara tu planta",
                description = "Colócala en un lugar con buena iluminación natural"
            )

            GuideItem(
                number = "2",
                title = "Toma la foto",
                description = "Presiona 'Tomar foto' y captura tu planta centrada"
            )

            GuideItem(
                number = "3",
                title = "Espera el análisis",
                description = "La IA analizará la calidad de tu foto en segundos"
            )

            GuideItem(
                number = "4",
                title = "Sigue las recomendaciones",
                description = "Si la IA sugiere ajustes, toma otra foto siguiendo sus indicaciones"
            )
        }
    }
}

@Composable
fun GuideItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = Purple600,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Purple700
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
