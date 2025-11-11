package com.thellex.pay.data.viewModels.rates

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.thellex.pay.features.auth.repository.UserRepository
import com.thellex.pay.features.wallet.model.IRatesResponseDto
import com.thellex.pay.network.services.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RateViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository.getInstance(application)
    private val ratePreferences = RatePreferences(application)

    private var pollJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFmt = DateTimeFormatter.ISO_DATE_TIME

    // StateFlow holding both rates and expiresAt together
    private val _currentRates = MutableStateFlow(IRatesResponseDto(emptyList(), ""))
    val currentRates: StateFlow<IRatesResponseDto> = _currentRates.asStateFlow()

    // Fallback interval if expiresAt is invalid
    private val fallbackIntervalMs: Long = 60_000L

    init {
        // Restore cached rates and expiration
        val cachedRates = ratePreferences.loadRates()
        val cachedExpiresAt = ratePreferences.loadExpiresAt()
        if (cachedRates.isNotEmpty()) {
            _currentRates.value = IRatesResponseDto(cachedRates, cachedExpiresAt)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startPolling() {
        // If polling job is active, do nothing
        if (pollJob?.isActive == true) {
            return
        }

        pollJob = viewModelScope.launch(SupervisorJob()) {
            while (isActive) {
                try {
                    val authToken = userRepository.getToken().firstOrNull()
                    if (authToken.isNullOrBlank()) {
                        Log.w("RateViewModel", "No auth token, retrying in $fallbackIntervalMs ms")
                        delay(fallbackIntervalMs)
                        continue
                    }

                    val response = ApiClient.getAuthenticatedPaymentApi(application, authToken).getRates()
                    val result = response.result

                    if (result?.rates.isNullOrEmpty()) {
                        Log.w("RateViewModel", "Rates empty, retrying in $fallbackIntervalMs ms")
                        delay(fallbackIntervalMs)
                        continue
                    }

                    // Update StateFlow and cache
                    if (result != null) {
                        _currentRates.value = IRatesResponseDto(result.rates, result.expiresAt)
                    }
                    ratePreferences.saveRates(result!!.rates, result.expiresAt)

                    // Compute next delay based on expiresAt
                    val delayMs = try {
                        val expiresMs = ZonedDateTime.parse(result.expiresAt, dateFmt)
                            .toInstant().toEpochMilli()
                        (expiresMs - System.currentTimeMillis() - 5_000L).coerceAtLeast(5_000L)
                    } catch (e: Exception) {
                        Log.e("RateViewModel", "Invalid expiresAt: ${result.expiresAt}, using fallback", e)
                        fallbackIntervalMs
                    }

                    delay(delayMs)

                } catch (e: Exception) {
                    Log.e("RateViewModel", "Polling error: ${e.message}, retrying in $fallbackIntervalMs ms", e)
                    delay(fallbackIntervalMs)
                }
            }
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}

