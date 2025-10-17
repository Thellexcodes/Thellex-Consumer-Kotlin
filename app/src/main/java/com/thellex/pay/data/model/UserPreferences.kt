package com.thellex.pay.data.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

object UserPreferences {
    private const val PREFS_NAME = "user_prefs"
    private const val APP_PREFS_NAME = "app_prefs"
    private const val KEY_REWARDS_COUNT = "rewards_count"
    private const val KEY_REWARDS_DISMISSED = "rewards_dismissed"
    private const val KEY_NOTIFICATIONS_DISMISSED = "notifications_dismissed"
    private const val KEY_HAS_ENABLED_NOTIFICATIONS = "has_enabled_notifications"
    private const val TAG = "UserPreferences"
    private val userFlow = MutableSharedFlow<UserEntity?>(replay = 1)
    private val adminDataFlow = MutableSharedFlow<AdminData?>(replay = 1)
    private val _rewardsCount = MutableLiveData<Int>()

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

    // --- AdminData ---
    fun saveAdminResult(context: Context, adminData: AdminData?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("admin_data", if (adminData != null) Gson().toJson(adminData) else null).apply()
        adminDataFlow.tryEmit(adminData)
    }

    fun getAdminResult(context: Context): Flow<AdminData?> = flow {
        emit(getAdminResultSync(context))
        emitAll(adminDataFlow)
    }

    fun getAdminResultSync(context: Context): AdminData? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("admin_data", null)
        return try {
            if (json != null) Gson().fromJson(json, AdminData::class.java) else null
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing AdminData: ${e.message}")
            null
        }
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

    fun getAuthResultSync(context: Context): UserEntity? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("user", null)
        return try {
            if (json != null) Gson().fromJson(json, UserEntity::class.java) else null
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing user: ${e.message}")
            null
        }
    }

    fun getAvailableRewards(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_REWARDS_COUNT, 0)
    }

    fun updateRewardsCount(context: Context, count: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_REWARDS_COUNT, count)
            .apply()
        _rewardsCount.postValue(count)
        Log.d(TAG, "Updated rewards count: $count")
    }

    fun isRewardsDismissed(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REWARDS_DISMISSED, false)
    }

    fun setRewardsDismissed(context: Context, dismissed: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REWARDS_DISMISSED, dismissed)
            .apply()
        Log.d(TAG, "Rewards dismissed set to: $dismissed")
    }

    fun isNotificationsDismissed(context: Context): Boolean {
        return context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_NOTIFICATIONS_DISMISSED, false)
    }

    fun setNotificationsDismissed(context: Context, dismissed: Boolean) {
        context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFICATIONS_DISMISSED, dismissed)
            .apply()
        Log.d(TAG, "Notifications dismissed set to: $dismissed")
    }

    fun hasEnabledNotifications(context: Context): Boolean {
        return context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_ENABLED_NOTIFICATIONS, false)
    }

    fun setHasEnabledNotifications(context: Context, enabled: Boolean) {
        context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_ENABLED_NOTIFICATIONS, enabled)
            .apply()
        Log.d(TAG, "has_enabled_notifications set to: $enabled")
    }

    fun addTransactionHistory(context: Context, transaction: ITransactionHistoryDto) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.transactionHistory?.toMutableList()?.apply {
                if (none { it.id == transaction.id }) {
                    add(transaction)
                    if (transaction.transactionType == TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT) {
                        val currentRewards = getAvailableRewards(context)
                        updateRewardsCount(context, currentRewards + 1)
                        setRewardsDismissed(context, false)
                    }
                }
            }?.sortedByDescending { it.createdAt }
            val updatedUser = currentUser.copy(transactionHistory = updatedList)
            Log.w(TAG, "Transaction $transaction and updatedUser: $updatedUser")
            saveAuthResult(context, updatedUser)
        }
    }

    fun updateTransactionById(context: Context, transactionId: String, transaction: ITransactionHistoryDto) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.transactionHistory?.map {
                if (it.id == transactionId) transaction else it
            }?.sortedByDescending { it.createdAt }
            val updatedUser = currentUser.copy(transactionHistory = updatedList)
            saveAuthResult(context, updatedUser)
        }
    }

    fun addNotification(context: Context, notification: NotificationEntity) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.notifications?.toMutableList()?.apply {
                add(notification)
            }
            val updatedUser = currentUser.copy(notifications = updatedList)
            saveAuthResult(context, updatedUser)
            setNotificationsDismissed(context, false)
        }
    }

    fun getTransactionById(context: Context, transactionId: String): ITransactionHistoryDto? {
        val currentUser = getAuthResultSync(context)
        return currentUser?.transactionHistory?.find { it.id == transactionId }
    }

    fun updateFiatCryptoRampTransactionById(
        context: Context,
        transactionId: String,
        updatedTransaction: IFiatCryptoRampTransactionsDto
    ) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.fiatCryptoRampTransactions?.map {
                if (it.id == transactionId) updatedTransaction else it
            }
            val updatedUser = currentUser.copy(fiatCryptoRampTransactions = updatedList)
            saveAuthResult(context, updatedUser)
        }
    }

    fun addFiatCryptoRampTransaction(
        context: Context,
        transaction: IFiatCryptoRampTransactionsDto
    ) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.fiatCryptoRampTransactions?.toMutableList()
            val alreadyExists = updatedList?.any { it.id == transaction.id }
            if (!alreadyExists!!) {
                updatedList.add(transaction)
                Log.d(TAG, "Fiat ramp Transaction added: ${transaction.id}")
            } else {
                Log.d(TAG, "Transaction already exists: ${transaction.id}")
            }
            val updatedUser = currentUser.copy(fiatCryptoRampTransactions = updatedList)
            saveAuthResult(context, updatedUser)
            refreshUser(context)
        } else {
            Log.d(TAG, "No user found when trying to add transaction: ${transaction.id}")
        }
    }

    private fun refreshUser(context: Context) {
        val currentUser = getAuthResultSync(context)
        userFlow.tryEmit(currentUser)
    }
}
