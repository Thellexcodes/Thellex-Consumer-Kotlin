package com.thellex.payments.features.auth.viewModel

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.thellex.payments.data.model.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.format.DateTimeParseException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class UserViewModel(application: Context) : AndroidViewModel(application as Application) {

    private val repository = UserRepository.getInstance(application)

    private val prefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    val token: LiveData<String?> = repository.getToken().asLiveData()

    private val _authResult = MutableLiveData<UserEntity?>()
    val authResult: LiveData<UserEntity?> = _authResult

    init {
        viewModelScope.launch {
            repository.getAuthResult().collect { user ->
                _authResult.postValue(user)
            }
        }
    }

    fun saveToken(token: String?) {
        viewModelScope.launch {
            repository.saveToken(token)
        }
    }

    fun saveAuthResult(result: UserEntity?) {
        viewModelScope.launch {
            try {
                repository.saveAuthResult(result)
                val latestUser = repository.getAuthResult().first()
                _authResult.postValue(latestUser)
            } catch (e: Exception) {
                Log.e("TAG", "Failed to save auth result", e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authResult.postValue(null)
        }
    }

    fun getToken(): String? {
        return token.value
    }

    fun saveExpiresAt(timeString: String) {
        prefs.edit().putString("expiresAt", timeString).apply()
    }

    @OptIn(ExperimentalTime::class)
    fun getExpiresAt(): Instant? {
        val timeString = prefs.getString("expiresAt", null) ?: return null
        return try {
            Instant.parse(timeString)
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(ExperimentalTime::class)
    fun isVerificationCodeValid(): Boolean {
        val expiresAtInstant = getExpiresAt() ?: return false
        val nowInstant = Clock.System.now()
        return expiresAtInstant > nowInstant
    }
}

