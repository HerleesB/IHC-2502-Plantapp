package com.jardin.inteligente.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.ApiResult
import com.jardin.inteligente.repository.CommunityRepository
import com.jardin.inteligente.repository.AuthRepository
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ShareState {
    object Idle : ShareState()
    object Loading : ShareState()
    data class Success(val postId: Int) : ShareState()
    data class Error(val message: String) : ShareState()
}

/**
 * ViewModel para compartir casos en la comunidad (CU-07, CU-18)
 */
class CommunityShareViewModel(
    private val context: Context
) : ViewModel() {
    
    private val communityRepository = CommunityRepository(ApiService.getInstance())
    private val authRepository = AuthRepository(context)
    
    private val _shareState = MutableStateFlow<ShareState>(ShareState.Idle)
    val shareState: StateFlow<ShareState> = _shareState.asStateFlow()
    
    fun sharePost(
        imageUri: Uri,
        description: String,
        plantName: String? = null,
        symptoms: String? = null,
        isAnonymous: Boolean = false
    ) {
        viewModelScope.launch {
            _shareState.value = ShareState.Loading
            
            try {
                val userId = authRepository.getUserId()
                if (userId == 0) {
                    _shareState.value = ShareState.Error("Debes iniciar sesión para publicar")
                    return@launch
                }
                
                // Por ahora, crear post sin diagnóstico previo
                // TODO: Implementar endpoint que acepte imagen directamente
                when (val result = communityRepository.createPostWithImage(
                    imageUri = imageUri,
                    description = description,
                    plantName = plantName,
                    symptoms = symptoms,
                    isAnonymous = isAnonymous,
                    userId = userId,
                    context = context
                )) {
                    is ApiResult.Success -> {
                        _shareState.value = ShareState.Success(result.data.id)
                    }
                    is ApiResult.Error -> {
                        _shareState.value = ShareState.Error(result.message)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _shareState.value = ShareState.Error("Error al publicar: ${e.message}")
            }
        }
    }
    
    fun resetState() {
        _shareState.value = ShareState.Idle
    }
}

class CommunityShareViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityShareViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityShareViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
