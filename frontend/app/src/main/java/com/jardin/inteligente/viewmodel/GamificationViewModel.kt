package com.jardin.inteligente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import com.jardin.inteligente.repository.GamificationRepository
import com.jardin.inteligente.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GamificationUiState(
    val achievements: AchievementsResponse? = null,
    val missions: MissionsResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para Gamificación (CU-06, CU-17)
 */
class GamificationViewModel(private val context: Context) : ViewModel() {
    
    private val repository = GamificationRepository(ApiService.getInstance())
    private val authRepository = AuthRepository(context)
    
    private val _uiState = MutableStateFlow(GamificationUiState())
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val userId = authRepository.getUserId().takeIf { it > 0 } ?: 1
            
            // Load achievements
            when (val achievementsResult = repository.getAchievements(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(achievements = achievementsResult.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = achievementsResult.message)
                }
                else -> {}
            }
            
            // Load missions
            when (val missionsResult = repository.getMissions(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        missions = missionsResult.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = missionsResult.message,
                        isLoading = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
}

class GamificationViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GamificationViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
