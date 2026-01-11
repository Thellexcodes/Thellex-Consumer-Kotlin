package com.thellex.pay.features.wallet.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thellex.pay.features.wallet.model.WalletState
import com.thellex.pay.features.wallet.prefrences.WalletManagerPreferences
import com.thellex.pay.network.services.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletRepository private constructor(private  val context: Context) {

    private val _walletBalance = MutableLiveData<WalletState?>()
    val walletBalance: LiveData<WalletState?> = _walletBalance

    // Scoped Coroutine for internal launches
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun loadWalletData(
        preferences: WalletManagerPreferences,
        tokenProvider: suspend () -> String?,
    ) {

        val token = tokenProvider()

        if (token.isNullOrEmpty()) {
            Log.e(TAG, "Token is null or empty. Cannot fetch balance.")
            return
        }

        try {
            val api = ApiClient.getAuthenticatedWalletManagerApi(context, token)
            val response = api.fetchBalance()

            val result = response.result
            Log.d(TAG, "this is wallet response $result")
            if (result != null) {
                preferences.saveWalletBalance(result)
                _walletBalance.postValue(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load wallet balance", e)
        }
    }

    companion object {
        private const val TAG = ""

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: WalletRepository? = null

        fun getInstance(context:Context): WalletRepository =
            instance ?: synchronized(this) {
                instance ?: WalletRepository(context.applicationContext).also { instance = it }
            }
    }
}
