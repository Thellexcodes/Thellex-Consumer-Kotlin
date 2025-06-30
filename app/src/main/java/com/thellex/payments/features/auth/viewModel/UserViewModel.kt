package com.thellex.payments.features.auth.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.thellex.payments.data.model.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserViewModel(application: Context) : AndroidViewModel(application as Application) {

    private val repository = UserRepository.getInstance(application)

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
}
