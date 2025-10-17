package com.thellex.pay.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thellex.pay.data.model.AppVersionCache
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.appVersionDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_version")

private object PreferencesKeys {
    val LAST_CHECK_TIMESTAMP = longPreferencesKey("last_check_timestamp")
    val LATEST_VERSION = stringPreferencesKey("latest_version")
    val MIN_SUPPORTED_VERSION = stringPreferencesKey("min_supported_version")
    val FORCE_UPDATE = booleanPreferencesKey("force_update")
    val UPDATE_TYPE = stringPreferencesKey("update_type")
    val DOWNLOAD_URL = stringPreferencesKey("download_url")
    val RELEASE_NOTES = stringPreferencesKey("releaseNotes")
}

suspend fun Context.saveAppVersionCache(cache: AppVersionCache) {
    appVersionDataStore.edit { preferences ->
        preferences[PreferencesKeys.LAST_CHECK_TIMESTAMP] = cache.lastCheckTimestamp
        preferences[PreferencesKeys.LATEST_VERSION] = cache.latestVersion
        preferences[PreferencesKeys.MIN_SUPPORTED_VERSION] = cache.minSupportedVersion
        preferences[PreferencesKeys.FORCE_UPDATE] = cache.forceUpdate
        preferences[PreferencesKeys.UPDATE_TYPE] = cache.updateType
        cache.downloadUrl?.let { preferences[PreferencesKeys.DOWNLOAD_URL] = it }
    }
}

suspend fun Context.getAppVersionCache(): AppVersionCache? {
    return appVersionDataStore.data.map { preferences ->
        val timestamp = preferences[PreferencesKeys.LAST_CHECK_TIMESTAMP] ?: return@map null
        val latestVersion = preferences[PreferencesKeys.LATEST_VERSION] ?: return@map null
        val minSupportedVersion = preferences[PreferencesKeys.MIN_SUPPORTED_VERSION] ?: return@map null
        val forceUpdate = preferences[PreferencesKeys.FORCE_UPDATE] ?: return@map null
        val updateType = preferences[PreferencesKeys.UPDATE_TYPE] ?: return@map null
        val releaseNotes = preferences[PreferencesKeys.RELEASE_NOTES] ?: return@map  null
        val downloadUrl = preferences[PreferencesKeys.DOWNLOAD_URL]

        AppVersionCache(
            lastCheckTimestamp = timestamp,
            latestVersion = latestVersion,
            minSupportedVersion = minSupportedVersion,
            forceUpdate = forceUpdate,
            updateType = updateType,
            downloadUrl = downloadUrl,
            releaseNotes =  releaseNotes
        )
    }.first()
}