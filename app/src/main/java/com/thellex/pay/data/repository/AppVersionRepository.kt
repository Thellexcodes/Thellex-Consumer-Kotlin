package com.thellex.pay.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.nfc.Tag
import android.util.Log
import com.thellex.pay.data.datastore.getAppVersionCache
import com.thellex.pay.data.datastore.saveAppVersionCache
import com.thellex.pay.data.model.AppVersionCache
import com.thellex.pay.data.model.AppVersionState
import com.thellex.pay.network.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AppVersionRepository(private val context: Context) {
    private val TAG = "AppVersionRepository"

    suspend fun checkAppVersion(): AppVersionState {
        val currentVersion = getCurrentVersion()
        val currentTime = System.currentTimeMillis()
        val oneWeekMillis = TimeUnit.DAYS.toMillis(7)

        // Check cache
        val cachedVersion = context.getAppVersionCache()
        if (cachedVersion != null && (currentTime - cachedVersion.lastCheckTimestamp) < oneWeekMillis) {
            return getStateFromCache(cachedVersion, currentVersion)
        }

        // Make API call
        return try {
            val response = ApiClient.getAppApi(context, "").checkAppVersion(
                platform = "android",
                currentVersion = currentVersion
            )

            Log.d(TAG, "response is $response")

            val data = response.result
            if (data != null) {
                // Cache result
                val cache = AppVersionCache(
                    lastCheckTimestamp = currentTime,
                    latestVersion = data.latestVersion,
                    minSupportedVersion = data.minSupportedVersion,
                    forceUpdate = data.forceUpdate,
                    updateType = data.updateType,
                    downloadUrl = data.downloadUrl,
                    releaseNotes = data.releaseNotes
                )
                context.saveAppVersionCache(cache)

                getStateFromCache(cache, currentVersion)
            } else {
                AppVersionState.Error("Invalid API response")
            }
        } catch (e: Exception) {
            AppVersionState.Error("Failed to check for updates: ${e.message}")
        }
    }

    private fun getStateFromCache(cache: AppVersionCache, currentVersion: String): AppVersionState {
        return when {
            cache.forceUpdate || cache.minSupportedVersion > currentVersion -> AppVersionState.ForceUpdate(
                latestVersion = cache.latestVersion,
                downloadUrl = cache.downloadUrl,
                updateType = cache.updateType,
                releaseNotes = cache.releaseNotes
            )
            cache.latestVersion != currentVersion -> AppVersionState.OptionalUpdate(
                latestVersion = cache.latestVersion,
                downloadUrl = cache.downloadUrl,
                updateType = cache.updateType,
                releaseNotes = cache.releaseNotes
            )
            else -> AppVersionState.UpToDate
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.1"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }
}

