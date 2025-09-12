package com.thellex.payments.features.wallet.utils

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thellex.payments.features.wallet.model.WalletBalanceDto
import com.thellex.payments.features.wallet.prefrences.WalletManagerPreferences
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletRepository private constructor(private  val context: Context) {

    private val _walletBalance = MutableLiveData<WalletBalanceDto?>()
    val walletBalance: LiveData<WalletBalanceDto?> = _walletBalance

    private val _secondsLeft = MutableLiveData<Long>()
    val secondsLeft: LiveData<Long> = _secondsLeft

    private var countDownTimer: CountDownTimer? = null
    private var isLoaded = false

    private var currentPreferences: WalletManagerPreferences? = null
    private var currentTokenProvider: (suspend () -> String?)? = null

    // Scoped Coroutine for internal launches
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun loadWalletData(
        preferences: WalletManagerPreferences,
        tokenProvider: suspend () -> String?,
        loadNow: Boolean? = false,
        action: String? = ""
    ) {
        currentPreferences = preferences
        currentTokenProvider = tokenProvider

        val cacheValidDuration = 1 * 60 * 1000L

        if (loadNow != true) {
            val currentTime = System.currentTimeMillis()
            val lastSaved = preferences.getWalletBalanceSaveTime()
            val timeSinceLastSave = currentTime - lastSaved
            val isExpired = timeSinceLastSave > cacheValidDuration

            val cachedBalance = preferences.getWalletBalance()

            if (!isExpired) {
                _walletBalance.postValue(cachedBalance)
                withContext(Dispatchers.Main) {
                    startCountdown(cacheValidDuration - timeSinceLastSave)
                }
                return
            }
        }

        val token = tokenProvider()

        if (token.isNullOrEmpty()) {
            Log.e(TAG, "Token is null or empty. Cannot fetch balance.")
            return
        }

        try {
            val api = ApiClient.getAuthenticatedWalletManagerApi(token)
            val response = api.fetchBalance(action)

            val result = response.result
            if (result != null) {
                preferences.saveWalletBalance(result)
                _walletBalance.postValue(result)
                isLoaded = true

                withContext(Dispatchers.Main) {
                    startCountdown(cacheValidDuration)
                }
            } else {
                Log.e(TAG, "API response result is null.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load wallet balance", e)
        }
    }

    private fun startCountdown(durationInMillis: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(durationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                _secondsLeft.postValue(seconds)
//                Log.d(TAG, "Time left: $seconds seconds")
            }

            override fun onFinish() {
                isLoaded = false

                val prefs = currentPreferences
                val tokenFn = currentTokenProvider

                if (prefs != null && tokenFn != null) {
                    coroutineScope.launch {
                        loadWalletData(prefs, tokenFn)
                    }
                }
            }
        }.start()
    }

    fun cancelCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    companion object {
        private const val TAG = ""

        @Volatile
        private var instance: WalletRepository? = null

        fun getInstance(context:Context): WalletRepository =
            instance ?: synchronized(this) {
                instance ?: WalletRepository(context.applicationContext).also { instance = it }
            }
    }
}
