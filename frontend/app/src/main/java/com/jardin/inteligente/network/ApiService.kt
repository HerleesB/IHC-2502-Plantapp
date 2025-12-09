package com.jardin.inteligente.network

import com.jardin.inteligente.model.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * Interface de la API REST
 */
interface ApiService {
    
    // ========== DIAGNOSIS ENDPOINTS ==========
    
    /**
     * Validar calidad de foto capturada (CU-01)
     */
    @Multipart
    @POST("api/diagnosis/capture-guidance")
    suspend fun validateCapturedPhoto(
        @Part image: MultipartBody.Part
    ): Response<CaptureGuidanceResponse>
    
    /**
     * Obtener diagnóstico completo de planta (CU-02)
     */
    @Multipart
    @POST("api/diagnosis/analyze")
    suspend fun analyzePlant(
        @Part("plant_id") plantId: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("symptoms") symptoms: RequestBody?,
        @Part("user_id") userId: RequestBody
    ): Response<DiagnosisResponse>
    
    /**
     * Obtener historial de diagnósticos (CU-08)
     */
    @GET("api/diagnosis/history/{user_id}")
    suspend fun getDiagnosisHistory(
        @Path("user_id") userId: Int,
        @Query("limit") limit: Int = 20
    ): Response<DiagnosisHistoryResponse>
    
    /**
     * Obtener historial de diagnósticos por planta (CU-08)
     */
    @GET("api/diagnosis/plant/{plant_id}/history")
    suspend fun getDiagnosisHistoryByPlant(
        @Path("plant_id") plantId: Int,
        @Query("limit") limit: Int = 20
    ): Response<List<DiagnosisHistoryItem>>
    
    /**
     * Feedback/corrección del diagnóstico (CU-12)
     */
    @POST("api/diagnosis/{diagnosis_id}/feedback")
    suspend fun submitDiagnosisFeedback(
        @Path("diagnosis_id") diagnosisId: Int,
        @Body feedback: DiagnosisFeedbackRequest,
        @Query("user_id") userId: Int = 1
    ): Response<FeedbackResponse>
    
    /**
     * Verificar si el usuario ya envió feedback para un diagnóstico (CU-12)
     */
    @GET("api/diagnosis/{diagnosis_id}/feedback/user/{user_id}")
    suspend fun getUserFeedbackForDiagnosis(
        @Path("diagnosis_id") diagnosisId: Int,
        @Path("user_id") userId: Int
    ): Response<UserFeedbackResponse>
    
    // ========== PLANTS ENDPOINTS ==========
    
    /**
     * Obtener plantas del usuario (CU-04, CU-16)
     */
    @GET("api/plants/user/{user_id}")
    suspend fun getUserPlants(
        @Path("user_id") userId: Int
    ): Response<List<PlantResponse>>
    
    /**
     * Obtener planta por ID (CU-08)
     */
    @GET("api/plants/{plant_id}")
    suspend fun getPlantById(
        @Path("plant_id") plantId: Int
    ): Response<PlantResponse>
    
    /**
     * Crear nueva planta (CU-04)
     */
    @POST("api/plants")
    suspend fun createPlant(
        @Body plant: PlantCreateRequest
    ): Response<PlantResponse>
    
