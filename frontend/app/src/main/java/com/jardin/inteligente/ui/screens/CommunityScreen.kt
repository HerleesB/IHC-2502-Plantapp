package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jardin.inteligente.model.CommentResponse
import com.jardin.inteligente.model.CommunityPostResponse
import com.jardin.inteligente.ui.theme.*
import com.jardin.inteligente.viewmodel.CommunityViewModel
import com.jardin.inteligente.viewmodel.CommunityViewModelFactory

/**
 * CU-07, CU-09, CU-19: Pantalla de Comunidad - Feed de posts, comentarios y compartir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    isGuestMode: Boolean = false,
    onNavigateToShare: () -> Unit = {},
    onNavigateToPostDetail: (Int) -> Unit = {},
    onLoginRequired: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: CommunityViewModel = viewModel(
        factory = CommunityViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showLoginDialog by remember { mutableStateOf(false) }
    
    // Estado para el diálogo de comentarios
    var showCommentsDialog by remember { mutableStateOf(false) }
    var selectedPostForComments by remember { mutableStateOf<CommunityPostResponse?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidad") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadPosts() }) {
                        Icon(Icons.Default.Refresh, "Actualizar", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isGuestMode) {
                        showLoginDialog = true
                    } else {
                        onNavigateToShare()
                    }
                },
                containerColor = GreenPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartir")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading && uiState.posts.isEmpty() -> {
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
                        Button(onClick = { viewModel.loadPosts() }) {
                            Text("Reintentar")
                        }
                    }
                }
                uiState.posts.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Groups, 
                            null, 
                            modifier = Modifier.size(80.dp), 
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "La comunidad está vacía",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sé el primero en compartir un caso",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        if (!isGuestMode) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavigateToShare,
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Compartir Caso")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Guest mode banner
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
                                                "Inicia sesión para comentar, dar likes y compartir",
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
                        
                        items(uiState.posts) { post ->
                            // Obtener si el usuario dio like a este post
                            val isLiked = uiState.userLikes[post.id] ?: false
                            
                            CommunityPostCard(
                                post = post,
                                isLiked = isLiked,
                                isGuestMode = isGuestMode,
                                onLike = { 
                                    if (isGuestMode) {
                                        showLoginDialog = true
                                    } else {
                                        viewModel.toggleLikePost(post.id)
                                    }
                                },
                                onComment = { 
                                    if (isGuestMode) {
                                        showLoginDialog = true
                                    } else {
                                        selectedPostForComments = post
                                        viewModel.loadComments(post.id)
                                        showCommentsDialog = true
                                    }
                                },
                                onViewDetails = {
                                    onNavigateToPostDetail(post.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo modal de comentarios
    if (showCommentsDialog && selectedPostForComments != null) {
        CommentsDialog(
            post = selectedPostForComments!!,
            comments = uiState.comments[selectedPostForComments!!.id] ?: emptyList(),
            isLoading = uiState.isLoadingComments,
            isAddingComment = uiState.isAddingComment,
            onDismiss = { 
                showCommentsDialog = false
                selectedPostForComments = null
                viewModel.clearCommentSuccess()
            },
            onAddComment = { content ->
                viewModel.addComment(selectedPostForComments!!.id, content)
            },
            onViewAllComments = {
                showCommentsDialog = false
                onNavigateToPostDetail(selectedPostForComments!!.id)
                selectedPostForComments = null
            }
        )
    }
    
    // Login dialog for guests
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
                Text("Para interactuar con la comunidad necesitas iniciar sesión o crear una cuenta.") 
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
 * Diálogo modal para ver los últimos 2 comentarios y agregar uno nuevo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsDialog(
    post: CommunityPostResponse,
    comments: List<CommentResponse>,
    isLoading: Boolean,
    isAddingComment: Boolean,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    onViewAllComments: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val latestComments = comments.take(2)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Comment,
                    contentDescription = null,
                    tint = BlueInfo
                )
                Text("Comentarios")
                if (post.commentsCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = BlueInfo.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${post.commentsCount}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = BlueInfo
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mostrar los últimos 2 comentarios
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else if (latestComments.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No hay comentarios aún",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                "¡Sé el primero en comentar!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    latestComments.forEach { comment ->
                        CommentItem(comment = comment)
                    }
                    
                    // Botón para ver todos los comentarios si hay más de 2
                    if (post.commentsCount > 2) {
                        TextButton(
                            onClick = onViewAllComments,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Ver todos los comentarios (${post.commentsCount})",
                                color = GreenPrimary
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Campo para agregar comentario
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Escribe un comentario...") },
                    maxLines = 3,
                    enabled = !isAddingComment,
                    trailingIcon = {
                        if (isAddingComment) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        onAddComment(commentText)
                                        commentText = ""
                                    }
                                },
                                enabled = commentText.isNotBlank()
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Enviar",
                                    tint = if (commentText.isNotBlank()) GreenPrimary else Color.Gray
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onViewAllComments) {
                Text("Ver detalles", color = GreenPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

/**
 * Item individual de comentario
 */
@Composable
fun CommentItem(comment: CommentResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar del usuario
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = GreenLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    // Nombre del autor
                    Text(
                        text = comment.authorName ?: "Usuario #${comment.userId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                    // Fecha
                    Text(
                        text = comment.createdAt.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                
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
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Solución",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contenido del comentario
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostCard(
    post: CommunityPostResponse,
    isLiked: Boolean = false,
    isGuestMode: Boolean = false,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = if (post.isAnonymous) Color.Gray.copy(alpha = 0.3f) else GreenLight
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (post.isAnonymous) Icons.Default.PersonOff else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (post.isAnonymous) Color.Gray else GreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (post.isAnonymous) {
                                "Usuario Anónimo"
                            } else {
                                post.authorName ?: "Usuario #${post.userId}"
                            },
                            style = MaterialTheme.typography.titleSmall,
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
                if (post.status != "pending") {
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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (post.status) {
                                "approved" -> GreenPrimary
                                "resolved" -> BlueInfo
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Post image if available
            post.imageUrl?.let { imageUrl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Imagen del post",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Plant name and symptoms
            post.plantName?.let { plantName ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalFlorist,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = plantName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            post.symptoms?.let { symptoms ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Healing,
                        contentDescription = null,
                        tint = YellowWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = symptoms,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Description
            post.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Content preview card (if no description)
            if (post.description == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalFlorist,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Diagnóstico #${post.diagnosisId}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ver diagnóstico completo y ayudar a este usuario",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Likes - Corazón relleno si dio like, solo borde si no
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onLike,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) {
                                    Icons.Default.Favorite  // Corazón relleno
                                } else {
                                    Icons.Outlined.FavoriteBorder  // Corazón solo borde
                                },
                                contentDescription = if (isLiked) "Quitar me gusta" else "Me gusta",
                                tint = RedError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "${post.likes}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Comments
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onComment,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Comment,
                                contentDescription = "Comentarios",
                                tint = BlueInfo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "${post.commentsCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Ver más button
                TextButton(onClick = onViewDetails) {
                    Text("Ver detalles", color = GreenPrimary)
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = GreenPrimary
                    )
                }
            }
        }
    }
}
