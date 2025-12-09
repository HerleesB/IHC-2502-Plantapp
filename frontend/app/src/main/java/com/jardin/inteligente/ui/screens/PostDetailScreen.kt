package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jardin.inteligente.model.CommentResponse
import com.jardin.inteligente.model.CommunityPostResponse
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.CommunityViewModel
import com.jardin.inteligente.viewmodel.CommunityViewModelFactory

/**
 * Pantalla de detalle del post con todos los comentarios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    isGuestMode: Boolean = false,
    onNavigateBack: () -> Unit,
    onLoginRequired: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: CommunityViewModel = viewModel(
        factory = CommunityViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    // Encontrar el post actual
    val post = uiState.posts.find { it.id == postId }
    val comments = uiState.comments[postId] ?: emptyList()
    val isLiked = uiState.userLikes[postId] ?: false
    
    var commentText by remember { mutableStateOf("") }
    var showLoginDialog by remember { mutableStateOf(false) }
    
    // Cargar comentarios al entrar
    LaunchedEffect(postId) {
        viewModel.loadComments(postId)
    }
    
    // Limpiar campo al agregar comentario exitosamente
    LaunchedEffect(uiState.commentSuccess) {
        if (uiState.commentSuccess) {
            commentText = ""
            viewModel.clearCommentSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de publicación") },
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
        },
        bottomBar = {
            // Barra inferior para agregar comentario
            if (!isGuestMode) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Escribe un comentario...") },
                            maxLines = 2,
                            enabled = !uiState.isAddingComment,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                cursorColor = GreenPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.addComment(postId, commentText)
                                }
                            },
                            enabled = commentText.isNotBlank() && !uiState.isAddingComment
                        ) {
                            if (uiState.isAddingComment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Enviar",
                                    tint = if (commentText.isNotBlank()) GreenPrimary else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (post == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando publicación...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Contenido del post
                item {
                    PostDetailCard(
                        post = post,
                        isLiked = isLiked,
                        onLike = {
                            if (isGuestMode) {
                                showLoginDialog = true
                            } else {
                                viewModel.toggleLikePost(post.id)
                            }
                        }
                    )
                }
                
                // Sección de comentarios
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Comment,
                            contentDescription = null,
                            tint = BlueInfo
                        )
                        Text(
                            "Comentarios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = CircleShape,
                            color = BlueInfo.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${post.commentsCount}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = BlueInfo
                            )
                        }
                    }
                }
                
                // Banner para modo invitado
                if (isGuestMode) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = YellowWarning.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = YellowWarning
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Modo Invitado",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Inicia sesión para dejar un comentario",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                TextButton(onClick = onLoginRequired) {
                                    Text("Entrar", color = GreenPrimary)
                                }
                            }
                        }
                    }
                }
                
                // Loading de comentarios
                if (uiState.isLoadingComments) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (comments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No hay comentarios aún",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    "¡Sé el primero en comentar y ayudar!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    // Lista de todos los comentarios
                    items(comments) { comment ->
                        CommentDetailItem(comment = comment)
                    }
                }
                
                // Espacio al final para que no se tape con la barra de comentarios
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
    
    // Login dialog
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            icon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = YellowWarning,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Acción Restringida") },
            text = { 
                Text("Para interactuar con la comunidad necesitas iniciar sesión.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginDialog = false
                        onLoginRequired()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Iniciar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Card con el detalle completo del post
 */
@Composable
fun PostDetailCard(
    post: CommunityPostResponse,
    isLiked: Boolean = false,
    onLike: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con autor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = if (post.isAnonymous) Color.Gray.copy(alpha = 0.3f) else GreenLight
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (post.isAnonymous) Icons.Default.PersonOff else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (post.isAnonymous) Color.Gray else GreenPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (post.isAnonymous) "Usuario Anónimo" else post.authorName ?: "Usuario #${post.userId}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.createdAt.take(10),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                // Status badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (post.status) {
                            "approved" -> GreenLight
                            "resolved" -> BlueInfo.copy(alpha = 0.2f)
                            else -> Color.LightGray
                        }
                    )
                ) {
                    Text(
                        text = when (post.status) {
                            "approved" -> "✓ Aprobado"
                            "resolved" -> "✓ Resuelto"
                            else -> post.status
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = when (post.status) {
                            "approved" -> GreenPrimary
                            "resolved" -> BlueInfo
                            else -> Color.Gray
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Imagen del post
            post.imageUrl?.let { imageUrl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Imagen del post",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Información de la planta
            post.plantName?.let { plantName ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GreenLight.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFlorist,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Planta",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = plantName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Síntomas
            post.symptoms?.let { symptoms ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = YellowWarning.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Healing,
                            contentDescription = null,
                            tint = YellowWarning,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Síntomas",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = symptoms,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Descripción completa
            post.description?.let { description ->
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Acciones (likes) - Corazón relleno si dio like, solo borde si no
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (isLiked) {
                            Icons.Default.Favorite  // Corazón relleno
                        } else {
                            Icons.Outlined.FavoriteBorder  // Corazón solo borde
                        },
                        contentDescription = if (isLiked) "Quitar me gusta" else "Me gusta",
                        tint = RedError,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    "${post.likes} me gusta",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Item de comentario para la pantalla de detalle (muestra nombre y contenido)
 */
@Composable
fun CommentDetailItem(comment: CommentResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = GreenLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Nombre del autor
                        Text(
                            text = comment.authorName ?: "Usuario #${comment.userId}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                        
                        // Badge si es solución
                        if (comment.isSolution) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GreenPrimary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Solución",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    
                    // Fecha del comentario
                    Text(
                        text = comment.createdAt.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Contenido del comentario
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.87f)
            )
        }
    }
}
