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

sealed class AddPlantState {
    object Idle : AddPlantState()
    object Loading : AddPlantState()
    data class Success(val plant: PlantResponse) : AddPlantState()
    data class Error(val message: String) : AddPlantState()
}

/**
 * ViewModel para agregar planta desde diagnóstico (CU-20)
 */
class AddPlantViewModel(
    private val context: Context
) : ViewModel() {
    
    private val plantRepository = PlantRepository(context)
    private val authRepository = AuthRepository(context)
    
    private val _addPlantState = MutableStateFlow<AddPlantState>(AddPlantState.Idle)
    val addPlantState: StateFlow<AddPlantState> = _addPlantState.asStateFlow()
    
    fun addPlant(
        name: String,
        species: String? = null,
        location: String? = null,
        diagnosisId: Int? = null
    ) {
        viewModelScope.launch {
            _addPlantState.value = AddPlantState.Loading
            
            try {
                val userId = authRepository.getUserId()
                if (userId == 0) {
                    _addPlantState.value = AddPlantState.Error("Debes iniciar sesión para agregar plantas")
                    return@launch
                }
                
                when (val result = plantRepository.createPlant(
                    name = name,
                    userId = userId,
                    species = species,
                    location = location,
                    diagnosisId = diagnosisId
                )) {
                    is ApiResult.Success -> {
                        _addPlantState.value = AddPlantState.Success(result.data)
                    }
                    is ApiResult.Error -> {
                        _addPlantState.value = AddPlantState.Error(result.message)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _addPlantState.value = AddPlantState.Error("Error al agregar planta: ${e.message}")
            }
        }
    }
    
    fun resetState() {
        _addPlantState.value = AddPlantState.Idle
    }
}

class AddPlantViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddPlantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddPlantViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
