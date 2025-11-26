package com.jardin.inteligente.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jardin.inteligente.network.ApiConfig
import com.jardin.inteligente.network.ApiService
import com.jardin.inteligente.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NetworkDiagnosticScreen() {
    val scope = rememberCoroutineScope()
    var testResult by remember { mutableStateOf<TestResult?>(null) }
    var isTesting by remember { mutableStateOf(false) }

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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Diagnóstico de Red",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Verifica la conexión con el backend",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEDE9FE)
                    )
                }
            }
        }

        // Configuración actual
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Configuración Actual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ConfigItem(
                        icon = Icons.Default.Dns,
                        label = "URL del Backend",
                        value = ApiConfig.BASE_URL,
                        isUrl = true
                    )

                    ConfigItem(
                        icon = Icons.Default.Timer,
                        label = "Timeout de Conexión",
                        value = "${ApiConfig.CONNECT_TIMEOUT} segundos"
                    )

                    ConfigItem(
                        icon = Icons.Default.Timer,
                        label = "Timeout de Lectura",
                        value = "${ApiConfig.READ_TIMEOUT} segundos"
                    )
                }
            }
        }

        // Instrucciones
        item {
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
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B)
                        )
                        Text(
                            text = "¿Cómo configurar?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "1. En tu PC, abre CMD (Símbolo del sistema)",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "2. Ejecuta: ipconfig",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "3. Busca 'Dirección IPv4' en tu adaptador WiFi/Ethernet",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "4. Copia esa IP (ej: 192.168.1.105)",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "5. Edita el archivo ApiConfig.kt y cambia LOCAL_IP",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Divider()

                    Text(
                        text = "⚠️ IMPORTANTE: Tu teléfono y PC deben estar conectados a la MISMA red WiFi",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }

        // Botón de prueba
        item {
            Button(
                onClick = {
                    scope.launch {
                        isTesting = true
                        testResult = testConnection()
                        isTesting = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTesting
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Probando conexión...")
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Probar Conexión")
                }
            }
        }

        // Resultado de la prueba
        if (testResult != null) {
            item {
                TestResultCard(testResult!!)
            }
        }
    }
}

data class TestResult(
    val success: Boolean,
    val message: String,
    val details: String? = null,
    val responseTime: Long? = null
)

suspend fun testConnection(): TestResult {
    return try {
        val startTime = System.currentTimeMillis()
        
        // Intentar hacer una petición simple al backend
        val response = ApiService.getInstance().validateCapturedPhoto(
            // Esto fallará porque no hay imagen, pero nos dirá si hay conexión
            image = okhttp3.MultipartBody.Part.createFormData("image", "test.jpg")
        )
        
        val endTime = System.currentTimeMillis()
        val responseTime = endTime - startTime

        TestResult(
            success = true,
            message = "✅ Conexión exitosa con el backend",
            details = "El servidor está respondiendo correctamente",
            responseTime = responseTime
        )
    } catch (e: java.net.ConnectException) {
        TestResult(
            success = false,
            message = "❌ No se pudo conectar al servidor",
            details = "Verifica que:\n• El backend esté ejecutándose\n• La IP sea correcta\n• Estés en la misma red WiFi"
        )
    } catch (e: java.net.SocketTimeoutException) {
        TestResult(
            success = false,
            message = "❌ Timeout de conexión",
            details = "El servidor tardó demasiado en responder.\nVerifica tu conexión a internet."
        )
    } catch (e: java.net.UnknownHostException) {
        TestResult(
            success = false,
            message = "❌ No se pudo resolver el host",
            details = "La dirección IP es incorrecta o no se puede alcanzar."
        )
    } catch (e: Exception) {
        // Si llega aquí con un error 400, significa que SÍ hay conexión
        if (e.message?.contains("400") == true || e.message?.contains("HTTP") == true) {
            TestResult(
                success = true,
                message = "✅ Conexión exitosa (error esperado)",
                details = "El servidor está respondiendo. El error es porque enviamos datos de prueba."
            )
        } else {
            TestResult(
                success = false,
                message = "❌ Error inesperado",
                details = e.message ?: "Error desconocido"
            )
        }
    }
}

@Composable
fun ConfigItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isUrl: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Blue600
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontFamily = if (isUrl) FontFamily.Monospace else FontFamily.Default,
                color = if (isUrl) Blue700 else Color.Black
            )
        }
    }
}

@Composable
fun TestResultCard(result: TestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.success) Green50 else Color(0xFFFEE2E2)
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
                    imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (result.success) Green600 else Color.Red,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Resultado de la Prueba",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (result.success) Green800 else Color(0xFF991B1B)
            )

            if (result.details != null) {
                Text(
                    text = result.details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (result.success) Green700 else Color(0xFf7F1D1D)
                )
            }

            if (result.responseTime != null) {
                Text(
                    text = "Tiempo de respuesta: ${result.responseTime}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (!result.success) {
                Divider()
                Text(
                    text = "💡 Consejo: Si el backend está corriendo pero no hay conexión, verifica que ambos dispositivos estén en la misma red WiFi y que tu firewall no esté bloqueando el puerto 8000.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
