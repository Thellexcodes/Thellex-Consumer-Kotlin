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
import com.thellex.payments.core.utils.EventBus
import com.thellex.payments.data.model.*
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class UserViewModel(application: Context) : AndroidViewModel(application as Application) {
    companion object {
        private const val TAG = "UserViewModel"
        private const val PREFS_NAME = "user_prefs"
    }

    private val repository = UserRepository.getInstance(application)
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val token: LiveData<String?> = repository.getToken().asLiveData()
    private val _authResult = MutableLiveData<UserEntity?>()
    val authResult: LiveData<UserEntity?> = _authResult
    private val _adminData = MutableLiveData<AdminData?>()
    val adminData : LiveData<AdminData?> = _adminData
    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    private val _selectedTab = MutableLiveData<Int>(0)
    val selectedTab: LiveData<Int> get() = _selectedTab
    private val _depositTransactions = MutableLiveData<List<ITransactionHistoryDto>>(emptyList())
    val depositTransactions: LiveData<List<ITransactionHistoryDto>> = _depositTransactions
    private val _withdrawalTransactions = MutableLiveData<List<ITransactionHistoryDto>>(emptyList())
    val withdrawalTransactions: LiveData<List<ITransactionHistoryDto>> = _withdrawalTransactions

    init {
        viewModelScope.launch {
            try {
                repository.getAuthResult().collect { user ->
                    _authResult.postValue(user)
                    updateFilteredTransactions(user)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize auth result: ${e.message}", e)
                _error.postValue("Failed to load user data")
            }
//            viewModelScope.launch {
//                try {
//                    repository.getAdminData().collect { admin ->
//                        Log.d(TAG, "Initialized admin data: ${admin?.rampTransactions?.data?.map { it.txnID }}")
//                        _adminData.postValue(admin)
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "Failed to initialize admin data: ${e.message}", e)
//                    _error.postValue("Failed to load admin data")
//                }
//            }
        }
        // Observe EventBus for transaction updates
        EventBus.transactionUpdate.observeForever { transaction ->
            viewModelScope.launch {
                updateTransactionHistory(transaction)
            }
        }

        EventBus.fiatCryptoTransactionUpdate.observeForever { transaction ->
            viewModelScope.launch {
                addFiatCryptoRampTransaction(transaction)
                Log.d(TAG, "Processed EventBus fiat-crypto transaction update: ${transaction.id}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.transactionUpdate.removeObserver { /* No-op */ }
        EventBus.fiatCryptoTransactionUpdate.removeObserver { /* No-op */ }
    }

    private fun updateFilteredTransactions(user: UserEntity?) {
        val transactions = user?.transactionHistory ?: emptyList()
        _depositTransactions.postValue(transactions
            .filter {
                it.transactionType in listOf(
                    TransactionTypeEnum.CRYPTO_TO_FIAT_DEPOSIT,
                    TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT,
                    TransactionTypeEnum.CRYPTO_DEPOSIT
                )
            }
            .sortedByDescending { it.createdAt })
        _withdrawalTransactions.postValue(transactions
            .filter {
                it.transactionType in listOf(
                    TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL,
                    TransactionTypeEnum.FIAT_TO_CRYPTO_WITHDRAWAL,
                    TransactionTypeEnum.CRYPTO_WITHDRAWAL
                )
            }
            .sortedByDescending { it.createdAt })
    }

    private fun updateTransactionHistory(transaction: ITransactionHistoryDto) {
        val user = _authResult.value ?: run {
            Log.w(TAG, "Cannot update transaction - user is null")
            _error.postValue("Cannot update transaction: User not logged in")
            return
        }
        val updatedList = (user.transactionHistory ?: emptyList()).toMutableList()
        val existingIndex = updatedList.indexOfFirst { it.id == transaction.id }
        if (existingIndex >= 0) {
            updatedList[existingIndex] = transaction
            Log.d(TAG, "Updated transaction: ${transaction.id}")
        } else {
            updatedList.add(transaction)
            Log.d(TAG, "Added transaction: ${transaction.id}")
        }
        val updatedUser = user.copy(transactionHistory = updatedList.sortedByDescending { it.createdAt })
        _authResult.postValue(updatedUser)
        viewModelScope.launch {
            try {
                repository.saveAuthResult(updatedUser)
                Log.d(TAG, "Saved updated user with transaction: ${transaction.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save updated user: ${e.message}", e)
                _error.postValue("Failed to save transaction")
            }
        }
        updateFilteredTransactions(updatedUser)
    }

    fun saveToken(token: String?) {
        viewModelScope.launch {
            try {
                repository.saveToken(token)
                Log.d(TAG, "Saved token: $token")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save token: ${e.message}", e)
                _error.postValue("Failed to save token")
            }
        }
    }

    fun getToken(): String? = token.value

    fun saveExpiresAt(timeString: String) {
        viewModelScope.launch {
            try {
                prefs.edit().putString("expiresAt", timeString).apply()
                Log.d(TAG, "Saved expiresAt: $timeString")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save expiresAt: ${e.message}", e)
                _error.postValue("Failed to save expiresAt")
            }
        }
    }

    fun saveAuthResult(result: UserEntity?) {
        viewModelScope.launch {
            try {
                repository.saveAuthResult(result)
                _authResult.postValue(result)
                updateFilteredTransactions(result)
                Log.d(TAG, "Saved auth result: ${result?.fiatCryptoRampTransactions?.map { it.id }}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save auth result: ${e.message}", e)
                _error.postValue("Failed to save user data")
            }
        }
    }

    fun saveAdminResult(result: AdminData?) {
        viewModelScope.launch {
            try {
                repository.saveAdminResult(result)
                _adminData.postValue(result)
                Log.d(TAG, "Saved admin result")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save ramp transactions result: ${e.message}", e)
                _error.postValue("Failed to save user data")
            }
        }
    }

    fun refreshAuthResult(context: Context) {
        viewModelScope.launch {
            try {
                val latestUser = UserPreferences.getAuthResultSync(context)
                _authResult.postValue(latestUser)
                updateFilteredTransactions(latestUser)
                Log.d(TAG, "Refreshed auth result: ${latestUser?.fiatCryptoRampTransactions?.map { it.id }}")
            } catch (e: Exception) {
                Log.e(TAG, "refreshAuthResult failed: ${e.message}", e)
                _error.postValue("Failed to refresh user data")
            }
        }
    }

    fun addFiatCryptoRampTransaction(transaction: IFiatCryptoRampTransactionsDto) {
        viewModelScope.launch {
            try {
                val user = _authResult.value ?: run {
                    Log.w(TAG, "Cannot add fiat-crypto ramp transaction - user is null")
                    _error.postValue("Cannot add transaction: User not logged in")
                    return@launch
                }

                val updatedList = user.fiatCryptoRampTransactions?.toMutableList()
                if (updatedList != null) {
                    if (updatedList.any { it.id == transaction.id }) {
                        Log.w(TAG, "Fiat-crypto ramp transaction already exists: ${transaction.id}")
                        return@launch
                    }
                }

                updatedList?.add(transaction)
                val updatedUser = user.copy(fiatCryptoRampTransactions = updatedList)

                // Save to repository and verify
                try {
                    repository.saveAuthResult(updatedUser)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save auth result for transaction: ${transaction.id}, error: ${e.message}", e)
                    _error.postValue("Failed to save transaction: Repository error")
                    return@launch
                }

                // Add transaction to repository
                try {
                    repository.addFiatCryptoRampTransaction(transaction)
                    Log.d(TAG, "Successfully called addFiatCryptoRampTransaction for: ${transaction.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add fiat-crypto ramp transaction to repository: ${transaction.id}, error: ${e.message}", e)
                    _error.postValue("Failed to save transaction: Repository error")
                    return@launch
                }

                // Update filtered transactions
                updateFilteredTransactions(updatedUser)

                // Post to LiveData
                _authResult.postValue(updatedUser)
                Log.d(TAG, "Added fiat-crypto ramp transaction: ${transaction.id}")

                // Post to EventBus for fiat-crypto transaction
                EventBus.postFiatCryptoTransactionUpdate(transaction)
                Log.d(TAG, "Posted fiat-crypto transaction to EventBus: ${transaction.id}")

                // Handle associated transaction history
                transaction.transaction?.let { historyTransaction ->
                    EventBus.postTransactionUpdate(historyTransaction)
                    Log.d(TAG, "Posted associated transaction history to EventBus: ${historyTransaction.id}")
                }

                // Log final user state
                Log.d(TAG, "Updated user state, fiatCryptoRampTransactions count: ${updatedUser.fiatCryptoRampTransactions?.size}")
                updatedUser.fiatCryptoRampTransactions?.forEachIndexed { index, txn ->
                    Log.d(
                        TAG,
                        "Post-save Transaction [$index]: id=${txn.id}, type=${txn.transactionType?.value ?: "Unknown"}, " +
                                "status=${txn.paymentStatus?.toString() ?: "Unknown"}, amount=${txn.netCryptoAmount ?: txn.mainAssetAmount ?: "Unknown"}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add fiat-crypto ramp transaction: ${transaction.id}, error: ${e.message}", e)
                _error.postValue("Failed to add transaction")
            }
        }
    }

    fun addTransaction(transaction: ITransactionHistoryDto) {
        updateTransactionHistory(transaction)
    }

    fun updateUserWithKycResult(result: KycResponseDto) {
        viewModelScope.launch {
            try {
                val user = _authResult.value ?: run {
                    Log.w(TAG, "Cannot update KYC - user is null")
                    _error.postValue("Cannot update KYC: User not logged in")
                    return@launch
                }
                val updatedUser = user.copy(
                    currentTier = result.currentTier,
                    nextTier = result.nextTier,
                    outstandingKyc = result.outstandingKyc,
                    remainingTiers = result.remainingTiers,
                    banks = result.banks
                )
                _authResult.postValue(updatedUser)
                repository.saveAuthResult(updatedUser)
                Log.d(TAG, "Updated KYC: currentTier=${result.currentTier}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update KYC: ${e.message}", e)
                _error.postValue("Failed to update KYC")
            }
        }
    }

    fun updateUserWithBvnResult(result: KycValidateBvnResponse) {
        viewModelScope.launch {
            try {
                val user = _authResult.value ?: run {
                    Log.w(TAG, "Cannot update BVN - user is null")
                    _error.postValue("Cannot update BVN: User not logged in")
                    return@launch
                }
                if (!result.isValid) {
                    Log.w(TAG, "BVN validation failed")
                    _error.postValue("BVN validation failed")
                    return@launch
                }
                val updatedUser = user.copy(outstandingKyc = user.outstandingKyc?.filter { it != "BVN" })
                _authResult.postValue(updatedUser)
                repository.saveAuthResult(updatedUser)
                Log.d(TAG, "Updated BVN")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update BVN: ${e.message}", e)
                _error.postValue("Failed to update BVN")
            }
        }
    }

    fun updateNotificationConsumed(result: INotificationConsumeDto) {
        viewModelScope.launch {
            try {
                val user = _authResult.value ?: run {
                    Log.w(TAG, "Cannot update notification - user is null")
                    _error.postValue("Cannot update notification: User not logged in")
                    return@launch
                }
                val updatedList = user.notifications?.map {
                    if (it.id == result.id) it.copy(consumed = result.consumed, kind = result.kind) else it
                }
                val updatedUser = user.copy(notifications = updatedList)
                _authResult.postValue(updatedUser)
                repository.saveAuthResult(updatedUser)
                Log.d(TAG, "Updated notification: ${result.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update notification: ${e.message}", e)
                _error.postValue("Failed to update notification")
            }
        }
    }

    fun addBankAccountToUser(newBankAccount: IBankAccountDto) {
        viewModelScope.launch {
            try {
                val user = _authResult.value ?: run {
                    Log.w(TAG, "Cannot add bank account - user is null")
                    _error.postValue("Cannot add bank account: User not logged in")
                    return@launch
                }
                val updatedBankAccounts = user.bankAccounts?.toMutableList()?.apply { add(newBankAccount) }
                val updatedUser = user.copy(bankAccounts = updatedBankAccounts)
                _authResult.postValue(updatedUser)
                repository.saveAuthResult(updatedUser)
                Log.d(TAG, "Added bank account")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add bank account: ${e.message}", e)
                _error.postValue("Failed to add bank account")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getExpiresAt(): Instant? = viewModelScope.run {
        val timeString = prefs.getString("expiresAt", null) ?: return null
        try {
            Instant.parse(timeString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse expiresAt", e)
            null
        }
    }

    @OptIn(ExperimentalTime::class)
    fun isVerificationCodeValid(): Boolean = viewModelScope.run {
        val expiresAtInstant = getExpiresAt() ?: return false
        val nowInstant = Clock.System.now()
        val isValid = expiresAtInstant > nowInstant
        Log.d(TAG, "Verification code valid: $isValid")
        isValid
    }

    fun isNotificationsDismissed(): Boolean = viewModelScope.run {
        repository.isNotificationsDismissed()
    }

    fun setNotificationsDismissed(dismissed: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsDismissed(dismissed)
            Log.d(TAG, "Set notifications dismissed: $dismissed")
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
            refreshNotificationsStatus()
            Log.d(TAG, "Set notifications enabled: $enabled")
        }
    }

    fun refreshNotificationsStatus() {
        viewModelScope.launch {
            val areEnabled = repository.areNotificationsEnabled() ?: false
            _notificationsEnabled.postValue(areEnabled)
            Log.d(TAG, "Notifications status refreshed: $areEnabled")
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun logout() {
        viewModelScope.launch {
            try {

                repository.logout()
                _authResult.postValue(null)
                _depositTransactions.postValue(emptyList())
                _withdrawalTransactions.postValue(emptyList())
                ActivityTracker.finishAll()
                Log.d(TAG, "Logged out successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Logout failed: ${e.message}", e)
                _error.postValue("Logout failed")
            }
        }
    }
}