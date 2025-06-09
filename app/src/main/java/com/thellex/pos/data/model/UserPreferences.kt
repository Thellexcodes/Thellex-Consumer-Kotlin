package com.thellex.pos.data.model

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {

    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val AUTH_RESULT_KEY = stringPreferencesKey("auth_result")

    private val gson = Gson()

    fun getToken(context: Context): Flow<String?> =
        context.dataStore.data.map { it[TOKEN_KEY] }

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { prefs -> prefs[TOKEN_KEY] = token }
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { prefs -> prefs.remove(TOKEN_KEY) }
    }

    fun getAuthResult(context: Context): Flow<UserEntity?> =
        context.dataStore.data.map { prefs ->
            prefs[AUTH_RESULT_KEY]?.let {
                try {
                    gson.fromJson(it, UserEntity::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }

    fun saveAuthResultAsync(appContext: Context, result: UserEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = gson.toJson(result)
//                Log.d("UserPreferencesS", "Serialized UserEntity: $json")

                appContext.dataStore.edit { prefs ->
                    prefs[AUTH_RESULT_KEY] = json
                    Log.d("UserPreferencesS", "UserEntity saved successfully.")
                }
            } catch (e: Exception) {
                Log.e("UserPreferencesS", "Failed to save UserEntity: ${e.message}", e)
            }
        }
    }

    suspend fun clearAuthResult(context: Context) {
        Log.d("UserPreferencesS", "Clearing auth result")
        context.dataStore.edit { prefs ->
            prefs.remove(AUTH_RESULT_KEY)
            Log.d("UserPreferencesS", "Auth result cleared")
        }
    }

    suspend fun updateUserEntity(context: Context, updateBlock: (UserEntity) -> UserEntity) {
        Log.d("UserPreferencesS", "Updating UserEntity")
        context.dataStore.edit { prefs ->
            val currentJson = prefs[AUTH_RESULT_KEY]
            val currentUser = currentJson?.let {
                try {
                    gson.fromJson(it, UserEntity::class.java)
                } catch (e: Exception) {
                    Log.e("UserPreferencesS", "Error parsing current UserEntity: ${e.message}")
                    null
                }
            }

            val updatedUser = currentUser?.let(updateBlock)
            if (updatedUser != null) {
                val newJson = gson.toJson(updatedUser)
                prefs[AUTH_RESULT_KEY] = newJson
                Log.d("UserPreferencesS", "UserEntity updated: $newJson")
            } else {
                Log.w("UserPreferencesS", "No existing UserEntity to update.")
            }
        }
    }

    // ✅ Add transaction history entry
    suspend fun addTransactionHistory(context: Context, transaction: TransactionHistoryEntity) {
        updateUserEntity(context) { user ->
            val updatedList = user.transactionHistory?.toMutableList() ?: mutableListOf()
            updatedList.add(transaction)
            user.copy(transactionHistory = updatedList)
        }
    }

    // ✅ Add notification
    suspend fun addNotification(context: Context, notification: NotificationEntity) {
        updateUserEntity(context) { user ->
            val updatedList = user.notifications?.toMutableList() ?: mutableListOf()
            updatedList.add(notification)
            user.copy(notifications = updatedList)
        }
    }
}