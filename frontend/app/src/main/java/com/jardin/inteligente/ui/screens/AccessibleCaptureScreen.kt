package com.jardin.inteligente.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
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
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jardin.inteligente.model.DiagnosisResponse
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.CaptureViewModel
import com.jardin.inteligente.viewmodel.CaptureViewModelFactory
import com.jardin.inteligente.viewmodel.ValidationState
import com.jardin.inteligente.viewmodel.DiagnosisState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AccessibleCaptureScreen(
    plantId: Int = 0,
    onDiagnosisComplete: (DiagnosisResponse) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: CaptureViewModel = viewModel(factory = CaptureViewModelFactory(context))
    
    val validationState by viewModel.validationState.collectAsState()
    val diagnosisState by viewModel.diagnosisState.collectAsState()
    val capturedImageUri by viewModel.capturedImageUri.collectAsState()
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    
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
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            viewModel.setCapturedImage(it)
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
    
    // Handle diagnosis completion
    LaunchedEffect(diagnosisState) {
        if (diagnosisState is DiagnosisState.Success) {
            val response = (diagnosisState as DiagnosisState.Success).response
            onDiagnosisComplete(response)
        }
    }
    
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
        
        // Image preview
        if (imageUri != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Foto capturada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GreenLight)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toma una foto o selecciona de galería")
                    }
                }
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (permissionsState.allPermissionsGranted) {
                        cameraLauncher.launch(tempImageUri)
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PhotoCamera, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cámara")
            }
            
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PhotoLibrary, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Galería")
            }
        }
        
        // Validation section
        if (imageUri != null && validationState is ValidationState.Idle) {
            Button(
                onClick = { imageUri?.let { viewModel.validatePhoto(it) } },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Icon(Icons.Default.CheckCircle, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validar con IA")
            }
        }
        
        // Validation result
        when (validationState) {
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
                        containerColor = if (response.success) GreenLight else Color(0xFFFEF2F2)
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
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        if (response.success && plantId > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { imageUri?.let { viewModel.analyzePlant(it, plantId) } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Icon(Icons.Default.Search, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Realizar Diagnóstico Completo")
                            }
                        }
                    }
                }
            }
            is ValidationState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = RedError)
                        Text((validationState as ValidationState.Error).message)
                    }
                }
            }
            else -> {}
        }
        
        // Diagnosis result
        when (diagnosisState) {
            is DiagnosisState.Loading -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Analizando planta con IA...",
                            style = MaterialTheme.typography.titleMedium
                        )
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
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
        
        // Info card
        if (plantId == 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlueInfo.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, null, tint = BlueInfo)
                    Text(
                        "Para realizar un diagnóstico completo, selecciona una planta desde 'Mi Jardín'",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
