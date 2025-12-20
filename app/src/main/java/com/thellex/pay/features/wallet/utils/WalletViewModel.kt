package com.thellex.pay.features.wallet.utils

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.thellex.pay.features.wallet.model.WalletBalanceDto
import com.thellex.pay.features.wallet.prefrences.WalletManagerPreferences
import kotlinx.coroutines.launch

class WalletManagerViewModel(application: Context): AndroidViewModel(application as Application) {

    private val repository = WalletRepository.getInstance(application)
    private val preferences = WalletManagerPreferences(application)

    val walletBalance: LiveData<WalletBalanceDto?> = repository.walletBalance

    fun loadWallet(tokenProvider: suspend () -> String?) {
        viewModelScope.launch {
            repository.loadWalletData(preferences,tokenProvider)
        }
    }

    fun getWalletPreferences(): WalletManagerPreferences {
        return preferences
    }
}