package com.jardin.inteligente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.*
import com.jardin.inteligente.repository.PlantRepository
import com.jardin.inteligente.repository.AuthRepository
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

/**
 * ViewModel para Mi Jardín (CU-04, CU-16)
 */
class MyGardenViewModel(private val context: Context) : ViewModel() {
    
    private val plantRepository = PlantRepository(context)
    private val authRepository = AuthRepository(context)
    
    private val _uiState = MutableStateFlow(MyGardenUiState())
    val uiState: StateFlow<MyGardenUiState> = _uiState.asStateFlow()
    
    init {
        loadPlants()
        loadStats()
    }
    
    fun loadPlants() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val userId = authRepository.getUserId().takeIf { it > 0 } ?: 1
            
            when (val result = plantRepository.getUserPlants(userId)) {
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
            val userId = authRepository.getUserId().takeIf { it > 0 } ?: 1
            
            when (val result = plantRepository.getProgressStats(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(stats = result.data)
                }
                else -> {}
            }
        }
    }
    
    fun createPlant(name: String, species: String? = null, location: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val userId = authRepository.getUserId().takeIf { it > 0 } ?: 1
            
            val request = PlantCreateRequest(
                name = name,
                userId = userId,
                species = species,
                location = location
            )
            
            when (plantRepository.createPlant(request)) {
                is ApiResult.Success -> {
                    loadPlants() // Reload list
                    loadStats()  // Reload stats
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
            when (plantRepository.waterPlant(plantId)) {
                is ApiResult.Success -> {
                    loadPlants() // Reload to get updated timestamp
                }
                else -> {}
            }
        }
    }
    
    fun deletePlant(plantId: Int) {
        viewModelScope.launch {
            when (plantRepository.deletePlant(plantId)) {
                is ApiResult.Success -> {
                    loadPlants()
                    loadStats()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al eliminar planta"
                    )
                }
                else -> {}
            }
        }
    }
}

class MyGardenViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyGardenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyGardenViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
