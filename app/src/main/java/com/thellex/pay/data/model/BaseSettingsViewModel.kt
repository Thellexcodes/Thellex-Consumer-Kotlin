package com.thellex.pay.data.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thellex.pay.data.datastore.getBaseSettingsCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BaseSettingsViewModel(
    private val context: Context
) : ViewModel() {

    private val _baseSettings =
        MutableStateFlow<BaseSettingsCache?>(null)
    val baseSettings: StateFlow<BaseSettingsCache?> = _baseSettings

    init {
        loadFromCache()
    }

    private fun loadFromCache() {
        viewModelScope.launch {
            _baseSettings.value = context.getBaseSettingsCache()
        }
    }
}

class BaseSettingsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BaseSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BaseSettingsViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
