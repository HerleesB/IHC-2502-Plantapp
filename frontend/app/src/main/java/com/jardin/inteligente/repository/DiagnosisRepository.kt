package com.jardin.inteligente.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class DiagnosisRepository(private val context: Context) {
    
    private val apiService = ApiService.getInstance()
    private val userId = 1 // TODO: Get from auth
    
    suspend fun validatePhoto(imageUri: Uri): ApiResult<CaptureGuidanceResponse> = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(imageUri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            val response = apiService.validateCapturedPhoto(imagePart)
            
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al validar foto: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("DiagnosisRepository", "Error validating photo", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun analyzePlant(
        imageUri: Uri,
        plantId: Int,
        symptoms: String? = null
    ): ApiResult<DiagnosisResponse> = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(imageUri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            val plantIdBody = plantId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val symptomsBody = symptoms?.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.analyzePlant(plantIdBody, imagePart, symptomsBody, userIdBody)
            
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error en diagnóstico: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("DiagnosisRepository", "Error analyzing plant", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            inputStream?.copyTo(output)
        }
        return file
    }
}
