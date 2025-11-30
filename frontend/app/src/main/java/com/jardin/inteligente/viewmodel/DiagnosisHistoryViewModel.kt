package com.jardin.inteligente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import com.jardin.inteligente.repository.AuthRepository
import com.jardin.inteligente.repository.DiagnosisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiagnosisHistoryUiState(
    val diagnoses: List<DiagnosisHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para historial de diagnósticos (CU-08)
 */
class DiagnosisHistoryViewModel(private val context: Context) : ViewModel() {
    
    private val diagnosisRepository = DiagnosisRepository(context)
    private val authRepository = AuthRepository(context)
    
    private val _uiState = MutableStateFlow(DiagnosisHistoryUiState())
    val uiState: StateFlow<DiagnosisHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistory()
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = diagnosisRepository.getDiagnosisHistory()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        diagnoses = result.data.diagnoses,
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
}

class DiagnosisHistoryViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiagnosisHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiagnosisHistoryViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
