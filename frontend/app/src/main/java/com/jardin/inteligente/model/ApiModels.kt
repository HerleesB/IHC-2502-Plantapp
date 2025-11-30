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
 * Planta
 */
data class PlantResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("species")
    val species: String?,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("image_url")
    val imageUrl: String?,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("health_score")
    val healthScore: Int,
    
    @SerializedName("last_watered")
    val lastWatered: String?,
    
    @SerializedName("last_fertilized")
    val lastFertilized: String?,
    
    @SerializedName("created_at")
    val createdAt: String
)

data class PlantCreateRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("species")
    val species: String? = null,
    
    @SerializedName("description")
    val description: String? = null
)

/**
 * Historial de diagnósticos
 */
data class DiagnosisHistoryResponse(
    @SerializedName("diagnoses")
    val diagnoses: List<DiagnosisHistoryItem>,
    
    @SerializedName("total")
    val total: Int
)

data class DiagnosisHistoryItem(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("plant_id")
    val plantId: Int,
    
    @SerializedName("plant_name")
    val plantName: String,
    
    @SerializedName("diagnosis_text")
    val diagnosisText: String,
    
    @SerializedName("disease_name")
    val diseaseName: String?,
    
    @SerializedName("severity")
    val severity: String,
    
    @SerializedName("confidence")
    val confidence: Float,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("recommendations")
    val recommendations: List<String>,
    
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Estadísticas de progreso
 */
data class ProgressStatsResponse(
    @SerializedName("total_plants")
    val totalPlants: Int,
    
    @SerializedName("healthy_plants")
    val healthyPlants: Int,
    
    @SerializedName("diagnoses_count")
    val diagnosesCount: Int,
    
    @SerializedName("streak_days")
    val streakDays: Int,
    
    @SerializedName("level")
    val level: Int,
    
    @SerializedName("xp")
    val xp: Int,
    
    @SerializedName("next_level_xp")
    val nextLevelXp: Int
)

/**
 * Comunidad - Posts
 */
data class CommunityPostResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("diagnosis_id")
    val diagnosisId: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("author_name")
    val authorName: String,
    
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,
    
    @SerializedName("likes")
    val likes: Int,
    
    @SerializedName("comments_count")
    val commentsCount: Int,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("created_at")
    val createdAt: String
)

data class CommunityPostCreateRequest(
    @SerializedName("diagnosis_id")
    val diagnosisId: Int,
    
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean = false
)

data class CommentResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("is_solution")
    val isSolution: Boolean,
    
    @SerializedName("likes")
    val likes: Int,
    
    @SerializedName("created_at")
    val createdAt: String
)

data class CommentCreateRequest(
    @SerializedName("content")
    val content: String,
    
    @SerializedName("is_solution")
    val isSolution: Boolean = false
)

/**
 * Gamificación
 */
data class AchievementResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("icon")
    val icon: String,
    
    @SerializedName("points")
    val points: Int,
    
    @SerializedName("unlocked")
    val unlocked: Boolean,
    
    @SerializedName("progress")
    val progress: Int,
    
    @SerializedName("progress_max")
    val progressMax: Int
)

data class AchievementsResponse(
    @SerializedName("unlocked")
    val unlocked: List<AchievementResponse>,
    
    @SerializedName("locked")
    val locked: List<AchievementResponse>,
    
    @SerializedName("total_points")
    val totalPoints: Int,
    
    @SerializedName("level")
    val level: Int,
    
    @SerializedName("xp")
    val xp: Int,
    
    @SerializedName("next_level_xp")
    val nextLevelXp: Int
)

data class MissionResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("xp")
    val xp: Int,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("progress")
    val progress: Int,
    
    @SerializedName("completed")
    val completed: Boolean
)

data class MissionsResponse(
    @SerializedName("daily")
    val daily: List<MissionResponse>,
    
    @SerializedName("weekly")
    val weekly: List<MissionResponse>,
    
    @SerializedName("streak_days")
    val streakDays: Int,
    
    @SerializedName("level")
    val level: Int,
    
    @SerializedName("xp")
    val xp: Int,
    
    @SerializedName("next_level_xp")
    val nextLevelXp: Int
)

/**
 * Autenticación
 */
data class UserResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("full_name")
    val fullName: String?,
    
    @SerializedName("level")
    val level: Int,
    
    @SerializedName("xp")
    val xp: Int,
    
    @SerializedName("points")
    val points: Int,
    
    @SerializedName("streak_days")
    val streakDays: Int
)

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("token_type")
    val tokenType: String,
    
    @SerializedName("user")
    val user: UserResponse
)

data class LoginRequest(
    @SerializedName("email_or_username")
    val emailOrUsername: String,
    
    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("full_name")
    val fullName: String? = null
)

/**
 * Estado de resultado de operación
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}
