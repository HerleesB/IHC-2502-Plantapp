package com.jardin.inteligente.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.CommunityShareViewModel
import com.jardin.inteligente.viewmodel.CommunityShareViewModelFactory
import com.jardin.inteligente.viewmodel.ShareState
import java.io.File

/**
 * CU-07, CU-18: Pantalla para compartir caso en la comunidad con imagen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityShareScreen(
    onPostSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: CommunityShareViewModel = viewModel(
        factory = CommunityShareViewModelFactory(context)
    )
    val shareState by viewModel.shareState.collectAsState()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }
    var plantName by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Launcher para cámara
    val tempImageFile = remember {
        File(context.cacheDir, "share_image_${System.currentTimeMillis()}.jpg").apply {
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
            selectedImageUri = tempImageUri
        }
    }
    
    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    // Handle share state
    LaunchedEffect(shareState) {
        when (shareState) {
            is ShareState.Success -> {
                showSuccessDialog = true
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compartir en Comunidad") },
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
            // Sección de imagen
            Text(
                text = "Foto de la planta *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedImageUri != null) Color.Transparent else GreenLight
                )
            ) {
                if (selectedImageUri != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Botón para cambiar imagen
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = Color.Black.copy(alpha = 0.5f)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Quitar imagen",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Agrega una foto de tu planta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Botones de captura
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { cameraLauncher.launch(tempImageUri) },
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
            
            HorizontalDivider()
            
            // Nombre de la planta
            OutlinedTextField(
                value = plantName,
                onValueChange = { plantName = it },
                label = { Text("Nombre de la planta") },
                placeholder = { Text("Ej: Mi monstera, Ficus del balcón...") },
                leadingIcon = { Icon(Icons.Default.LocalFlorist, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Síntomas
            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                label = { Text("Síntomas observados") },
                placeholder = { Text("Ej: Hojas amarillas, manchas marrones...") },
                leadingIcon = { Icon(Icons.Default.Healing, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción del caso *") },
                placeholder = { Text("Cuéntanos qué le pasa a tu planta y qué ayuda necesitas...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6
            )
            
            // Toggle anónimo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAnonymous) BlueInfo.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAnonymous = !isAnonymous }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isAnonymous) Icons.Default.PersonOff else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isAnonymous) BlueInfo else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Publicar anónimamente",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (isAnonymous) "Tu nombre no se mostrará" else "Tu nombre será visible",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BlueInfo,
                            checkedTrackColor = BlueInfo.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            
            // Error message
            if (shareState is ShareState.Error) {
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
                            (shareState as ShareState.Error).message,
                            color = RedError
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón publicar
            Button(
                onClick = {
                    selectedImageUri?.let { uri ->
                        viewModel.sharePost(
                            imageUri = uri,
                            description = description,
                            plantName = plantName.ifBlank { null },
                            symptoms = symptoms.ifBlank { null },
                            isAnonymous = isAnonymous
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedImageUri != null && 
                         description.isNotBlank() && 
                         shareState !is ShareState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                if (shareState is ShareState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publicar en Comunidad")
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
                        "Al publicar, otros usuarios podrán ver tu caso y ayudarte con consejos y soluciones.",
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
                onPostSuccess()
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("¡Publicado!") },
            text = { 
                Text("Tu caso ha sido compartido en la comunidad. Otros usuarios podrán verlo y ayudarte.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPostSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Ver Comunidad")
                }
            }
        )
    }
}
