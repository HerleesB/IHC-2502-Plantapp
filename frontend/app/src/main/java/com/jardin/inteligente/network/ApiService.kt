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
    
    /**
     * Validar calidad de foto capturada
     */
    @Multipart
    @POST("api/diagnosis/capture-guidance")
    suspend fun validateCapturedPhoto(
        @Part image: MultipartBody.Part
    ): Response<CaptureGuidanceResponse>
    
    /**
     * Obtener diagnóstico completo de planta
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
     * Obtener historial de diagnósticos
     */
    @GET("api/diagnosis/history/{user_id}")
    suspend fun getDiagnosisHistory(
        @Path("user_id") userId: Int,
        @Query("limit") limit: Int = 20
    ): Response<DiagnosisHistoryResponse>
    
    /**
     * Obtener plantas del usuario
     */
    @GET("api/plants/user/{user_id}")
    suspend fun getUserPlants(
        @Path("user_id") userId: Int
    ): Response<List<PlantResponse>>
    
    /**
     * Crear nueva planta
     */
    @POST("api/plants")
    suspend fun createPlant(
        @Body plant: PlantCreateRequest
    ): Response<PlantResponse>
    
    /**
     * Registrar riego de planta
     */
    @PUT("api/plants/{plant_id}/water")
    suspend fun waterPlant(
        @Path("plant_id") plantId: Int
    ): Response<Unit>
    
    /**
     * Obtener estadísticas de progreso
     */
    @GET("api/plants/user/{user_id}/progress")
    suspend fun getProgressStats(
        @Path("user_id") userId: Int
    ): Response<ProgressStatsResponse>
    
    // ========== COMMUNITY ENDPOINTS ==========
    
    /**
     * Obtener posts de comunidad
     */
    @GET("api/community/posts")
    suspend fun getCommunityPosts(
        @Query("limit") limit: Int = 20
    ): Response<List<CommunityPostResponse>>
    
    /**
     * Crear post en comunidad
     */
    @POST("api/community/posts")
    suspend fun createCommunityPost(
        @Body post: CommunityPostCreateRequest,
        @Query("user_id") userId: Int = 1
    ): Response<CommunityPostResponse>
    
    /**
     * Dar like a post
     */
    @POST("api/community/posts/{post_id}/like")
    suspend fun likePost(
        @Path("post_id") postId: Int
    ): Response<Unit>
    
    /**
     * Obtener comentarios de un post
     */
    @GET("api/community/posts/{post_id}/comments")
    suspend fun getPostComments(
        @Path("post_id") postId: Int
    ): Response<List<CommentResponse>>
    
    /**
     * Agregar comentario a post
     */
    @POST("api/community/posts/{post_id}/comments")
    suspend fun addComment(
        @Path("post_id") postId: Int,
        @Body comment: CommentCreateRequest,
        @Query("user_id") userId: Int = 1
    ): Response<Unit>
    
    // ========== GAMIFICATION ENDPOINTS ==========
    
    /**
     * Obtener logros del usuario
     */
    @GET("api/gamification/achievements/{user_id}")
    suspend fun getAchievements(
        @Path("user_id") userId: Int
    ): Response<AchievementsResponse>
    
    /**
     * Obtener misiones del usuario
     */
    @GET("api/gamification/missions/{user_id}")
    suspend fun getMissions(
        @Path("user_id") userId: Int
    ): Response<MissionsResponse>
    
    // ========== AUTH ENDPOINTS ==========
    
    /**
     * Registrar usuario
     */
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<TokenResponse>
    
    /**
     * Iniciar sesión
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>
    
    /**
     * Obtener usuario actual
     */
    @GET("api/auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>
    
    // ========== COMMUNITY ENDPOINTS ==========
    
    /**
     * Obtener posts de la comunidad
     */
    @GET("api/community/posts")
    suspend fun getCommunityPosts(
        @Query("limit") limit: Int = 20
    ): Response<List<CommunityPostResponse>>
    
    /**
     * Crear post en comunidad
     */
    @POST("api/community/posts")
    suspend fun createCommunityPost(
        @Body post: CommunityPostCreateRequest,
        @Query("user_id") userId: Int = 1
    ): Response<CommunityPostResponse>
    
    /**
     * Agregar comentario
     */
    @POST("api/community/posts/{post_id}/comments")
    suspend fun addComment(
        @Path("post_id") postId: Int,
        @Body comment: CommentCreateRequest,
        @Query("user_id") userId: Int = 1
    ): Response<Unit>
    
    // ========== GAMIFICATION ENDPOINTS ==========
    
    /**
     * Obtener logros del usuario
     */
    @GET("api/gamification/achievements/{user_id}")
    suspend fun getAchievements(
        @Path("user_id") userId: Int
    ): Response<AchievementsResponse>
    
    /**
     * Obtener misiones del usuario
     */
    @GET("api/gamification/missions/{user_id}")
    suspend fun getMissions(
        @Path("user_id") userId: Int
    ): Response<MissionsResponse>
    
    companion object {
        /**
         * Singleton para obtener instancia de ApiService
         */
        private var instance: ApiService? = null
        
        fun getInstance(): ApiService {
            if (instance == null) {
                instance = create()
            }
            return instance!!
        }
        
        /**
         * Crear instancia de Retrofit con configuración
         */
        private fun create(): ApiService {
            // Logging interceptor para debug
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (ApiConfig.ENABLE_LOGGING) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
            
            // Cliente HTTP con configuración
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
            
            // Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
    }
}
