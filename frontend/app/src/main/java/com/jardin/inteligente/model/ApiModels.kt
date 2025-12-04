package com.jardin.inteligente.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * DTOs (Data Transfer Objects) para comunicación con la API
 */

// ========== REQUEST MODELS ==========

data class CaptureGuidanceRequest(
    val image: String
)

// ========== RESPONSE MODELS ==========

/**
 * Respuesta de validación de captura de foto (CU-01)
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
 * Respuesta de diagnóstico completo (CU-02)
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
) : Serializable

/**
 * Tarea semanal del plan de cuidado (CU-03)
 */
data class WeeklyTask(
    @SerializedName("day")
    val day: String,
    
    @SerializedName("task")
    val task: String,
    
    @SerializedName("priority")
    val priority: String
) : Serializable

/**
 * Respuesta genérica de error de la API
 */
data class ApiError(
    @SerializedName("detail")
    val detail: String
)

/**
 * Planta (CU-04, CU-08, CU-16)
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
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Request para crear planta (CU-04, CU-20)
 */
data class PlantCreateRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("species")
    val species: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("diagnosis_id")
    val diagnosisId: Int? = null
)

data class PlantUpdateRequest(
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("species")
    val species: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("location")
    val location: String? = null
)

/**
 * Historial de diagnósticos (CU-08)
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
 * Feedback de diagnóstico (CU-12)
 */
data class DiagnosisFeedbackRequest(
    @SerializedName("is_correct")
    val isCorrect: Boolean,
    
    @SerializedName("correct_diagnosis")
    val correctDiagnosis: String? = null,
    
    @SerializedName("feedback_text")
    val feedbackText: String? = null
)

/**
 * Estadísticas de progreso (CU-08)
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
 * Comunidad - Posts (CU-07, CU-18, CU-19)
 */
data class CommunityPostResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("diagnosis_id")
    val diagnosisId: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("author_name")
    val authorName: String?,
    
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,
    
    @SerializedName("likes")
    val likes: Int,
    
    @SerializedName("comments_count")
    val commentsCount: Int,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("plant_name")
    val plantName: String? = null,
    
    @SerializedName("symptoms")
    val symptoms: String? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
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
    
    @SerializedName("author_name")
    val authorName: String? = null,
    
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
 * Gamificación (CU-06, CU-17)
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
    val progressMax: Int,
    
    @SerializedName("unlocked_at")
    val unlockedAt: String? = null
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
    
    @SerializedName("target")
    val target: Int = 1,
    
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

data class GamificationStatsResponse(
    @SerializedName("level")
    val level: Int,
    
    @SerializedName("xp")
    val xp: Int,
    
    @SerializedName("next_level_xp")
    val nextLevelXp: Int,
    
    @SerializedName("total_points")
    val totalPoints: Int,
    
    @SerializedName("streak_days")
    val streakDays: Int,
    
    @SerializedName("unlocked_achievements")
    val unlockedAchievements: Int,
    
    @SerializedName("total_achievements")
    val totalAchievements: Int,
    
    @SerializedName("completed_missions")
    val completedMissions: Int
)

/**
 * Recordatorios (CU-06)
 */
data class ReminderResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("plant_id")
    val plantId: Int,
    
    @SerializedName("plant_name")
    val plantName: String,
    
    @SerializedName("reminder_type")
    val reminderType: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("scheduled_time")
    val scheduledTime: String,
    
    @SerializedName("completed")
    val completed: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String
)

data class ReminderCreateRequest(
    @SerializedName("plant_id")
    val plantId: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("reminder_type")
    val reminderType: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("scheduled_time")
    val scheduledTime: String
)

/**
 * Autenticación (CU-15)
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
