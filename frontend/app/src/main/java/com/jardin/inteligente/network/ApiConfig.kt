package com.jardin.inteligente.network

/**
 * Configuración de la API
 */
object ApiConfig {
    /**
     * URL base de la API
     * 
     * INSTRUCCIONES PARA CONFIGURAR:
     * ================================
     * 
     * 1. DISPOSITIVO FÍSICO (tu caso):
     *    - Encuentra tu IP local en Windows:
     *      • Abre CMD y ejecuta: ipconfig
     *      • Busca "Adaptador de LAN inalámbrica Wi-Fi" o "Adaptador de Ethernet"
     *      • Copia la "Dirección IPv4" (ej: 192.168.1.105)
     *    - Cambia LOCAL_IP abajo con tu IP
     *    - Cambia USE_EMULATOR = false
     *    - IMPORTANTE: Tu teléfono y PC deben estar en la MISMA red WiFi
     * 
     * 2. EMULADOR ANDROID:
     *    - Cambia USE_EMULATOR = true
     *    - No necesitas cambiar nada más
     * 
     * 3. PRODUCCIÓN:
     *    - Cambia BASE_URL directamente a tu servidor
     */
    
    // ========== CONFIGURACIÓN - EDITA AQUÍ ==========
    
    private const val USE_EMULATOR = true // true = emulador, false = dispositivo físico
    
    // CAMBIA ESTA IP POR LA IP DE TU PC EN LA RED LOCAL
    // Para encontrarla: CMD > ipconfig > busca "Dirección IPv4"
    private const val LOCAL_IP = "192.168.18.5" // 👈 CAMBIAR AQUÍ
    
    // =================================================
    
    val BASE_URL: String = when {
        USE_EMULATOR -> {
            // Emulador Android usa 10.0.2.2 para acceder a localhost del host
            "http://10.0.2.2:8000/"
        }
        else -> {
            // Dispositivo físico: usa IP local de la PC
            "http://$LOCAL_IP:8000/"
        }
    }
    
    // Timeouts (aumentados para dar más tiempo al análisis de IA)
    const val CONNECT_TIMEOUT = 60L // segundos
    const val READ_TIMEOUT = 60L // segundos - tiempo para esperar respuesta de IA
    const val WRITE_TIMEOUT = 60L // segundos - tiempo para subir imagen
    
    // Configuración
    const val ENABLE_LOGGING = true // Logs detallados de red (solo debug)
    
    /**
     * Información de configuración para debug
     */
    fun getDebugInfo(): String {
        return buildString {
            appendLine("📡 Configuración de Red")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Modo: ${if (USE_EMULATOR) "Emulador" else "Dispositivo Físico"}")
            appendLine("URL: $BASE_URL")
            appendLine("Timeout conexión: ${CONNECT_TIMEOUT}s")
            appendLine("Timeout lectura: ${READ_TIMEOUT}s")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
        }
    }
}
