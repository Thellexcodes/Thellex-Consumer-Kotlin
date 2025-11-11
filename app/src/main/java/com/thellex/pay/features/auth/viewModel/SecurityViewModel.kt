package com.thellex.pay.features.auth.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thellex.pay.data.model.SecurityUiState
import com.thellex.pay.features.auth.repository.SecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class SecurityViewModel(
    private val repository: SecurityRepository,
) : ViewModel() {

    private val _setupSkipped = MutableStateFlow(false)
    val setupSkipped: StateFlow<Boolean> = _setupSkipped

    suspend fun updateSecurityPin(pin: String, token: String): Boolean {
        return try {
            val response = repository.updateRemoteSecurityPin(pin, token)
            response.result ?: false
        } catch (e: Exception) {
            Log.e("SecurityViewModel", "Failed to update security status", e)
            false
        } finally {
        }
    }

    fun verifyUserPin(enteredPin: String, authToken: String,callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.fetchRemoteSecurityStatus(enteredPin, authToken)
                callback(response.result!!)
            } catch (e: Exception) {
                callback(false)
            } finally {
            }
        }
    }

    companion object {
        private val SecurityViewModel = "SecurityViewModel"
    }
}

class SecurityViewModelFactory(
    private val repository: SecurityRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecurityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SecurityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}