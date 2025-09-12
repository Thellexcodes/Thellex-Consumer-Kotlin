package com.thellex.payments.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.thellex.payments.data.datastore.getAppVersionCache
import com.thellex.payments.data.datastore.saveAppVersionCache
import com.thellex.payments.data.model.AppVersionCache
import com.thellex.payments.data.model.AppVersionState
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.network.services.AppService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AppVersionRepository(private val context: Context)
{ private val _versionState = MutableStateFlow<AppVersionState>(AppVersionState.Idle)
    val versionState: StateFlow<AppVersionState> = _versionState

    suspend fun checkAppVersion() {
        _versionState.value = AppVersionState.Idle
        val currentVersion = getCurrentVersion()
        val currentTime = System.currentTimeMillis()
        val oneWeekMillis = TimeUnit.DAYS.toMillis(7)

        // Check cache
        val cachedVersion = context.getAppVersionCache()
        if (cachedVersion != null && (currentTime - cachedVersion.lastCheckTimestamp) < oneWeekMillis) {
            // Use cached data
            updateStateFromCache(cachedVersion, currentVersion)
            return
        }

        // Make API call
        try {
            val response = ApiClient.getAppApi("").checkAppVersion(
                platform = "android",
                currentVersion = currentVersion
            )

//            if (response.status == "success" && response.data != null) {
//                val cache = AppVersionCache(
//                    lastCheckTimestamp = currentTime,
//                    latestVersion = response.data.latestVersion,
//                    minSupportedVersion = response.data.minSupportedVersion,
//                    forceUpdate = response.data.forceUpdate,
//                    updateType = response.data.updateType,
//                    downloadUrl = response.data.downloadUrl
//                )
//                context.saveAppVersionCache(cache)
//                updateStateFromCache(cache, currentVersion)
//            } else {
//                _versionState.value = AppVersionState.Error("Invalid API response")
//            }
        } catch (e: Exception) {
            _versionState.value = AppVersionState.Error("Failed to check for updates: ${e.message}")
        }
    }

    private suspend fun updateStateFromCache(cache: AppVersionCache, currentVersion: String) {
        withContext(Dispatchers.Main) {
            if (cache.forceUpdate || cache.minSupportedVersion > currentVersion) {
                _versionState.value = AppVersionState.ForceUpdate(
                    latestVersion = cache.latestVersion,
                    downloadUrl = cache.downloadUrl,
                    updateType = cache.updateType
                )
            } else if (cache.latestVersion != currentVersion) {
                _versionState.value = AppVersionState.OptionalUpdate(
                    latestVersion = cache.latestVersion,
                    downloadUrl = cache.downloadUrl,
                    updateType = cache.updateType
                )
            } else {
                _versionState.value = AppVersionState.UpToDate
            }
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.1"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0" // Default fallback version
        }
    }
}
