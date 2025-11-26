package com.jardin.inteligente.repository

import android.util.Log
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlantRepository(private val apiService: ApiService) {
    
    suspend fun getUserPlants(userId: Int): ApiResult<List<PlantResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserPlants(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al obtener plantas: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error getting plants", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun createPlant(plant: PlantCreateRequest): ApiResult<PlantResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPlant(plant)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al crear planta: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error creating plant", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun waterPlant(plantId: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.waterPlant(plantId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Error al regar planta: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error watering plant", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun getProgressStats(userId: Int): ApiResult<ProgressStatsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProgressStats(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al obtener estadísticas: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error getting stats", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
}
