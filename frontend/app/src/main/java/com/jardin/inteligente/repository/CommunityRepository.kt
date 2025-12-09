package com.jardin.inteligente.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiConfig
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class CommunityRepository(private val apiService: ApiService) {
    
    suspend fun getPosts(limit: Int = 20): ApiResult<List<CommunityPostResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCommunityPosts(limit)
            if (response.isSuccessful && response.body() != null) {
                // Procesar posts para agregar URL base a las imágenes
                val postsWithFullImageUrl = response.body()!!.map { post ->
                    post.copy(
                        imageUrl = buildFullImageUrl(post.imageUrl)
                    )
                }
                ApiResult.Success(postsWithFullImageUrl)
            } else {
                ApiResult.Error("Error al obtener posts: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error getting posts", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Construir URL completa para la imagen
     * Convierte rutas relativas como "uploads/community/xxx.jpg" 
     * a URLs completas como "http://192.168.18.5:8000/uploads/community/xxx.jpg"
     */
    private fun buildFullImageUrl(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null
        
        // Si ya es una URL completa, retornarla tal cual
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl
        }
        
        // Construir URL completa usando la BASE_URL de ApiConfig
        val baseUrl = ApiConfig.BASE_URL.trimEnd('/')
        val path = imageUrl.trimStart('/')
        return "$baseUrl/$path"
    }
    
    suspend fun createPost(diagnosisId: Int, isAnonymous: Boolean = false): ApiResult<CommunityPostResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CommunityPostCreateRequest(diagnosisId, isAnonymous)
            val response = apiService.createCommunityPost(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al crear post: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error creating post", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Crear post con imagen directamente (CU-18)
     */
    suspend fun createPostWithImage(
        imageUri: Uri,
        description: String,
        plantName: String?,
        symptoms: String?,
        isAnonymous: Boolean,
        userId: Int,
        context: Context
    ): ApiResult<CommunityPostResponse> = withContext(Dispatchers.IO) {
        try {
            // Convertir Uri a File
            val imageFile = uriToFile(context, imageUri) 
                ?: return@withContext ApiResult.Error("Error al procesar imagen")
            
            // Crear multipart
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val plantNameBody = plantName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val symptomsBody = symptoms?.toRequestBody("text/plain".toMediaTypeOrNull())
            val isAnonymousBody = isAnonymous.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.createCommunityPostWithImage(
                image = imagePart,
                description = descriptionBody,
                plantName = plantNameBody,
                symptoms = symptomsBody,
                isAnonymous = isAnonymousBody,
                userId = userIdBody
            )
            
            if (response.isSuccessful && response.body() != null) {
                // Procesar la respuesta para agregar URL base a la imagen
                val postWithFullImageUrl = response.body()!!.copy(
                    imageUrl = buildFullImageUrl(response.body()!!.imageUrl)
                )
                ApiResult.Success(postWithFullImageUrl)
            } else {
                ApiResult.Error("Error al crear post: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error creating post with image", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            inputStream?.close()
            file
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error converting uri to file", e)
            null
        }
    }
    
    /**
     * Toggle like en un post (dar o quitar like)
     * Retorna LikeResponse con el estado actual del like
     */
    suspend fun toggleLikePost(postId: Int, userId: Int = 1): ApiResult<LikeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.toggleLikePost(postId, userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al dar/quitar like: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error toggling like", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Verificar si el usuario dio like a un post
     */
    suspend fun checkUserLikedPost(postId: Int, userId: Int): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkUserLikedPost(postId, userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.liked)
            } else {
                ApiResult.Error("Error al verificar like: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error checking like", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun getComments(postId: Int): ApiResult<List<CommentResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPostComments(postId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al obtener comentarios: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error getting comments", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun addComment(postId: Int, content: String, isSolution: Boolean = false, userId: Int = 1): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = CommentCreateRequest(content, isSolution)
            val response = apiService.addComment(postId, request, userId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Error al agregar comentario: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error adding comment", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
}
