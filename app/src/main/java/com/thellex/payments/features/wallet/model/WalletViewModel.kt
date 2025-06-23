package com.thellex.payments.features.wallet.model

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.thellex.payments.features.wallet.prefrences.WalletManagerPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WalletManagerViewModel(application: Context): AndroidViewModel(application as Application) {

    @SuppressLint("StaticFieldLeak")
    private val preferences = WalletManagerPreferences(application)

    private val _walletBalance = MutableLiveData<WalletManagerBalanceResponse>()
    val walletBalance: LiveData<WalletManagerBalanceResponse> = _walletBalance

    fun setWalletBalanceResponse(response: WalletManagerBalanceResponse) {
        _walletBalance.value = response
    }

    fun fetchWalletBalanceIfNeeded() {
        preferences.getWalletBalance()?.let {
            _walletBalance.postValue(it)
            return
        }

        viewModelScope.launch {
            try {
                val response = fetchWalletBalanceFromServer()
                preferences.saveWalletBalance(response)
                _walletBalance.postValue(response)
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error fetching balance", e)
            }
        }
    }

    fun getWalletPreferences(): WalletManagerPreferences {
        return preferences
    }

    private suspend fun fetchWalletBalanceFromServer(): WalletManagerBalanceResponse {
        delay(1000)
        return WalletManagerBalanceResponse(
            totalBalance = "1500.00",
            currency = "USD",
            wallets = listOf(
                WalletEntry(
                    "0xABC...", "bep20",
                    1000.0,
                    14000.0,
                    assetCode = "usdt"
                ),
            )
        )
    }
}

