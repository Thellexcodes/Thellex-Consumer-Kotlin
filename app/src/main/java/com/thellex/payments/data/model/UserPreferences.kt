package com.thellex.payments.data.model

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object UserPreferences {
    private const val PREFS_NAME = "user_prefs"
    private val userFlow = MutableSharedFlow<UserEntity?>(replay = 1)

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("token", token)
            .apply()
    }

    fun clearToken(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("token")
            .apply()
    }

    fun getToken(context: Context): Flow<String?> = flow {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        emit(prefs.getString("token", null))
    }

    fun saveAuthResult(context: Context, user: UserEntity?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("user", if (user != null) Gson().toJson(user) else null)
            .apply()
        userFlow.tryEmit(user)
    }

    fun clearAuthResult(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("user")
            .apply()
        userFlow.tryEmit(null)
    }

    fun getAuthResult(context: Context): Flow<UserEntity?> = flow {
        emit(getAuthResultSync(context))
        emitAll(userFlow)
    }

    private fun getAuthResultSync(context: Context): UserEntity? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("user", null)
        return try {
            if (json != null) Gson().fromJson(json, UserEntity::class.java) else null
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error parsing user: ${e.message}")
            null
        }
    }

    fun addTransactionHistory(context: Context, transaction: ITransactionHistoryDto) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.transactionHistory.toMutableList().apply {
                if (none { it.transactionId == transaction.transactionId }) {
                    add(transaction)
                }
            }.sortedByDescending { it.createdAt }
            val updatedUser = currentUser.copy(transactionHistory = updatedList)
            saveAuthResult(context, updatedUser)
        }
    }

    fun updateTransactionById(context: Context, transactionId: String, transaction: ITransactionHistoryDto) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.transactionHistory.map {
                if (it.transactionId == transactionId) transaction else it
            }.sortedByDescending { it.createdAt }
            val updatedUser = currentUser.copy(transactionHistory = updatedList)
            saveAuthResult(context, updatedUser)
        }
    }

    fun addNotification(context: Context, notification: NotificationEntity) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.notifications.toMutableList().apply {
                add(notification)
            }
            val updatedUser = currentUser.copy(notifications = updatedList)
            saveAuthResult(context, updatedUser)
        }
    }

    fun getTransactionById(context: Context, transactionId: String): ITransactionHistoryDto? {
        val currentUser = getAuthResultSync(context)
        return currentUser?.transactionHistory?.find { it.transactionId == transactionId }
    }
}private val Context.dataStore by preferencesDataStore(name = "user_prefs")