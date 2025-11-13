package com.jardin.inteligente.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.ApiResult
import com.jardin.inteligente.model.CaptureGuidanceResponse
import com.jardin.inteligente.repository.DiagnosisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados posibles de la validación de foto
 */
sealed class ValidationState {
    object Idle : ValidationState()
    object Loading : ValidationState()
    data class Success(val response: CaptureGuidanceResponse) : ValidationState()
    data class Error(val message: String) : ValidationState()
}

/**
 * ViewModel para manejo de captura y validación de fotos
 */
class CaptureViewModel(context: Context) : ViewModel() {
    
    private val repository = DiagnosisRepository(context)
    
    // Estado de validación
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Idle)
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()
    
    // URI de la imagen capturada
    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri.asStateFlow()
    
    /**
     * Guardar URI de imagen capturada
     */
    fun setCapturedImage(uri: Uri) {
        _capturedImageUri.value = uri
    }
    
    /**
     * Validar foto capturada con IA
     */
    fun validatePhoto(imageUri: Uri) {
        viewModelScope.launch {
            _validationState.value = ValidationState.Loading
            
            when (val result = repository.validateCapturedPhoto(imageUri)) {
                is ApiResult.Success -> {
                    _validationState.value = ValidationState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _validationState.value = ValidationState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Ya está en Loading
                }
            }
        }
    }
    
    /**
     * Resetear validación para nueva captura
     */
    fun resetValidation() {
        _validationState.value = ValidationState.Idle
        _capturedImageUri.value = null
    }
    
    /**
     * Reintentar validación con la imagen actual
     */
    fun retryValidation() {
        _capturedImageUri.value?.let { uri ->
            validatePhoto(uri)
        }
    }
}

/**
 * Factory para crear CaptureViewModel con Context
 */
class CaptureViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaptureViewModel::class.java)) {
            return CaptureViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
