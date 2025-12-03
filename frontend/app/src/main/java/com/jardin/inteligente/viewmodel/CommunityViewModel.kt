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
    val error: String? = null
)

/**
 * ViewModel para la Comunidad (CU-07, CU-19)
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
    
    fun likePost(postId: Int) {
        viewModelScope.launch {
            when (repository.likePost(postId)) {
                is ApiResult.Success -> {
                    // Update local state
                    val updatedPosts = _uiState.value.posts.map { post ->
                        if (post.id == postId) {
                            post.copy(likes = post.likes + 1)
                        } else post
                    }
                    _uiState.value = _uiState.value.copy(posts = updatedPosts)
                }
                else -> {}
            }
        }
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
