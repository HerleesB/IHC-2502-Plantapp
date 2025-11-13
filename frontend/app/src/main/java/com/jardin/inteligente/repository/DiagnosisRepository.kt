package com.jardin.inteligente.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jardin.inteligente.model.ApiResult
import com.jardin.inteligente.model.CaptureGuidanceResponse
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Repository para operaciones de diagnóstico de plantas
 */
class DiagnosisRepository(private val context: Context) {
    
    private val apiService = ApiService.getInstance()
    private val TAG = "DiagnosisRepository"
    
    /**
     * Validar foto capturada con IA
     * 
     * @param imageUri URI de la imagen capturada
     * @return ApiResult con la respuesta de validación
     */
    suspend fun validateCapturedPhoto(imageUri: Uri): ApiResult<CaptureGuidanceResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Iniciando validación de foto: $imageUri")
                
                // Convertir URI a File
                val imageFile = uriToFile(imageUri)
                if (imageFile == null) {
                    Log.e(TAG, "Error al convertir URI a archivo")
                    return@withContext ApiResult.Error("No se pudo leer la imagen")
                }
                
                Log.d(TAG, "Archivo de imagen: ${imageFile.name}, tamaño: ${imageFile.length()} bytes")
                
                // Validar tamaño (máx 10MB)
                if (imageFile.length() > 10 * 1024 * 1024) {
                    return@withContext ApiResult.Error("La imagen es demasiado grande. Máximo 10MB")
                }
                
                // Crear RequestBody para multipart
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    requestFile
                )
                
                Log.d(TAG, "Enviando request a API...")
                
                // Llamar a la API
                val response = apiService.validateCapturedPhoto(imagePart)
                
                Log.d(TAG, "Response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Validación exitosa: success=${body.success}, guidance=${body.guidance}")
                        ApiResult.Success(body)
                    } else {
                        Log.e(TAG, "Response body es null")
                        ApiResult.Error("Respuesta vacía del servidor")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e(TAG, "Error del servidor: ${response.code()} - $errorBody")
                    ApiResult.Error(
                        message = parseErrorMessage(errorBody),
                        code = response.code()
                    )
                }
                
            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "Error de conexión: No se puede conectar al servidor", e)
                ApiResult.Error("No se puede conectar al servidor. Verifica tu conexión a internet y que el backend esté ejecutándose.")
            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Timeout de conexión", e)
                ApiResult.Error("La conexión tardó demasiado. Intenta de nuevo.")
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado en validación", e)
                ApiResult.Error("Error: ${e.localizedMessage ?: "Error desconocido"}")
            }
        }
    }
    
    /**
     * Convierte URI a File temporal
     */
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
            
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir URI a archivo", e)
            null
        }
    }
    
    /**
     * Parsear mensaje de error del servidor
     */
    private fun parseErrorMessage(errorBody: String): String {
        return try {
            // Intentar parsear JSON de error
            val errorJson = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
            errorJson.get("detail")?.asString ?: "Error del servidor"
        } catch (e: Exception) {
            "Error del servidor"
        }
    }
}
