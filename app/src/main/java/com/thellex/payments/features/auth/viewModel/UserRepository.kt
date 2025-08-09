package com.thellex.payments.features.auth.viewModel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
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

    suspend fun saveToken(token: String?) {
        if (token != null) {
            UserPreferences.saveToken(context, token)
        } else {
            UserPreferences.clearToken(context)
        }
    }

    suspend fun saveAuthResult(result: UserEntity?) {
        UserPreferences.saveAuthResult(context, result)
    }

    fun addFiatCryptoRampTransaction(transaction: IFiatCryptoRampTransactionsDto) {
        UserPreferences.addFiatCryptoRampTransaction(context, transaction)
    }

    fun isNotificationsDismissed(): Boolean = UserPreferences.isNotificationsDismissed(context)

    fun setNotificationsDismissed(dismissed: Boolean) {
        UserPreferences.setNotificationsDismissed(context, dismissed)
    }

    fun areNotificationsEnabled(): Boolean? {
        // Check persisted state first
        val hasEnabled = UserPreferences.hasEnabledNotifications(context)
        if (hasEnabled != null) {
            return hasEnabled
        }
        // Fallback to system check if not set
        val enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        return enabled
    }

//    fun areNotificationsEnabled(): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ContextCompat.checkSelfPermission(
//                context, Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//        } else {
//            NotificationManagerCompat.from(context).areNotificationsEnabled()
//        }
//    }

    fun setNotificationsEnabled(enabled: Boolean) {
        UserPreferences.setHasEnabledNotifications(context, enabled)
    }

    suspend fun logout() {
        UserPreferences.clearToken(context)
        UserPreferences.clearAuthResult(context)
        walletPreferences.clearWalletCache()
    }
}
