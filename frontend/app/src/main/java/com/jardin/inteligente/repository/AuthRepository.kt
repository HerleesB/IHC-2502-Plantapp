package com.jardin.inteligente.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.jardin.inteligente.model.*
import com.jardin.inteligente.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    
    private val apiService = ApiService.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }
    
    suspend fun login(emailOrUsername: String, password: String): ApiResult<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(emailOrUsername, password)
            val response = apiService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                saveSession(tokenResponse)
                ApiResult.Success(tokenResponse)
            } else {
                ApiResult.Error("Credenciales incorrectas")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error in login", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String? = null
    ): ApiResult<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(username, email, password, fullName)
            val response = apiService.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                saveSession(tokenResponse)
                ApiResult.Success(tokenResponse)
            } else {
                ApiResult.Error("Error al registrar usuario")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error in register", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    suspend fun getCurrentUser(): ApiResult<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext ApiResult.Error("No hay sesión activa")
            val response = apiService.getCurrentUser("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                if (response.code() == 401) {
                    clearSession()
                }
                ApiResult.Error("Sesión expirada")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting current user", e)
            ApiResult.Error("Error de conexión: ${e.message}")
        }
    }
    
    private fun saveSession(tokenResponse: TokenResponse) {
        prefs.edit().apply {
            putString(KEY_TOKEN, tokenResponse.accessToken)
            putInt(KEY_USER_ID, tokenResponse.user.id)
            putString(KEY_USERNAME, tokenResponse.user.username)
            putString(KEY_EMAIL, tokenResponse.user.email)
            apply()
        }
    }
    
    fun clearSession() {
        prefs.edit().clear().apply()
    }
    
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, 0)
    
    fun isLoggedIn(): Boolean = getToken() != null
    
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
}