    /**
     * Crear planta con imagen desde diagnóstico (CU-20)
     */
    @Multipart
    @POST("api/plants/with-diagnosis")
    suspend fun createPlantWithDiagnosis(
        @Part("name") name: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("species") species: RequestBody?,
        @Part("location") location: RequestBody?,
        @Part("diagnosis_id") diagnosisId: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<PlantResponse>
    
    /**
     * Actualizar planta (CU-04)
     */
    @PUT("api/plants/{plant_id}")
    suspend fun updatePlant(
        @Path("plant_id") plantId: Int,
        @Body plant: PlantUpdateRequest
    ): Response<PlantResponse>
    
    /**
     * Eliminar planta (CU-04)
     */
    @DELETE("api/plants/{plant_id}")
    suspend fun deletePlant(
        @Path("plant_id") plantId: Int,
        @Query("user_id") userId: Int
    ): Response<DeletePlantResponse>
    
    /**
     * Registrar riego de planta (CU-06)
     */
    @PUT("api/plants/{plant_id}/water")
    suspend fun waterPlant(
        @Path("plant_id") plantId: Int
    ): Response<Unit>
    
    /**
     * Obtener estadísticas de progreso (CU-08)
     */
    @GET("api/plants/user/{user_id}/progress")
    suspend fun getProgressStats(
        @Path("user_id") userId: Int
    ): Response<ProgressStatsResponse>
    
    // ========== COMMUNITY ENDPOINTS ==========
    
    /**
     * Obtener posts de comunidad (CU-19)
     */
    @GET("api/community/posts")
    suspend fun getCommunityPosts(
        @Query("limit") limit: Int = 20
    ): Response<List<CommunityPostResponse>>
    
    /**
     * Crear post en comunidad (CU-07)
     */
    @POST("api/community/posts")
    suspend fun createCommunityPost(
        @Body post: CommunityPostCreateRequest,
        @Query("user_id") userId: Int = 1
    ): Response<CommunityPostResponse>
    
    /**
     * Crear post con imagen directamente (CU-18)
     */
    @Multipart
    @POST("api/community/posts/with-image")
    suspend fun createCommunityPostWithImage(
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("plant_name") plantName: RequestBody?,
        @Part("symptoms") symptoms: RequestBody?,
        @Part("is_anonymous") isAnonymous: RequestBody,
        @Part("user_id") userId: RequestBody
    ): Response<CommunityPostResponse>
    
    /**
     * Dar like a post (CU-19)
     */
    @POST("api/community/posts/{post_id}/like")
    suspend fun likePost(
        @Path("post_id") postId: Int
    ): Response<Unit>
    
    /**
     * Obtener comentarios de un post (CU-09)
     */
    @GET("api/community/posts/{post_id}/comments")
    suspend fun getPostComments(
        @Path("post_id") postId: Int
    ): Response<List<CommentResponse>>
    
    /**
     * Agregar comentario a post (CU-09)
     */
    @POST("api/community/posts/{post_id}/comments")
    suspend fun addComment(
        @Path("post_id") postId: Int,
        @Body comment: CommentCreateRequest,
        @Query("user_id") userId: Int = 1
    ): Response<Unit>
    
    // ========== GAMIFICATION ENDPOINTS ==========
    
    /**
     * Obtener logros del usuario (CU-06, CU-17)
     */
    @GET("api/gamification/achievements/{user_id}")
    suspend fun getAchievements(
        @Path("user_id") userId: Int
    ): Response<AchievementsResponse>
    
    /**
     * Obtener misiones del usuario (CU-06)
     */
    @GET("api/gamification/missions/{user_id}")
    suspend fun getMissions(
        @Path("user_id") userId: Int
    ): Response<MissionsResponse>
    
    /**
     * Obtener stats de gamificación (CU-17)
     */
    @GET("api/gamification/stats/{user_id}")
    suspend fun getGamificationStats(
        @Path("user_id") userId: Int
    ): Response<GamificationStatsResponse>
    
    // ========== REMINDERS ENDPOINTS ==========
    
    /**
     * Obtener recordatorios del usuario (CU-06)
     */
    @GET("api/reminders/user/{user_id}")
    suspend fun getReminders(
        @Path("user_id") userId: Int
    ): Response<List<ReminderResponse>>
    
    /**
     * Crear recordatorio (CU-06)
     */
    @POST("api/reminders")
    suspend fun createReminder(
        @Body reminder: ReminderCreateRequest
    ): Response<ReminderResponse>
    
    /**
     * Marcar recordatorio como completado (CU-06)
     */
    @PUT("api/reminders/{reminder_id}/complete")
    suspend fun completeReminder(
        @Path("reminder_id") reminderId: Int
    ): Response<Unit>
    
    // ========== AUTH ENDPOINTS ==========
    
    /**
     * Registrar usuario (CU-15)
     */
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<TokenResponse>
    
    /**
     * Iniciar sesión (CU-15)
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>
    
    /**
     * Obtener usuario actual (CU-15)
     */
    @GET("api/auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>
    
    /**
     * Refrescar token
     */
    @POST("api/auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): Response<TokenResponse>

    companion object {
        private var instance: ApiService? = null
        
        fun getInstance(): ApiService {
            if (instance == null) {
                instance = create()
            }
            return instance!!
        }
        
        private fun create(): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (ApiConfig.ENABLE_LOGGING) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
    }
}
