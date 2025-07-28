package com.thellex.payments.features.fiat

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.copyToClipboard
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.databinding.ActivityFiatDepositBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import java.time.Instant
import java.time.format.DateTimeParseException

class FiatDepositActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private lateinit var binding: ActivityFiatDepositBinding

    private var countdownTimer: CountDownTimer? = null  // <-- Add this to manage timer lifecycle

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiatDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemUI()
        setupViewModels()

        val result = getRampResultFromIntent()
        if (result == null) {
            showErrorAndFinish("Failed to load transaction details")
            return
        }

        setupUI(result)
        startExpiryCountdown(result.expiresAt)
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel() // Cancel timer to avoid leaks
    }

    private fun setupSystemUI() {
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()
    }

    private fun setupViewModels() {
        userViewModel = createViewModel(UserViewModelFactory(applicationContext), UserViewModel::class.java)
        walletManagerViewModel = createViewModel(WalletManagerModelFactory(applicationContext), WalletManagerViewModel::class.java)
    }

    private fun <T : ViewModel> createViewModel(factory: ViewModelProvider.Factory, clazz: Class<T>): T {
        return ViewModelProvider(this, factory)[clazz]
    }

    private fun getRampResultFromIntent(): IFiatCryptoRampTransactionsDto? {
        val jsonResult = intent.getStringExtra(FIAT_CRYPTO_RAMP_RESULT_JSON)
        return jsonResult?.let { Gson().fromJson(it, IFiatCryptoRampTransactionsDto::class.java) }
    }

    private fun showErrorAndFinish(message: String) {
        CustomToast.show(this, "Error", message)
        finish()
    }

    private fun setupUI(result: IFiatCryptoRampTransactionsDto) {
        binding.buttonBack.setOnClickListener { finish() }

        val accountDetails = result.bankInfo

        binding.accountNumber.text = accountDetails.accountNumber ?: "N/A"
        binding.accountName.text = accountDetails.accountHolder ?: "N/A"
        binding.bankName.text = accountDetails.bankName ?: "N/A"

        binding.copyAccountDetails.setOnClickListener {
            val accountNumber = accountDetails.accountNumber
            if (!accountNumber.isNullOrEmpty()) {
                copyToClipboard("Account Number", accountNumber)
            } else {
                CustomToast.show(this, "Empty", "No account number to copy")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startExpiryCountdown(expiresAt: String?) {
        if (expiresAt.isNullOrEmpty()) {
            binding.textCountdownTimer.text = "No expiry info"
            return
        }

        try {
            val expiryInstant = Instant.parse(expiresAt)
            val nowInstant = Instant.now()

            val millisUntilExpiry = expiryInstant.toEpochMilli() - nowInstant.toEpochMilli()
            if (millisUntilExpiry <= 0) {
                binding.textCountdownTimer.text = "Expired"
                return
            }

            countdownTimer?.cancel()

            countdownTimer = object : CountDownTimer(millisUntilExpiry, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val totalSeconds = millisUntilFinished / 1000
                    val hours = totalSeconds / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60

                    val timeString = if (hours > 0) {
                        String.format("%d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        String.format("%d:%02d", minutes, seconds)
                    }

                    binding.textCountdownTimer.text = timeString
                }

                override fun onFinish() {
                    binding.textCountdownTimer.text = "Expired"
                }
            }
            countdownTimer?.start()

        } catch (e: DateTimeParseException) {
            binding.textCountdownTimer.text = "Invalid expiry date"
            Log.e(TAG, "Failed to parse expiresAt date", e)
        }
    }

    companion object {
        private const val TAG = "TAGY"
        private const val FIAT_CRYPTO_RAMP_RESULT_JSON = "fiatCryptoRampResultJson"
    }
}
