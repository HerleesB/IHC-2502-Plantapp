package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jardin.inteligente.model.CommunityPost
import com.jardin.inteligente.model.Contributor
import com.jardin.inteligente.model.PostStatus
import com.jardin.inteligente.ui.theme.*

@Composable
fun CommunityScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Casos", "Compartir", "Top Ayudantes")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Blue600
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
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Comunidad de Jardineros",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Comparte experiencias y ayuda a otros jardineros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFDEEBFF)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("1,247", "Miembros activos")
                    StatItem("358", "Casos resueltos")
                    StatItem("89%", "Tasa de ayuda")
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Blue600
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Content
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    val posts = getCommunityPosts()
                    items(posts) { post ->
                        CommunityPostCard(post)
                    }
                }
                1 -> {
                    item {
                        ShareCaseContent()
                    }
                }
                2 -> {
                    item {
                        TopContributorsContent()
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFDEEBFF)
        )
    }
}

@Composable
fun CommunityPostCard(post: CommunityPost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Author info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Green600,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(
                            text = post.author.first().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.wrapContentSize(Alignment.Center)
                        )
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = post.author,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (post.isExpert) {
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                "Experto",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Yellow400.copy(alpha = 0.2f),
                                        labelColor = Color(0xFF92400E)
                                    )
                                )
                            }
                        }
                        Text(
                            text = post.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                when (post.status) {
                    PostStatus.RESOLVED -> AssistChip(
                        onClick = { },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Resuelto", style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Green50,
                            labelColor = Green700
                        )
                    )
                    PostStatus.OPEN -> AssistChip(
                        onClick = { },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Abierto", style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFDEEBFF),
                            labelColor = Blue700
                        )
                    )
                    PostStatus.SUCCESS -> AssistChip(
                        onClick = { },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Éxito", style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFF3E8FF),
                            labelColor = Purple700
                        )
                    )
                }
            }

            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(post.plant, style = MaterialTheme.typography.labelSmall) }
                )
                AssistChip(
                    onClick = { },
                    label = { Text(post.issue, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Orange500.copy(alpha = 0.1f),
                        labelColor = Orange500
                    )
                )
            }

            // Description
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Image
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.plant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Diagnosis card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFDEEBFF)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Blue600,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Diagnóstico IA:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = post.diagnosis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1E40AF),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Stats and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = post.views.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = post.comments.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Green600
                        )
                        Text(
                            text = "${post.helpful} útiles",
                            style = MaterialTheme.typography.bodySmall,
                            color = Green600
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { }) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = post.likes.toString(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // View comments button
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver comentarios y responder")
            }
        }
    }
}

@Composable
fun ShareCaseContent() {
    var anonymousShare by remember { mutableStateOf(false) }
    var selectedPlant by remember { mutableStateOf("Monstera deliciosa") }
    var caseDescription by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Blue600
                    )
                    Text(
                        text = "Compartir mi caso con la comunidad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Publica tu experiencia y recibe ayuda de la comunidad",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // Plant selection
                Text(
                    text = "Selecciona tu planta",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = selectedPlant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )

                // Description
                Text(
                    text = "Describe el problema o logro",
                    style = MaterialTheme.typography.labelMedium
                )
                OutlinedTextField(
                    value = caseDescription,
                    onValueChange = { caseDescription = it },
                    placeholder = {
                        Text("Ejemplo: Las hojas de mi planta se están poniendo amarillas...")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                Text(
                    text = "Sé específico sobre síntomas, ubicación, cuidados recientes y fotos disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // AI generation info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Yellow400.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF92400E)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "El sistema generará automáticamente:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "• Resumen estructurado del caso\n• Diagnóstico inicial por IA\n• Etiquetas relevantes para búsqueda\n• Sugerencias de expertos relacionados",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                }

                // Anonymous option
                Card(
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Publicar de forma anónima",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Tu nombre no será visible en la publicación",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = anonymousShare,
                            onCheckedChange = { anonymousShare = it }
                        )
                    }
                }

                // Share button
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publicar caso")
                }

                if (!anonymousShare) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Green50
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Green600
                            )
                            Text(
                                text = "Al compartir con tu nombre, ganarás +50 puntos de reputación",
                                style = MaterialTheme.typography.bodySmall,
                                color = Green800
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopContributorsContent() {
    val contributors = listOf(
        Contributor("Ana Martínez", "AM", 1850, "Experta"),
        Contributor("Pedro López", "PL", 1420, "Colaborador"),
        Contributor("Laura Sánchez", "LS", 980, "Ayudante")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Yellow400
                    )
                    Text(
                        text = "Top Colaboradores del Mes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Usuarios que más han ayudado a la comunidad",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        contributors.forEachIndexed { index, contributor ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Gray50
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Surface(
                                shape = CircleShape,
                                color = Green600,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Text(
                                    text = contributor.avatar,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.wrapContentSize(Alignment.Center)
                                )
                            }
                            if (index == 0) {
                                Text(
                                    text = "👑",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = contributor.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        contributor.badge,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = contributor.points.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "puntos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3E8FF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "¡Únete al ranking!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Purple700
                )
                Text(
                    text = "Responde preguntas, comparte tus éxitos y ayuda a otros jardineros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Purple600
                )
                OutlinedButton(
                    onClick = { },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Purple600
                    )
                ) {
                    Text("Ver cómo ganar puntos")
                }
            }
        }
    }
}

fun getCommunityPosts() = listOf(
    CommunityPost(
        id = 1,
        author = "María García",
        authorAvatar = "MG",
        isExpert = true,
        date = "Hace 2 horas",
        plant = "Monstera deliciosa",
        issue = "Hojas amarillas",
        description = "Mi monstera ha desarrollado hojas amarillas en la parte inferior. He seguido el plan de cuidado pero no mejora.",
        imageUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?w=600",
        diagnosis = "Posible exceso de riego",
        likes = 24,
        comments = 8,
        views = 156,
        helpful = 12,
        status = PostStatus.RESOLVED
    ),
    CommunityPost(
        id = 2,
        author = "Usuario Anónimo",
        authorAvatar = "?",
        isExpert = false,
        date = "Hace 5 horas",
        plant = "Rosa del desierto",
        issue = "Hojas caídas",
        description = "Las hojas de mi adenium se están cayendo. La tengo en interior con luz indirecta.",
        imageUrl = "https://images.unsplash.com/photo-1590065707696-8b0bbf4930d0?w=600",
        diagnosis = "Falta de luz solar directa",
        likes = 18,
        comments = 5,
        views = 89,
        helpful = 7,
        status = PostStatus.OPEN
    ),
    CommunityPost(
        id = 3,
        author = "Carlos Rodríguez",
        authorAvatar = "CR",
        isExpert = true,
        date = "Hace 1 día",
        plant = "Suculenta",
        issue = "Recuperación exitosa",
        description = "¡Mi suculenta se ha recuperado completamente! Gracias a los consejos de la comunidad.",
        imageUrl = "https://images.unsplash.com/photo-1459156212016-c812468e2115?w=600",
        diagnosis = "Caso de éxito",
        likes = 45,
        comments = 12,
        views = 234,
        helpful = 28,
        status = PostStatus.SUCCESS
    )
)
