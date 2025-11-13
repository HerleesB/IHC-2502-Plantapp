package com.jardin.inteligente.model

import com.google.gson.annotations.SerializedName

/**
 * DTOs (Data Transfer Objects) para comunicación con la API
 */

// ========== REQUEST MODELS ==========

data class CaptureGuidanceRequest(
    val image: String // Base64 encoded image o se usa Multipart
)

// ========== RESPONSE MODELS ==========

/**
 * Respuesta de validación de captura de foto
 */
data class CaptureGuidanceResponse(
    @SerializedName("step")
    val step: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("guidance")
    val guidance: String,
    
    @SerializedName("audio_url")
    val audioUrl: String? = null
)

/**
 * Respuesta de diagnóstico completo
 */
data class DiagnosisResponse(
    @SerializedName("diagnosis_id")
    val diagnosisId: Int,
    
    @SerializedName("diagnosis_text")
    val diagnosisText: String,
    
    @SerializedName("disease_name")
    val diseaseName: String?,
    
    @SerializedName("confidence")
    val confidence: Float,
    
    @SerializedName("severity")
    val severity: String,
    
    @SerializedName("recommendations")
    val recommendations: List<String>,
    
    @SerializedName("weekly_plan")
    val weeklyPlan: List<WeeklyTask>,
    
    @SerializedName("audio_url")
    val audioUrl: String? = null
)

/**
 * Tarea semanal del plan de cuidado
 */
data class WeeklyTask(
    @SerializedName("day")
    val day: String,
    
    @SerializedName("task")
    val task: String,
    
    @SerializedName("priority")
    val priority: String
)

/**
 * Respuesta genérica de error de la API
 */
data class ApiError(
    @SerializedName("detail")
    val detail: String
)

/**
 * Estado de resultado de operación
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}
