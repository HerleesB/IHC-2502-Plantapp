package com.jardin.inteligente.repository

import android.util.Log
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GamificationRepository(private val apiService: ApiService) {
    
    suspend fun getAchievements(userId: Int): ApiResult<AchievementsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAchievements(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al obtener logros: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("GamificationRepository", "Error getting achievements", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun getMissions(userId: Int): ApiResult<MissionsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMissions(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al obtener misiones: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("GamificationRepository", "Error getting missions", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
}
