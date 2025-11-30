package com.jardin.inteligente.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiagnosisHistoryUiState(
    val diagnoses: List<DiagnosisHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DiagnosisHistoryViewModel : ViewModel() {
    
    private val apiService = ApiService.getInstance()
    private val userId = 1 // TODO: Get from auth
    
    private val _uiState = MutableStateFlow(DiagnosisHistoryUiState())
    val uiState: StateFlow<DiagnosisHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistory()
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val response = apiService.getDiagnosisHistory(userId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        diagnoses = response.body()!!.diagnoses,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al cargar historial",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error de conexión: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}
