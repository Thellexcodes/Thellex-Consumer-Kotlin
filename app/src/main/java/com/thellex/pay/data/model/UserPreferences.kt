package com.thellex.pay.data.model

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.thellex.pay.data.model.UserPreferences.userDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking

object UserPreferences {
    private const val TAG = "UserPreferences"

    private val Context.userDataStore by preferencesDataStore(name = "user_prefs")
    private val Context.appDataStore by preferencesDataStore(name = "app_prefs")

    private val KEY_TOKEN = stringPreferencesKey("token")
    private val KEY_USER = stringPreferencesKey("user")
    private val KEY_ADMIN_DATA = stringPreferencesKey("admin_data")
    private val KEY_REWARDS_COUNT = intPreferencesKey("rewards_count")
    private val KEY_REWARDS_DISMISSED = booleanPreferencesKey("rewards_dismissed")
    private val KEY_NOTIFICATIONS_DISMISSED = booleanPreferencesKey("notifications_dismissed")
    private val KEY_HAS_ENABLED_NOTIFICATIONS = booleanPreferencesKey("has_enabled_notifications")
    private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    private val HAS_PIN = booleanPreferencesKey("has_pin")

    private val gson = Gson()
    private val userFlow = MutableSharedFlow<UserEntity?>(replay = 1)
    private val adminDataFlow = MutableSharedFlow<AdminData?>(replay = 1)
    private val _rewardsCount = MutableLiveData<Int>()

