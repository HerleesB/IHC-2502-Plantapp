package com.jardin.inteligente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.*
import com.jardin.inteligente.repository.PlantRepository
import com.jardin.inteligente.repository.DiagnosisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de UI para detalle de planta (CU-08)
 */
data class PlantDetailUiState(
    val plant: PlantResponse? = null,
    val diagnoses: List<DiagnosisHistoryItem> = emptyList(),
    val progressSummary: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la pantalla de detalle de planta
 * Implementa CU-08: Inventario y progreso de plantas
 */
class PlantDetailViewModel(
    private val context: Context,
    private val plantId: Int
) : ViewModel() {
    
    private val plantRepository = PlantRepository(context)
    private val diagnosisRepository = DiagnosisRepository(context)
    
    private val _uiState = MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadPlantData()
    }
    
    private fun loadPlantData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Cargar datos de la planta
                when (val plantResult = plantRepository.getPlantById(plantId)) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(plant = plantResult.data)
                        
                        // Cargar historial de diagnósticos
                        loadDiagnosisHistory()
                        
                        // Generar resumen de progreso
                        generateProgressSummary(plantResult.data)
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = plantResult.message,
                            isLoading = false
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar datos: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    private suspend fun loadDiagnosisHistory() {
        when (val result = diagnosisRepository.getDiagnosisHistoryByPlant(plantId)) {
            is ApiResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    diagnoses = result.data,
                    isLoading = false
                )
            }
            is ApiResult.Error -> {
                // No mostrar error si solo falla el historial
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
            else -> {}
        }
    }
    
    private fun generateProgressSummary(plant: PlantResponse) {
        // Generar un resumen simple basado en los datos disponibles
        val diagnoses = _uiState.value.diagnoses
        
        val summary = buildString {
            append("Tu ${plant.name} ")
            
            when {
                plant.healthScore >= 80 -> append("está en excelente estado. ")
                plant.healthScore >= 60 -> append("está saludable pero podría mejorar. ")
                plant.healthScore >= 40 -> append("necesita atención. ")
                else -> append("requiere cuidados urgentes. ")
            }
            
            if (diagnoses.isNotEmpty()) {
                append("Se han realizado ${diagnoses.size} diagnóstico(s). ")
                
                val lastDiagnosis = diagnoses.firstOrNull()
                lastDiagnosis?.let {
                    append("El último diagnóstico detectó: ${it.diseaseName ?: "estado general"}.")
                }
            } else {
                append("Aún no se han realizado diagnósticos.")
            }
        }
        
        _uiState.value = _uiState.value.copy(progressSummary = summary)
    }
    
    fun waterPlant() {
        viewModelScope.launch {
            when (plantRepository.waterPlant(plantId)) {
                is ApiResult.Success -> {
                    // Recargar datos
                    loadPlantData()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al registrar riego"
                    )
                }
                else -> {}
            }
        }
    }
    
    fun refreshData() {
        loadPlantData()
    }
}

class PlantDetailViewModelFactory(
    private val context: Context,
    private val plantId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailViewModel(context, plantId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
