package com.jardin.inteligente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jardin.inteligente.model.*
import com.jardin.inteligente.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserResponse) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(context: Context) : ViewModel() {
    
    private val repository = AuthRepository(context)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkAuthentication()
    }
    
    private fun checkAuthentication() {
        viewModelScope.launch {
            if (repository.isLoggedIn()) {
                when (val result = repository.getCurrentUser()) {
                    is ApiResult.Success -> {
                        _authState.value = AuthState.Authenticated(result.data)
                    }
                    else -> {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    fun login(emailOrUsername: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            when (val result = repository.login(emailOrUsername, password)) {
                is ApiResult.Success -> {
                    _authState.value = AuthState.Authenticated(result.data.user)
                }
                is ApiResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun register(username: String, email: String, password: String, fullName: String? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            when (val result = repository.register(username, email, password, fullName)) {
                is ApiResult.Success -> {
                    _authState.value = AuthState.Authenticated(result.data.user)
                }
                is ApiResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun logout() {
        repository.clearSession()
        _authState.value = AuthState.Unauthenticated
    }
    
    fun getCurrentUserId(): Int = repository.getUserId()
    
    fun isLoggedIn(): Boolean = repository.isLoggedIn()
}

// Factory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