    // --- Token ---
    suspend fun saveToken(context: Context, token: String) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
    }

    suspend fun clearToken(context: Context) {
        context.userDataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
        }
    }

    fun getToken(context: Context): Flow<String?> =
        context.userDataStore.data.map { it[KEY_TOKEN] }

    suspend fun setBiometricEnabled(context: Context, enabled: Boolean) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    fun isBiometricEnabled(context: Context): Flow<Boolean> =
        context.userDataStore.data.map { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setHasPin(context: Context, hasPin: Boolean) {
        context.userDataStore.edit { prefs ->
            prefs[HAS_PIN] = hasPin
        }
    }

    fun hasPin(context: Context): Flow<Boolean> =
        context.userDataStore.data.map { prefs -> prefs[HAS_PIN] ?: false
    }

    // --- Auth Result ---
    suspend fun saveAuthResult(context: Context, user: UserEntity?) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_USER] = if (user != null) gson.toJson(user) else ""
        }
        userFlow.emit(user)
    }

    suspend fun clearAuthResult(context: Context) {
        context.userDataStore.edit { prefs -> prefs.remove(KEY_USER) }
        userFlow.emit(null)
    }

    fun getAuthResult(context: Context): Flow<UserEntity?> =
        context.userDataStore.data
            .map { prefs ->
                prefs[KEY_USER]?.takeIf { it.isNotEmpty() }?.let {
                    try { gson.fromJson(it, UserEntity::class.java) } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user: ${e.message}")
                        null
                    }
                }
            }
            .onStart { emitAll(userFlow) }

    fun getAuthResultSync(context: Context): UserEntity? = runBlocking {
        context.userDataStore.data.first()[KEY_USER]?.let {
            try { gson.fromJson(it, UserEntity::class.java) } catch (e: Exception) {
                Log.e(TAG, "Error parsing user: ${e.message}")
                null
            }
        }
    }

    // --- Admin Data ---
    suspend fun saveAdminResult(context: Context, adminData: AdminData?) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_ADMIN_DATA] = if (adminData != null) gson.toJson(adminData) else ""
        }
        adminDataFlow.emit(adminData)
    }

    fun getAdminResult(context: Context): Flow<AdminData?> =
        context.userDataStore.data
            .map { prefs ->
                prefs[KEY_ADMIN_DATA]?.takeIf { it.isNotEmpty() }?.let {
                    try { gson.fromJson(it, AdminData::class.java) } catch (e: Exception) {
                        Log.e(TAG, "Error parsing AdminData: ${e.message}")
                        null
                    }
                }
            }
            .onStart { emitAll(adminDataFlow) }

    // --- Rewards ---
    fun getAvailableRewards(context: Context): Flow<Int> =
        context.userDataStore.data.map { it[KEY_REWARDS_COUNT] ?: 0 }

    suspend fun updateRewardsCount(context: Context, count: Int) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_REWARDS_COUNT] = count
        }
        _rewardsCount.postValue(count)
        Log.d(TAG, "Updated rewards count: $count")
    }

    suspend fun setRewardsDismissed(context: Context, dismissed: Boolean) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_REWARDS_DISMISSED] = dismissed
        }
        Log.d(TAG, "Rewards dismissed set to: $dismissed")
    }

    fun isRewardsDismissed(context: Context): Flow<Boolean> =
        context.userDataStore.data.map { it[KEY_REWARDS_DISMISSED] ?: false }

    // --- Notifications ---
    suspend fun setNotificationsDismissed(context: Context, dismissed: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_DISMISSED] = dismissed
        }
        Log.d(TAG, "Notifications dismissed set to: $dismissed")
    }

    fun isNotificationsDismissed(context: Context): Flow<Boolean> =
        context.appDataStore.data.map { it[KEY_NOTIFICATIONS_DISMISSED] ?: false }

    suspend fun setHasEnabledNotifications(context: Context, enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_HAS_ENABLED_NOTIFICATIONS] = enabled
        }
        Log.d(TAG, "has_enabled_notifications set to: $enabled")
    }

    fun hasEnabledNotifications(context: Context): Flow<Boolean> =
        context.appDataStore.data.map { it[KEY_HAS_ENABLED_NOTIFICATIONS] ?: false }

    // --- Transaction Management ---
    suspend fun addTransactionHistory(context: Context, transaction: ITransactionHistoryDto) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.transactionHistory?.toMutableList()?.apply {
                if (none { it.id == transaction.id }) {
                    add(transaction)
                    if (transaction.transactionType == TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT) {
                        val currentRewards = getAvailableRewards(context).first()
                        updateRewardsCount(context, currentRewards + 1)
                        setRewardsDismissed(context, false)
                    }
                }
            }?.sortedByDescending { it.createdAt }

            val updatedUser = currentUser.copy(transactionHistory = updatedList)
            saveAuthResult(context, updatedUser)
        }
    }

    suspend fun updateTransactionById(context: Context, transactionId: String, transaction: ITransactionHistoryDto) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.transactionHistory?.map {
                if (it.id == transactionId) transaction else it
            }?.sortedByDescending { it.createdAt }

            val updatedUser = currentUser.copy(transactionHistory = updatedList)
            saveAuthResult(context, updatedUser)
        }
    }

    suspend fun addNotification(context: Context, notification: NotificationEntity) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.notifications?.toMutableList()?.apply { add(notification) }
            val updatedUser = currentUser.copy(notifications = updatedList)
            saveAuthResult(context, updatedUser)
            setNotificationsDismissed(context, false)
        }
    }

    fun getTransactionById(context: Context, transactionId: String): ITransactionHistoryDto? {
        val currentUser = getAuthResultSync(context)
        return currentUser?.transactionHistory?.find { it.id == transactionId }
    }

    suspend fun updateFiatCryptoRampTransactionById(
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

    suspend fun addFiatCryptoRampTransaction(context: Context, transaction: IFiatCryptoRampTransactionsDto) {
        val currentUser = getAuthResultSync(context)
        if (currentUser != null) {
            val updatedList = currentUser.fiatCryptoRampTransactions?.toMutableList()
            val alreadyExists = updatedList?.any { it.id == transaction.id }
            if (alreadyExists == false) {
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

    private suspend fun refreshUser(context: Context) {
        val currentUser = getAuthResultSync(context)
        userFlow.emit(currentUser)
    }
}