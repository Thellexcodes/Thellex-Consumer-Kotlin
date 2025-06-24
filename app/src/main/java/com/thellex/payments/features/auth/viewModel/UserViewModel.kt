package com.thellex.payments.features.auth.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.thellex.payments.data.model.UserEntity
import kotlinx.coroutines.launch

class UserViewModel(application: Context) : AndroidViewModel(application as Application) {

    private val repository = UserRepository.getInstance(application)

    val token: LiveData<String?> = repository.getToken().asLiveData()
    val authResult: LiveData<UserEntity?> = repository.getAuthResult().asLiveData()

    fun saveToken(token: String?) {
        viewModelScope.launch {
            repository.saveToken(token)
        }
    }

    fun saveAuthResult(result: UserEntity?) {
        viewModelScope.launch {
            repository.saveAuthResult(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
