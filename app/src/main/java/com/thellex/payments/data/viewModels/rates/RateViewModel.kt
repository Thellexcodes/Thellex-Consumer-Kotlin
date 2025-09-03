package com.thellex.payments.data.viewModels.rates

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thellex.payments.features.auth.viewModel.UserRepository
import com.thellex.payments.features.wallet.model.IRatesDto
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.Job
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

    private val _rates = MutableStateFlow<List<IRatesDto>>(emptyList())
    val rates: StateFlow<List<IRatesDto>> = _rates.asStateFlow()

    private var pollJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFmt = DateTimeFormatter.ISO_DATE_TIME

    init {
        // Restore cached rates on initialization
        val cached = ratePreferences.loadRates()
        if (cached.isNotEmpty()) {
            _rates.value = cached
            Log.d("RateViewModel", "Restored cached rates: $cached")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startPolling(intervalFallbackMs: Long = 60_000L) {
        if (pollJob != null) {
            Log.d("RateViewModel", "Polling already running")
            return
        }

        pollJob = viewModelScope.launch {
            Log.d("RateViewModel", "Starting rate polling")
            while (isActive) {
                val authToken = userRepository.getToken().firstOrNull()
                if (authToken.isNullOrBlank()) {
                    Log.w("RateViewModel", "No auth token, retrying in ${intervalFallbackMs}ms")
                    delay(intervalFallbackMs)
                    continue
                }

                try {
                    val response = ApiClient.getAuthenticatedPaymentApi(authToken).getRates()
                    val result = response.result

                    if (result?.rates.isNullOrEmpty()) {
                        Log.w("RateViewModel", "Rates empty, retrying in ${intervalFallbackMs}ms")
                        delay(intervalFallbackMs)
                        continue
                    }

                    _rates.value = result!!.rates
                    ratePreferences.saveRates(result.rates)

                    // Compute delay
                    val expiresStr = result.expiresAt
                    val delayMs = if (expiresStr.isNotBlank()) {
                        try {
                            val expiresMs = ZonedDateTime.parse(expiresStr, dateFmt).toInstant().toEpochMilli()
                            (expiresMs - System.currentTimeMillis() - 5_000L).coerceAtLeast(5_000L)
                        } catch (e: Exception) {
                            Log.e("RateViewModel", "Invalid expiresAt: $expiresStr", e)
                            intervalFallbackMs
                        }
                    } else {
                        intervalFallbackMs
                    }

                    Log.d("RateViewModel", "Next poll in ${delayMs}ms")
                    delay(delayMs)

                } catch (e: Exception) {
                    Log.e("RateViewModel", "Error fetching rates: ${e.message}", e)
                    delay(intervalFallbackMs)
                }
            }
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
        Log.d("RateViewModel", "Polling stopped")
        super.onCleared()
    }
}
