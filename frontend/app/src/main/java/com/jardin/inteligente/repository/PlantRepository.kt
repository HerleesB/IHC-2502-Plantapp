package com.jardin.inteligente.repository

import android.content.Context
import android.util.Log
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para gestión de plantas (CU-04, CU-08, CU-16, CU-20)
 */
class PlantRepository(private val context: Context) {
    
    private val apiService = ApiService.getInstance()
    
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
    
    suspend fun getPlantById(plantId: Int): ApiResult<PlantResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPlantById(plantId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Planta no encontrada: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error getting plant by id", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun createPlant(plant: PlantCreateRequest): ApiResult<PlantResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("PlantRepository", "Creating plant: $plant")
            val response = apiService.createPlant(plant)
            if (response.isSuccessful && response.body() != null) {
                Log.d("PlantRepository", "Plant created successfully: ${response.body()}")
                ApiResult.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PlantRepository", "Error creating plant: ${response.code()} - $errorBody")
                ApiResult.Error("Error al crear planta: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error creating plant", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Crear planta con datos completos (CU-20)
     * @param name Nombre de la planta (requerido)
     * @param userId ID del usuario (requerido)
     * @param species Especie de la planta (opcional)
     * @param location Ubicación de la planta (opcional)
     * @param diagnosisId ID del diagnóstico para vincular (opcional)
     */
    suspend fun createPlant(
        name: String,
        userId: Int,
        species: String? = null,
        location: String? = null,
        diagnosisId: Int? = null
    ): ApiResult<PlantResponse> = withContext(Dispatchers.IO) {
        try {
            val request = PlantCreateRequest(
                name = name,
                userId = userId,
                species = species,
                location = location,
                diagnosisId = diagnosisId
            )
            
            Log.d("PlantRepository", "Creating plant with diagnosis: $request")
            val response = apiService.createPlant(request)
            if (response.isSuccessful && response.body() != null) {
                Log.d("PlantRepository", "Plant created successfully: ${response.body()}")
                ApiResult.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PlantRepository", "Error creating plant: ${response.code()} - $errorBody")
                ApiResult.Error("Error al crear planta: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error creating plant", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun updatePlant(plantId: Int, request: PlantUpdateRequest): ApiResult<PlantResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updatePlant(plantId, request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al actualizar planta: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error updating plant", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun deletePlant(plantId: Int, userId: Int): ApiResult<DeletePlantResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deletePlant(plantId, userId)
            if (response.isSuccessful && response.body() != null) {
                Log.d("PlantRepository", "Plant deleted: ${response.body()!!.message}")
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al eliminar planta: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error deleting plant", e)
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
