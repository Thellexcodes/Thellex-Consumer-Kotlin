package com.thellex.pay.features.auth.repository

import android.annotation.SuppressLint
import android.content.Context
import com.thellex.pay.data.model.AdminData
import com.thellex.pay.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.pay.data.model.UserEntity
import com.thellex.pay.data.model.UserPreferences
import com.thellex.pay.data.viewModels.rates.RatePreferences
import com.thellex.pay.features.wallet.prefrences.WalletManagerPreferences
import kotlinx.coroutines.flow.Flow

class UserRepository private constructor(private val context: Context) {

    private val walletPreferences = WalletManagerPreferences(context)
    private val ratePreferences = RatePreferences(context)

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(context: Context): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // ------------------ TOKEN ------------------
    fun getToken(): Flow<String?> = UserPreferences.getToken(context)

    suspend fun saveToken(token: String?) {
        if (token != null) {
            UserPreferences.saveToken(context, token)
        } else {
            UserPreferences.clearToken(context)
        }
    }

    // ------------------ USER ------------------
    fun getAuthResult(): Flow<UserEntity?> = UserPreferences.getAuthResult(context)

    suspend fun saveAuthResult(result: UserEntity?) {
        UserPreferences.saveAuthResult(context, result)
    }

    fun getAuthResultSync(): UserEntity? = UserPreferences.getAuthResultSync(context)

    // ------------------ ADMIN DATA ------------------
    fun getAdminData(): Flow<AdminData?> = UserPreferences.getAdminResult(context)

    suspend fun saveAdminResult(adminData: AdminData?) {
        UserPreferences.saveAdminResult(context, adminData)
    }

    // ------------------ TRANSACTIONS ------------------
    suspend fun addFiatCryptoRampTransaction(transaction: IFiatCryptoRampTransactionsDto) {
        UserPreferences.addFiatCryptoRampTransaction(context, transaction)
    }

    // ------------------ NOTIFICATIONS ------------------
    fun isNotificationsDismissed(): Flow<Boolean> =
        UserPreferences.isNotificationsDismissed(context)

    suspend fun setNotificationsDismissed(dismissed: Boolean) {
        UserPreferences.setNotificationsDismissed(context, dismissed)
    }

    fun areNotificationsEnabled(): Flow<Boolean> =
        UserPreferences.hasEnabledNotifications(context)

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        UserPreferences.setHasEnabledNotifications(context, enabled)
    }

    // ------------------ BIOMETRIC ------------------
    suspend fun setBiometricEnabled(enabled: Boolean) {
        UserPreferences.setBiometricEnabled(context, enabled)
    }

    fun isBiometricEnabled(): Flow<Boolean> =
        UserPreferences.isBiometricEnabled(context)

    // ------------------ PIN ------------------
    suspend fun setHasPin(hasPin: Boolean) {
        UserPreferences.setHasPin(context, hasPin)
    }

    fun hasPin(): Flow<Boolean> =
        UserPreferences.hasPin(context)

    // ------------------ LOGOUT ------------------
    suspend fun logout() {
        UserPreferences.clearToken(context)
        UserPreferences.clearAuthResult(context)
        walletPreferences.clearWalletCache()
        ratePreferences.clearRates()
    }
}