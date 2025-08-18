package com.thellex.payments.features.auth.viewModel

import android.annotation.SuppressLint
import android.content.Context
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.UserEntity
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.features.wallet.prefrences.WalletManagerPreferences
import kotlinx.coroutines.flow.Flow

class UserRepository private constructor(private val context: Context) {
    private val walletPreferences = WalletManagerPreferences(context)

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

    fun getToken(): Flow<String?> {
        return UserPreferences.getToken(context)
    }

    fun getAuthResult(): Flow<UserEntity?> {
        return UserPreferences.getAuthResult(context)
    }

    fun saveToken(token: String?) {
        if (token != null) {
            UserPreferences.saveToken(context, token)
        } else {
            UserPreferences.clearToken(context)
        }
    }

    fun saveAuthResult(result: UserEntity?) {
        UserPreferences.saveAuthResult(context, result)
    }

    fun addFiatCryptoRampTransaction(transaction: IFiatCryptoRampTransactionsDto) {
        UserPreferences.addFiatCryptoRampTransaction(context, transaction)
    }

    fun isNotificationsDismissed(): Boolean = UserPreferences.isNotificationsDismissed(context)

    fun setNotificationsDismissed(dismissed: Boolean) {
        UserPreferences.setNotificationsDismissed(context, dismissed)
    }

    fun areNotificationsEnabled(): Boolean {
        return UserPreferences.hasEnabledNotifications(context)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        UserPreferences.setHasEnabledNotifications(context, enabled)
    }

    fun logout() {
        UserPreferences.clearToken(context)
        UserPreferences.clearAuthResult(context)
        walletPreferences.clearWalletCache()
    }
}