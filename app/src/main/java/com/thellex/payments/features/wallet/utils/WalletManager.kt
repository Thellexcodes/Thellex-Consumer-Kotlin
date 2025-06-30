package com.thellex.payments.features.wallet.utils

import com.thellex.payments.features.wallet.model.WalletBalanceDto
import com.thellex.payments.features.wallet.prefrences.WalletManagerPreferences
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WalletManager {

    private suspend fun fetchWalletData(
        token: String,
        walletPreferences: WalletManagerPreferences,
        onSuccess: (WalletBalanceDto) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val api = ApiClient.getAuthenticatedWalletManagerApi(token)
            val response = api.fetchBalance()
            val walletBalanceResponse = response.result

            if (walletBalanceResponse != null && walletBalanceResponse.wallets.isNotEmpty()) {
                walletPreferences.saveWalletBalance(walletBalanceResponse)
                withContext(Dispatchers.Main) {
                    onSuccess(walletBalanceResponse)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError(Exception("Empty wallet data"))
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e)
            }
        }
    }

    suspend fun loadWalletData(
        walletManagerPreferences: WalletManagerPreferences,
        tokenProvider: suspend () -> String?,
        onResult: (WalletBalanceDto) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        walletManagerPreferences.getWalletBalance()?.let { cached ->
            withContext(Dispatchers.Main) { onResult(cached) }
            return
        }
       tokenProvider()?.let{
            fetchWalletData(it, walletManagerPreferences, onResult, onError)
       }
    }
}

