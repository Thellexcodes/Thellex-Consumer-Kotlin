package com.thellex.payments.data.viewModels.rates

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thellex.payments.features.auth.viewModel.UserRepository
import com.thellex.payments.features.wallet.model.IRatesDto
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RateViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository.getInstance(application)
    private val _rates = MutableStateFlow<List<IRatesDto>>(emptyList())
    val rates: StateFlow<List<IRatesDto>> = _rates.asStateFlow()
    private var pollJob: Job? = null
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFmt = DateTimeFormatter.ISO_DATE_TIME
    private val sharedPreferences = application.getSharedPreferences("RateCache", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        // Restore cached rates on initialization
        restoreCachedRates()
    }

    private fun restoreCachedRates() {
        val cachedRatesJson = sharedPreferences.getString("rates", null)
        if (cachedRatesJson != null) {
            try {
                val type = object : TypeToken<List<IRatesDto>>() {}.type
                val cachedRates: List<IRatesDto> = gson.fromJson(cachedRatesJson, type)
                if (cachedRates.isNotEmpty()) {
                    _rates.value = cachedRates
                    Log.d("RateViewModel", "Restored cached rates: $cachedRates")
                } else {
                    Log.d("RateViewModel", "Cached rates empty")
                }
            } catch (e: Exception) {
                Log.e("RateViewModel", "Failed to restore cached rates: $e")
            }
        } else {
            Log.d("RateViewModel", "No cached rates found")
        }
    }

    private fun cacheRates(rates: List<IRatesDto>) {
        try {
            val ratesJson = gson.toJson(rates)
            sharedPreferences.edit().putString("rates", ratesJson).apply()
            Log.d("RateViewModel", "Cached rates: $rates")
        } catch (e: Exception) {
            Log.e("RateViewModel", "Failed to cache rates: $e")
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
                    Log.d("RateViewModel", "Fetching rates with token: [REDACTED]")
                    val response = ApiClient.getAuthenticatedPaymentApi(authToken).getRates()
                    val result = response.result

                    if (result?.rates.isNullOrEmpty()) {
                        Log.w("RateViewModel", "Rates are null or empty, retrying in ${intervalFallbackMs}ms")
                        delay(intervalFallbackMs)
                        continue
                    }

                    // Cache the rates safely
                    try {
                        _rates.value = result!!.rates
                        cacheRates(result.rates)
                        Log.d("RateViewModel", "Fetched and cached rates: ${result.rates}")
                    } catch (e: Exception) {
                        Log.e("RateViewModel", "Error caching rates: ${e.message}", e)
                    }

                    // Calculate delay from expiresAt
                    val expiresStr = result?.expiresAt
                    val delayMs = if (!expiresStr.isNullOrBlank()) {
                        try {
                            val expiresMs = ZonedDateTime.parse(expiresStr, dateFmt).toInstant().toEpochMilli()
                            (expiresMs - System.currentTimeMillis() - 5_000L).coerceAtLeast(5_000L)
                        } catch (e: Exception) {
                            Log.e("RateViewModel", "Invalid expiresAt format: $expiresStr", e)
                            intervalFallbackMs
                        }
                    } else {
                        Log.w("RateViewModel", "No expiresAt provided, using fallback delay")
                        intervalFallbackMs
                    }

                    Log.d("RateViewModel", "Scheduling next poll in ${delayMs}ms")
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