package com.thellex.payments.features.auth.viewModel

import android.annotation.SuppressLint
import android.content.Context
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
        result?.let {
            UserPreferences.saveAuthResultAsync(context, it)
        }
    }

    suspend fun logout() {
        UserPreferences.clearToken(context)
        UserPreferences.clearAuthResult(context)
        walletPreferences.clearWalletCache()
    }
}
