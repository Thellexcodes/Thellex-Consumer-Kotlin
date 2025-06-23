package com.thellex.payments.features.wallet.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thellex.payments.features.wallet.model.WalletBalanceDto
import com.thellex.payments.features.wallet.prefrences.WalletManagerPreferences

class WalletRepository private constructor() {

    private val _walletBalance = MutableLiveData<WalletBalanceDto>()
    val walletBalance: LiveData<WalletBalanceDto> = _walletBalance

    private var isLoaded = false

    suspend fun loadWalletData(
        preferences: WalletManagerPreferences,
        tokenProvider: suspend () -> String?
    ) {
        if (isLoaded) return

        WalletManager.loadWalletData(
            walletManagerPreferences = preferences,
            tokenProvider = tokenProvider,
            onResult = { walletDto ->
                _walletBalance.postValue(walletDto)
                isLoaded = true
            },
            onError = { }
        )
    }

    companion object {
        @Volatile private var instance: WalletRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: WalletRepository().also { instance = it }
            }
    }
}
