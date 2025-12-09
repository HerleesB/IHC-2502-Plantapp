package com.jardin.inteligente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import com.jardin.inteligente.repository.CommunityRepository
import com.jardin.inteligente.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CommunityUiState(
    val posts: List<CommunityPostResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // Estados para comentarios
    val comments: Map<Int, List<CommentResponse>> = emptyMap(),
    val isLoadingComments: Boolean = false,
    val isAddingComment: Boolean = false,
    val commentError: String? = null,
    val commentSuccess: Boolean = false,
    // Estados para likes - Map de postId a si el usuario actual dio like
    val userLikes: Map<Int, Boolean> = emptyMap()
)

/**
 * ViewModel para la Comunidad (CU-07, CU-09, CU-19)
 */
class CommunityViewModel(private val context: Context) : ViewModel() {
    
    private val repository = CommunityRepository(ApiService.getInstance())
    private val authRepository = AuthRepository(context)
    
    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()
    
    init {
        loadPosts()
    }
    
    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = repository.getPosts()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        posts = result.data,
                        isLoading = false
                    )
                    // Verificar likes del usuario para cada post
                    checkUserLikesForPosts(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Verificar si el usuario dio like a cada post
     */
    private fun checkUserLikesForPosts(posts: List<CommunityPostResponse>) {
        val userId = authRepository.getUserId().takeIf { it > 0 } ?: return
        
        viewModelScope.launch {
            val likesMap = mutableMapOf<Int, Boolean>()
            
            posts.forEach { post ->
                when (val result = repository.checkUserLikedPost(post.id, userId)) {
                    is ApiResult.Success -> {
                        likesMap[post.id] = result.data
                    }
                    else -> {
                        likesMap[post.id] = false
                    }
                }
            }
            
            _uiState.value = _uiState.value.copy(userLikes = likesMap)
        }
    }
    
    /**
     * Toggle like en un post (dar o quitar)
     */
    fun toggleLikePost(postId: Int) {
        viewModelScope.launch {
            val userId = authRepository.getUserId().takeIf { it > 0 } ?: 1
            
            when (val result = repository.toggleLikePost(postId, userId)) {
                is ApiResult.Success -> {
                    val likeResponse = result.data
                    
                    // Actualizar el estado de likes del usuario
                    val updatedUserLikes = _uiState.value.userLikes.toMutableMap()
                    updatedUserLikes[postId] = likeResponse.liked
                    
                    // Actualizar el contador de likes en el post
                    val updatedPosts = _uiState.value.posts.map { post ->
                        if (post.id == postId) {
                            post.copy(likes = likeResponse.totalLikes)
                        } else post
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        posts = updatedPosts,
                        userLikes = updatedUserLikes
                    )
                }
                is ApiResult.Error -> {
                    // Opcional: mostrar error
                }
                else -> {}
            }
        }
    }
    
    /**
     * Verificar si el usuario actual dio like a un post específico
     */
    fun hasUserLikedPost(postId: Int): Boolean {
        return _uiState.value.userLikes[postId] ?: false
    }
    
    fun createPost(diagnosisId: Int, isAnonymous: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (repository.createPost(diagnosisId, isAnonymous)) {
                is ApiResult.Success -> {
                    loadPosts() // Reload
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al crear post",
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Cargar comentarios de un post específico
     */
    fun loadComments(postId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingComments = true, commentError = null)
            
            when (val result = repository.getComments(postId)) {
                is ApiResult.Success -> {
                    val updatedComments = _uiState.value.comments.toMutableMap()
                    updatedComments[postId] = result.data
                    _uiState.value = _uiState.value.copy(
                        comments = updatedComments,
                        isLoadingComments = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        commentError = result.message,
                        isLoadingComments = false
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Agregar comentario a un post
     */
    fun addComment(postId: Int, content: String, isSolution: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAddingComment = true, 
                commentError = null,
                commentSuccess = false
            )
            
            val userId = authRepository.getUserId().takeIf { it > 0 } ?: 1
            
            when (val result = repository.addComment(postId, content, isSolution, userId)) {
                is ApiResult.Success -> {
                    // Recargar comentarios para obtener el nuevo
                    loadComments(postId)
                    
                    // Actualizar contador de comentarios en el post
                    val updatedPosts = _uiState.value.posts.map { post ->
                        if (post.id == postId) {
                            post.copy(commentsCount = post.commentsCount + 1)
                        } else post
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        posts = updatedPosts,
                        isAddingComment = false,
                        commentSuccess = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        commentError = result.message,
                        isAddingComment = false
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Obtener los últimos N comentarios de un post (para el modal)
     */
    fun getLatestComments(postId: Int, limit: Int = 2): List<CommentResponse> {
        return _uiState.value.comments[postId]?.take(limit) ?: emptyList()
    }
    
    /**
     * Limpiar estado de éxito de comentario
     */
    fun clearCommentSuccess() {
        _uiState.value = _uiState.value.copy(commentSuccess = false)
    }
    
    /**
     * Limpiar error de comentario
     */
    fun clearCommentError() {
        _uiState.value = _uiState.value.copy(commentError = null)
    }
}

class CommunityViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
