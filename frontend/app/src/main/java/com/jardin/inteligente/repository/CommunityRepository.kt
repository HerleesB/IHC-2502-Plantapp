package com.jardin.inteligente.repository

import android.util.Log
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityRepository(private val apiService: ApiService) {
    
    suspend fun getPosts(limit: Int = 20): ApiResult<List<CommunityPostResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCommunityPosts(limit)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al obtener posts: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error getting posts", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
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
    
    suspend fun likePost(postId: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.likePost(postId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Error al dar like: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error liking post", e)
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
    
    suspend fun addComment(postId: Int, content: String, isSolution: Boolean = false): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = CommentCreateRequest(content, isSolution)
            val response = apiService.addComment(postId, request)
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
