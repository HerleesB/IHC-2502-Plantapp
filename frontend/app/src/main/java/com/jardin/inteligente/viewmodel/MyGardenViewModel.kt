package com.jardin.inteligente.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import com.jardin.inteligente.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyGardenUiState(
    val plants: List<PlantResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: ProgressStatsResponse? = null
)

class MyGardenViewModel : ViewModel() {
    
    private val repository = PlantRepository(ApiService.getInstance())
    private val userId = 1 // TODO: Get from auth
    
    private val _uiState = MutableStateFlow(MyGardenUiState())
    val uiState: StateFlow<MyGardenUiState> = _uiState.asStateFlow()
    
    init {
        loadPlants()
        loadStats()
    }
    
    fun loadPlants() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = repository.getUserPlants(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        plants = result.data,
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
    
    fun loadStats() {
        viewModelScope.launch {
            when (val result = repository.getProgressStats(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(stats = result.data)
                }
                else -> {}
            }
        }
    }
    
    fun createPlant(name: String, species: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val request = PlantCreateRequest(
                name = name,
                userId = userId,
                species = species
            )
            
            when (repository.createPlant(request)) {
                is ApiResult.Success -> {
                    loadPlants() // Reload list
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al crear planta",
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }
    
    fun waterPlant(plantId: Int) {
        viewModelScope.launch {
            when (repository.waterPlant(plantId)) {
                is ApiResult.Success -> {
                    loadPlants() // Reload to get updated timestamp
                }
                else -> {}
            }
        }
    }
}
