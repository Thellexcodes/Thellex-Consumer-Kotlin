package com.thellex.pay.data.repository

import android.content.Context
import android.util.Log
import com.thellex.pay.data.datastore.getSupportedChainsCache
import com.thellex.pay.data.datastore.saveSupportedChains
import com.thellex.pay.data.model.ApiResponse
import com.thellex.pay.data.model.ChainInfo
import com.thellex.pay.network.services.ApiClient
import java.util.concurrent.TimeUnit

class BaseSettingsRepository(
    private val context: Context
) {

    private val TAG = "BaseSettingsRepository"

    suspend fun getBaseSettings(authToken: String): List<ChainInfo> {
        val now = System.currentTimeMillis()
        val cacheValidity = TimeUnit.DAYS.toMillis(7)

        val cached = context.getSupportedChainsCache()
        if (cached != null) {
            val (timestamp, chains) = cached
            if (now - timestamp < cacheValidity && chains.isNotEmpty()) {
                Log.d(TAG, "Using cached supported chains")
                return chains
            }
        }

        return try {
            val response = ApiClient
                .getAppApi(context, authToken)
                .getAppSettings().result!!
            Log.d(TAG, "data is saved at $response")

            context.saveSupportedChains(response, now)
            response
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch supported chains", e)
            cached?.second ?: emptyList()
        }
    }
}
