package com.jardin.inteligente.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.ApiResult
import com.jardin.inteligente.model.CaptureGuidanceResponse
import com.jardin.inteligente.model.DiagnosisResponse
import com.jardin.inteligente.repository.DiagnosisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ValidationState {
    object Idle : ValidationState()
    object Loading : ValidationState()
    data class Success(val response: CaptureGuidanceResponse) : ValidationState()
    data class Error(val message: String) : ValidationState()
}

sealed class DiagnosisState {
    object Idle : DiagnosisState()
    object Loading : DiagnosisState()
    data class Success(val response: DiagnosisResponse) : DiagnosisState()
    data class Error(val message: String) : DiagnosisState()
}

/**
 * ViewModel para captura y diagnóstico (CU-01, CU-02, CU-14, CU-20)
 */
class CaptureViewModel(context: Context) : ViewModel() {
    
    private val repository = DiagnosisRepository(context)
    
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Idle)
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()
    
    private val _diagnosisState = MutableStateFlow<DiagnosisState>(DiagnosisState.Idle)
    val diagnosisState: StateFlow<DiagnosisState> = _diagnosisState.asStateFlow()
    
    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri.asStateFlow()
    
    fun setCapturedImage(uri: Uri) {
        _capturedImageUri.value = uri
        // Reset states when new image is captured
        _validationState.value = ValidationState.Idle
        _diagnosisState.value = DiagnosisState.Idle
    }
    
    fun validatePhoto(imageUri: Uri) {
        viewModelScope.launch {
            _validationState.value = ValidationState.Loading
            
            when (val result = repository.validatePhoto(imageUri)) {
                is ApiResult.Success -> {
                    _validationState.value = ValidationState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _validationState.value = ValidationState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun analyzePlant(imageUri: Uri, plantId: Int, symptoms: String? = null) {
        viewModelScope.launch {
            _diagnosisState.value = DiagnosisState.Loading
            
            when (val result = repository.analyzePlant(imageUri, plantId, symptoms)) {
                is ApiResult.Success -> {
                    _diagnosisState.value = DiagnosisState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _diagnosisState.value = DiagnosisState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun resetValidation() {
        _validationState.value = ValidationState.Idle
    }
    
    fun resetDiagnosis() {
        _diagnosisState.value = DiagnosisState.Idle
    }
    
    fun resetStates() {
        _validationState.value = ValidationState.Idle
        _diagnosisState.value = DiagnosisState.Idle
        _capturedImageUri.value = null
    }
}
