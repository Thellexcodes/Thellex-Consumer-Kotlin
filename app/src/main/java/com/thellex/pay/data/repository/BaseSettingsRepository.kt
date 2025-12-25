package com.thellex.pay.data.repository

import android.content.Context
import android.util.Log
import com.thellex.pay.data.datastore.getBaseSettingsCache
import com.thellex.pay.data.datastore.saveBaseSettings
import com.thellex.pay.network.services.ApiClient
import java.util.concurrent.TimeUnit

class BaseSettingsRepository(
    private val context: Context
) {

    private val TAG = "BaseSettingsRepository"

    suspend fun getBaseSettings(authToken: String){
        val now = System.currentTimeMillis()
        val cacheValidity = TimeUnit.DAYS.toMillis(7)

//        val cached = context.getBaseSettingsCache()
//        if (cached != null) {
//            val (timestamp, chains) = cached
//            if (now - timestamp < cacheValidity && chains.isNotEmpty()) {
//                Log.d(TAG, "Using cached supported chains")
//                return
//            }
//        }

        return try {
            val response = ApiClient
                .getAppApi(context, authToken)
                .getBaseAppSettings()
                .result ?: return

            context.saveBaseSettings(
                chains = response.supportedChains,
                tokens = response.supportedTokens,
                timestamp = now
            )

        } catch (e: Exception) {
        }
    }
}
