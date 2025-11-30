package com.jardin.inteligente.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import com.jardin.inteligente.repository.GamificationRepository
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

class GamificationViewModel : ViewModel() {
    
    private val repository = GamificationRepository(ApiService.getInstance())
    private val userId = 1 // TODO: Get from auth
    
    private val _uiState = MutableStateFlow(GamificationUiState())
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
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
