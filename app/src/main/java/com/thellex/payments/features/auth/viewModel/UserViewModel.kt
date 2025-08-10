package com.thellex.payments.features.auth.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.data.model.IBankAccountDto
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.INotificationConsumeDto
import com.thellex.payments.data.model.ITransactionHistoryDto
import com.thellex.payments.data.model.KycResponseDto
import com.thellex.payments.data.model.KycValidateBvnResponse
import com.thellex.payments.data.model.UserEntity
import com.thellex.payments.data.model.UserPreferences
import kotlinx.coroutines.launch
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
    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        viewModelScope.launch {
            try {
                repository.getAuthResult().collect { user ->
                    _authResult.postValue(user)
                    Log.d(TAG, "Initialized auth result: ${user?.fiatCryptoRampTransactions?.map { it.id }}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize auth result: ${e.message}", e)
                _error.postValue("Failed to load user data: ${e.message}")
            }
        }
    }

    fun saveToken(token: String?) {
        viewModelScope.launch {
            try {
                repository.saveToken(token)
                Log.d(TAG, "Saved token: $token")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save token: ${e.message}", e)
                _error.postValue("Failed to save token: ${e.message}")
            }
        }
    }

    fun getToken(): String? = token.value

    fun saveExpiresAt(timeString: String) {
        try {
            prefs.edit().putString("expiresAt", timeString).apply()
            Log.d(TAG, "Saved expiresAt: $timeString")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save expiresAt: ${e.message}", e)
            _error.postValue("Failed to save expiresAt: ${e.message}")
        }
    }

    fun saveAuthResult(result: UserEntity?) {
        viewModelScope.launch {
            try {
                repository.saveAuthResult(result)
                Log.d(TAG, "Saved auth result: ${result?.fiatCryptoRampTransactions?.map { it.id }}")
                _authResult.postValue(result) // Ensure UI updates
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save auth result: ${e.message}", e)
                _error.postValue("Failed to save user data: ${e.message}")
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
                Log.e(TAG, "refreshAuthResult failed: ${e.message}", e)
                _error.postValue("Failed to refresh user data: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
                _authResult.postValue(null)
                ActivityTracker.finishAll()
                Log.d(TAG, "Logged out successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Logout failed: ${e.message}", e)
                _error.postValue("Logout failed: ${e.message}")
            }
        }
    }

    fun addFiatCryptoRampTransaction(transaction: IFiatCryptoRampTransactionsDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser == null) {
                    Log.w(TAG, "Cannot add fiat-crypto ramp transaction - user is null")
                    _error.postValue("Cannot add transaction: User not logged in")
                    return@launch
                }

                val updatedList = currentUser.fiatCryptoRampTransactions.toMutableList()
                if (updatedList.any { it.id == transaction.id }) {
                    Log.w(TAG, "Fiat-crypto ramp transaction already exists: ${transaction.id}")
                    return@launch
                }

                updatedList.add(transaction)
                val sortedList = updatedList.sortedByDescending { it.createdAt }
                val updatedUser = currentUser.copy(fiatCryptoRampTransactions = sortedList)
                Log.d(TAG, "Added fiat-crypto ramp transaction: ${transaction.id}, Updated list: $sortedList")

                _authResult.postValue(updatedUser)
                saveAuthResult(updatedUser)

                try {
                    repository.addFiatCryptoRampTransaction(transaction)
                    Log.d(TAG, "Successfully synced fiat-crypto ramp transaction: ${transaction.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync fiat-crypto ramp transaction ${transaction.id}: ${e.message}", e)
                    _error.postValue("Failed to sync transaction ${transaction.id}: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add fiat-crypto ramp transaction: ${e.message}", e)
                _error.postValue("Failed to add transaction: ${e.message}")
            }
        }
    }

    fun addTransaction(transaction: ITransactionHistoryDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser == null) {
                    Log.w(TAG, "Cannot add transaction - user is null")
                    _error.postValue("Cannot add transaction: User not logged in")
                    return@launch
                }

                val updatedList = currentUser.transactionHistory.toMutableList()
                if (updatedList.any { it.id == transaction.id }) {
                    Log.w(TAG, "Transaction already exists: ${transaction.id}")
                    return@launch
                }

                updatedList.add(transaction)
                val sortedList = updatedList.sortedByDescending { it.createdAt }
                val updatedUser = currentUser.copy(transactionHistory = sortedList)
                Log.d(TAG, "Added transaction: ${transaction.id}, Updated list: $sortedList")

                _authResult.postValue(updatedUser)
                saveAuthResult(updatedUser)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add transaction: ${e.message}", e)
                _error.postValue("Failed to add transaction: ${e.message}")
            }
        }
    }

    fun updateTransaction(updatedTransaction: ITransactionHistoryDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser == null) {
                    Log.w(TAG, "Cannot update transaction - user is null")
                    _error.postValue("Cannot update transaction: User not logged in")
                    return@launch
                }

                val updatedTransactions = currentUser.transactionHistory.map {
                    if (it.id == updatedTransaction.id) updatedTransaction else it
                }
                val updatedUser = currentUser.copy(transactionHistory = updatedTransactions)
                Log.d(TAG, "Updated transaction: ${updatedTransaction.id}")

                _authResult.postValue(updatedUser)
                saveAuthResult(updatedUser)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update transaction: ${e.message}", e)
                _error.postValue("Failed to update transaction: ${e.message}")
            }
        }
    }

    fun updateUserWithKycResult(result: KycResponseDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser == null) {
                    Log.w(TAG, "Cannot update KYC - user is null")
                    _error.postValue("Cannot update KYC: User not logged in")
                    return@launch
                }

                val updatedUser = currentUser.copy(
                    currentTier = result.currentTier,
                    nextTier = result.nextTier,
                    outstandingKyc = result.outstandingKyc,
                    remainingTiers = result.remainingTiers
                )
                Log.d(TAG, "Updated KYC: currentTier=${result.currentTier}, outstandingKyc=${result.outstandingKyc}")

                _authResult.postValue(updatedUser)
                saveAuthResult(updatedUser)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update KYC: ${e.message}", e)
                _error.postValue("Failed to update KYC: ${e.message}")
            }
        }
    }

    fun updateUserWithBvnResult(result: KycValidateBvnResponse) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser == null) {
                    Log.w(TAG, "Cannot update outstandingKyc - user is null")
                    _error.postValue("Cannot update BVN: User not logged in")
                    return@launch
                }

                if (!result.isValid) {
                    Log.w(TAG, "BVN validation failed: $result")
                    _error.postValue("BVN validation failed")
                    return@launch
                }

                val updatedOutStandingKyc = currentUser.outstandingKyc.filter { it != "BVN" }
                val updatedUser = currentUser.copy(outstandingKyc = updatedOutStandingKyc)
                Log.d(TAG, "Updated outstandingKyc: $updatedOutStandingKyc, from payload: $result")

                _authResult.postValue(updatedUser)
                saveAuthResult(updatedUser)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user with BVN result: ${e.message}", e)
                _error.postValue("Failed to update BVN: ${e.message}")
            }
        }
    }

    fun updateNotificationConsumed(result: INotificationConsumeDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser == null) {
                    Log.w(TAG, "Cannot update notification - user is null")
                    _error.postValue("Cannot update notification: User not logged in")
                    return@launch
                }

                val updatedList = currentUser.notifications.map {
                    if (it.id == result.id) it.copy(consumed = result.consumed, kind = result.kind) else it
                }
                val updatedUser = currentUser.copy(notifications = updatedList)
                Log.d(TAG, "Updated notification: ${result.id}, consumed=${result.consumed}")

                _authResult.postValue(updatedUser)
                saveAuthResult(updatedUser)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update notification consumed: ${e.message}", e)
                _error.postValue("Failed to update notification: ${e.message}")
            }
        }
    }

    fun addBankAccountToUser(newBankAccount: IBankAccountDto) {
        viewModelScope.launch {
            try {
                val currentUser = _authResult.value
                if (currentUser == null) {
                    Log.w(TAG, "Cannot add bank account - user is null")
                    _error.postValue("Cannot add bank account: User not logged in")
                    return@launch
                }

                val updatedBankAccounts = currentUser.bankAccounts.toMutableList().apply {
                    add(newBankAccount)
                }
                val updatedUser = currentUser.copy(bankAccounts = updatedBankAccounts)

                _authResult.postValue(updatedUser)
                saveAuthResult(updatedUser)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add bank account: ${e.message}", e)
                _error.postValue("Failed to add bank account: ${e.message}")
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
        val isValid = expiresAtInstant > nowInstant
        Log.d(TAG, "Verification code valid: $isValid, expiresAt=$expiresAtInstant, now=$nowInstant")
        return isValid
    }

    fun isNotificationsDismissed(): Boolean = repository.isNotificationsDismissed()

    fun setNotificationsDismissed(dismissed: Boolean) {
        repository.setNotificationsDismissed(dismissed)
        Log.d(TAG, "Set notifications dismissed: $dismissed")
    }

    fun refreshNotificationsStatus() {
        val areEnabled = repository.areNotificationsEnabled() ?: false
        _notificationsEnabled.postValue(areEnabled)
        Log.d(TAG, "Notifications status refreshed: $areEnabled")
    }
}