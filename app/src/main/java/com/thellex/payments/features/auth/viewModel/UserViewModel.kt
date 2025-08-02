package com.thellex.payments.features.auth.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.data.model.IBankAccountDto
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.INotificationConsumeDto
import com.thellex.payments.data.model.ITransactionHistoryDto
import com.thellex.payments.data.model.KycResponseDto
import com.thellex.payments.data.model.UserEntity
import com.thellex.payments.data.model.UserPreferences
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class UserViewModel(application: Context) : AndroidViewModel(application as Application) {
    companion object {
        private const val TAG = "UserViewModel"
    }

    private val repository = UserRepository.getInstance(application)
    private val prefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    val token: LiveData<String?> = repository.getToken().asLiveData()

    private val _authResult = MutableLiveData<UserEntity?>()
    val authResult: LiveData<UserEntity?> = _authResult

    init {
        viewModelScope.launch {
            repository.getAuthResult().collect { user ->
                _authResult.postValue(user)
            }
        }
    }

    fun saveToken(token: String?) {
        viewModelScope.launch {
            try {
                repository.saveToken(token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save token", e)
            }
        }
    }

    fun getToken(): String? {
        return token.value
    }

    fun saveExpiresAt(timeString: String) {
        try {
            prefs.edit().putString("expiresAt", timeString).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save expiresAt", e)
        }
    }

    fun saveAuthResult(result: UserEntity?) {
        viewModelScope.launch {
            try {
                repository.saveAuthResult(result)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save auth result", e)
            }
        }
    }

    fun refreshAuthResult(context: Context) {
        viewModelScope.launch {
            try {
                val latestUser = UserPreferences.getAuthResultSync(context)
                _authResult.postValue(latestUser)
                Log.d(TAG, "Refreshed auth result: ${latestUser?.fiatCryptoRampTransactions?.map { it.id }}")
            } catch (e: Exception) {
                Log.e(TAG, "refreshAuthResult failed", e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
                _authResult.postValue(null)
                ActivityTracker.finishAll()
            } catch (e: Exception) {
                Log.e(TAG, "Logout failed", e)
            }
        }
    }

    fun addFiatCryptoRampTransaction(transaction: IFiatCryptoRampTransactionsDto) {
        viewModelScope.launch {
            try {
                repository.addFiatCryptoRampTransaction(transaction)
                Log.d(TAG, "Added fiat crypto ramp transaction: ${transaction.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add fiat crypto ramp transaction", e)
            }
        }
    }

    fun addTransaction(transaction: ITransactionHistoryDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser != null) {
                    val updatedList = currentUser.transactionHistory.toMutableList()
                    if (updatedList.isEmpty() || !updatedList.any { it.id == transaction.id }) {
                        updatedList.add(transaction)
                    } else {
                        Log.w(TAG, "Transaction already exists: ${transaction.blockchainTxId}")
                    }
                    val sortedList = updatedList.sortedByDescending { it.createdAt }
                    val updatedUser = currentUser.copy(transactionHistory = sortedList)
                    saveAuthResult(updatedUser)
                } else {
                    Log.w(TAG, "Cannot add transaction - user is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add transaction", e)
            }
        }
    }

    fun updateTransaction(updatedTransaction: ITransactionHistoryDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser != null) {
                    val updatedTransactions = currentUser.transactionHistory.map {
                        if (it.transactionId == updatedTransaction.transactionId) updatedTransaction else it
                    }
                    val updatedUser = currentUser.copy(transactionHistory = updatedTransactions)
                    saveAuthResult(updatedUser)
                } else {
                    Log.w(TAG, "Cannot update transaction - user is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update transaction", e)
            }
        }
    }

    fun updateUserWithKycResult(result: KycResponseDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        currentTier = result.currentTier,
                        nextTier = result.nextTier,
                        outstandingKyc = result.outstandingKyc,
                        remainingTiers = result.remainingTiers
                    )
                    saveAuthResult(updatedUser)
                } else {
                    Log.w(TAG, "Cannot update KYC - user is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update KYC", e)
            }
        }
    }

    fun updateNotificationConsumed(result: INotificationConsumeDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser != null) {
                    val updatedList = currentUser.notifications.map {
                        if (it.id == result.id) it.copy(consumed = result.consumed, kind = result.kind) else it
                    }
                    val updatedUser = currentUser.copy(notifications = updatedList)
                    Log.d(TAG, "Updated notification: $updatedList, from payload: $result")
                    saveAuthResult(updatedUser)
                } else {
                    Log.w(TAG, "Cannot update notification - user is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update notification consumed", e)
            }
        }
    }

    fun addBankAccountToUser(newBankAccount: IBankAccountDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser != null) {
                    val updatedBankAccounts = currentUser.bankAccounts.toMutableList().apply {
                        add(newBankAccount)
                    }
                    val updatedUser = currentUser.copy(bankAccounts = updatedBankAccounts)
                    saveAuthResult(updatedUser)
                } else {
                    Log.w(TAG, "Cannot add bank account - user is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add bank account", e)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getExpiresAt(): Instant? {
        val timeString = prefs.getString("expiresAt", null) ?: return null
        return try {
            Instant.parse(timeString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse expiresAt", e)
            null
        }
    }

    @OptIn(ExperimentalTime::class)
    fun isVerificationCodeValid(): Boolean {
        val expiresAtInstant = getExpiresAt() ?: return false
        val nowInstant = Clock.System.now()
        return expiresAtInstant > nowInstant
    }
}
