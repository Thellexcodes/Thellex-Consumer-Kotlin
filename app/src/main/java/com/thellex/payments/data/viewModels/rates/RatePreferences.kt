package com.thellex.payments.data.viewModels.rates

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thellex.payments.features.wallet.model.IRatesDto

class RatePreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("RateCache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveRates(rates: List<IRatesDto>) {
        try {
            val json = gson.toJson(rates)
            sharedPreferences.edit().putString("rates", json).apply()
            Log.d("RatePreferences", "Saved rates: $rates")
        } catch (e: Exception) {
            Log.e("RatePreferences", "Failed to save rates: ${e.message}", e)
        }
    }

    fun loadRates(): List<IRatesDto> {
        val json = sharedPreferences.getString("rates", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<IRatesDto>>() {}.type
            gson.fromJson<List<IRatesDto>>(json, type)
        } catch (e: Exception) {
            Log.e("RatePreferences", "Failed to load rates: ${e.message}", e)
            emptyList()
        }
    }

    fun clearRates() {
        sharedPreferences.edit().remove("rates").apply()
        Log.d("RatePreferences", "Cleared cached rates")
    }
}
