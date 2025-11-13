package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jardin.inteligente.model.Plant
import com.jardin.inteligente.ui.theme.*

@Composable
fun MyGardenScreen() {
    val plants = listOf(
        Plant(
            id = 1,
            name = "Rosa del Desierto",
            species = "Adenium obesum",
            health = 85,
            status = "Saludable",
            nextAction = "Regar en 2 días",
            streak = 14,
            imageUrl = "https://images.unsplash.com/photo-1590065707696-8b0bbf4930d0?w=400"
        ),
        Plant(
            id = 2,
            name = "Monstera",
            species = "Monstera deliciosa",
            health = 65,
            status = "Necesita atención",
            nextAction = "Fertilizar hoy",
            streak = 7,
            imageUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?w=400"
        ),
        Plant(
            id = 3,
            name = "Suculenta Mix",
            species = "Echeveria elegans",
            health = 95,
            status = "Excelente",
            nextAction = "Todo al día",
            streak = 21,
            imageUrl = "https://images.unsplash.com/photo-1459156212016-c812468e2115?w=400"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Green600
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Bienvenido a tu jardín 🌱",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tienes ${plants.size} plantas en crecimiento",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green50
                    )
                }
            }
        }

        // Plants list
        items(plants) { plant ->
            PlantCard(plant = plant)
        }

        // Progress card
        item {
            ProgressCard()
        }
    }
}

@Composable
fun PlantCard(plant: Plant) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = plant.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with streak
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = plant.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = plant.species,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    if (plant.streak >= 14) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🔥")
                                    Text("${plant.streak} días")
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Orange500.copy(alpha = 0.1f),
                                labelColor = Orange500
                            )
                        )
                    }
                }

                // Health progress
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Salud general",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "${plant.health}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Green600,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = plant.health / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            plant.health >= 80 -> Green600
                            plant.health >= 60 -> Yellow400
                            else -> Color.Red
                        }
                    )
                }

                // Next action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Blue600,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = plant.nextAction,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Status badge
                AssistChip(
                    onClick = { },
                    label = { Text(plant.status) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when {
                            plant.health >= 80 -> Green50
                            plant.health >= 60 -> Yellow400.copy(alpha = 0.1f)
                            else -> Color.Red.copy(alpha = 0.1f)
                        },
                        labelColor = when {
                            plant.health >= 80 -> Green700
                            plant.health >= 60 -> Color(0xFF92400E)
                            else -> Color.Red
                        }
                    )
                )

                // View details button
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver detalle")
                }
            }
        }
    }
}

@Composable
fun ProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDEEBFF)
        ),
        shape = RoundedCornerShape(16.dp)
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
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Blue600
                )
                Text(
                    text = "Progreso esta semana",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressItem("12", "Diagnósticos")
                ProgressItem("8", "Riegos")
                ProgressItem("3", "Fertilizaciones")
                ProgressItem("95%", "Adherencia")
            }
        }
    }
}

@Composable
fun ProgressItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
