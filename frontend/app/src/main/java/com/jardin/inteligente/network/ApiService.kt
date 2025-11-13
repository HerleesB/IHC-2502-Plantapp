package com.jardin.inteligente.network

import com.jardin.inteligente.model.CaptureGuidanceResponse
import com.jardin.inteligente.model.DiagnosisResponse
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
