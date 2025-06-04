package com.thellex.pos.data.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val AUTH_RESULT_KEY = stringPreferencesKey("auth_result")
    private val json = Json { ignoreUnknownKeys = true }

    fun getToken(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[TOKEN_KEY]
    }

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { prefs -> prefs[TOKEN_KEY] = token }
    }

    fun getAuthResult(context: Context): Flow<UserEntity?> =
        context.dataStore.data.map { prefs ->
            prefs[AUTH_RESULT_KEY]?.let {
                try {
                    json.decodeFromString<UserEntity>(it)
                } catch (e: Exception) {
                    null
                }
            }
        }

    suspend fun saveAuthResult(context: Context, result: UserEntity) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_RESULT_KEY] = json.encodeToString(result)
        }
    }

    suspend fun clearAuthResult(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(AUTH_RESULT_KEY)
        }
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}
