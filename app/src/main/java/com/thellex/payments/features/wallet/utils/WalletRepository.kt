package com.thellex.payments.features.wallet.utils

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

class WalletRepository private constructor() {

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
        tokenProvider: suspend () -> String?
    ) {
        currentPreferences = preferences
        currentTokenProvider = tokenProvider

        val currentTime = System.currentTimeMillis()
        val lastSaved = preferences.getWalletBalanceSaveTime()
        val cacheValidDuration = 1 * 60 * 1000L
        val timeSinceLastSave = currentTime - lastSaved
        val isExpired = timeSinceLastSave > cacheValidDuration

        val cachedBalance = preferences.getWalletBalance()
        if (!isExpired) {
            _walletBalance.postValue(cachedBalance)
            startCountdown(cacheValidDuration - timeSinceLastSave)
            return
        }

        Log.d(TAG, "Making a new request")

        val token = tokenProvider()

        try {
            val api = ApiClient.getAuthenticatedWalletManagerApi(token!!)
            val response = api.fetchBalance()

            preferences.saveWalletBalance(response.result!!)
            _walletBalance.postValue(response.result)
            isLoaded = true

            startCountdown(cacheValidDuration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCountdown(durationInMillis: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(durationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                _secondsLeft.postValue(seconds)
                Log.d(TAG, "Time left: $seconds seconds")
            }

            override fun onFinish() {
                Log.d(TAG, "Cache expired.")
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
        private const val TAG = "TAG"

        @Volatile
        private var instance: WalletRepository? = null

        fun getInstance(): WalletRepository =
            instance ?: synchronized(this) {
                instance ?: WalletRepository().also { instance = it }
            }
    }
}
