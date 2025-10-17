package com.thellex.pay.features.fiat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.pay.R
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.CustomToast
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.copyToClipboard
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.core.utils.Helpers.truncateToTwoDecimals
import com.thellex.pay.data.model.IBankInfoRequestDto
import com.thellex.pay.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.pay.databinding.ActivityFiatDepositBinding
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.pos.ui.POSHomeActivity
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
import com.thellex.pay.settings.FiatTickers
import java.time.Instant

class OnRampFiatSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFiatDepositBinding
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private var countdownTimer: CountDownTimer? = null

    companion object {
        private const val FIAT_CRYPTO_RAMP_RESULT_JSON = "fiatCryptoRampResultJson"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiatDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTopBar()
        configureSystemUI()
        initViewModels()

        getRampTransactionFromIntent()?.let {
            renderTransactionUI(it)
        } ?: finishWithError("Failed to load transaction details")

        binding.onrampGoToDashboardBtn.setOnClickListener{ finish()
            startActivity(Intent(this@OnRampFiatSummaryActivity, POSHomeActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        countdownTimer?.cancel()
        super.onDestroy()
    }

    // region Setup Methods

    private fun setupTopBar() {
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.onrampTopAppBar),
            title = "SUMMARY"
        )
    }

    private fun configureSystemUI() {
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.onrampDepositRoot.applyAdvancedSystemBarInsets()
    }

    private fun initViewModels() {
        userViewModel = createViewModel(UserViewModelFactory(applicationContext), UserViewModel::class.java)
        walletManagerViewModel = createViewModel(WalletManagerModelFactory(applicationContext), WalletManagerViewModel::class.java)
    }

    private fun <T : ViewModel> createViewModel(factory: ViewModelProvider.Factory, clazz: Class<T>): T {
        return ViewModelProvider(this, factory)[clazz]
    }

    // endregion

    // region Transaction Handling

    private fun getRampTransactionFromIntent(): IFiatCryptoRampTransactionsDto? {
        return intent.getStringExtra(FIAT_CRYPTO_RAMP_RESULT_JSON)
            ?.let { Gson().fromJson(it, IFiatCryptoRampTransactionsDto::class.java) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun renderTransactionUI(result: IFiatCryptoRampTransactionsDto) {
        displayBankAccountInfo(result.bankInfo)
        displayTransactionInfo(result)
        startCountdownTimer(result.expiresAt)
    }

    @SuppressLint("SetTextI18n")
    private fun displayTransactionInfo(result: IFiatCryptoRampTransactionsDto) {
        binding.onrampUsdAmount.text = "${result.netCryptoAmount.truncateToTwoDecimals().toString()} ${result.recipientInfo.assetCode.uppercase()}"
        binding.onrampFiatAmount.text = "${FiatTickers.getByCodeOrCountry("ngn")?.symbol}${result.mainFiatAmount}"

        binding.onrampTransactionDetails.run {
            rampServiceFeeValue.text =
                "${FiatTickers.getByCodeOrCountry("ngn")?.symbol}${result.serviceFeeAmountLocal} | " +
                        "${FiatTickers.getByCodeOrCountry("us")?.symbol}${result.serviceFeeAmountUSD}"

            rampExchangeRateValue.text = "${result.rate} ${FiatTickers.getByCodeOrCountry("ngn")?.currencyCode}/${result.recipientInfo.assetCode.uppercase()}"
            rampBlockchainNetworkName.text = result.recipientInfo.network.uppercase()
            rampBlockchainNetworkIcon.setImageResource(Helpers.getIconResIdForBlockchain(result.recipientInfo.network))
            rampTransactionReasonValue.text = result.paymentReason.uppercase()
        }
    }

    private fun displayBankAccountInfo(info: IBankInfoRequestDto?) {
        if (info == null) {
            finishWithError("Missing account details")
            return
        }

        with(binding.onrampBankAccountInfo) {
            rampBankName.text = info.bankName ?: "N/A"
            rampAccountHolderName.text = info.accountHolder ?: "N/A"
            rampBankAccountNumber.text = info.accountNumber ?: "N/A"

            rampCopyBankDetailsIcon.setOnClickListener {
                val number = info.accountNumber
                if (number.isNotEmpty()) {
                    copyToClipboard("Account Number", number)
                } else {
                    CustomToast.show(this@OnRampFiatSummaryActivity, "Empty", "No account number to copy")
                }
            }
        }
    }

    // endregion

    // region Countdown

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startCountdownTimer(expiresAt: String?) {
        if (expiresAt.isNullOrEmpty()) {
            setCountdownText("Expired")
            return
        }

        try {
            val millisUntilExpiry = Instant.parse(expiresAt).toEpochMilli() - Instant.now().toEpochMilli()

            if (millisUntilExpiry <= 0) {
                setCountdownText("Expired")
                return
            }

            countdownTimer = object : CountDownTimer(millisUntilExpiry, 1000) {
                override fun onTick(millis: Long) {
                    setCountdownText(formatTime(millis))
                }

                override fun onFinish() {
                    finish()
                }
            }.also { it.start() }

        } catch (e: Exception) {
            setCountdownText("Invalid expiry")
        }
    }

    private fun setCountdownText(text: String) {
        binding.onrampExpirationContainer.rampExpirationTimer.text = text
    }

    private fun formatTime(millis: Long): String {
        val totalSecs = millis / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60

        return if (hours > 0) "%d:%02d:%02d".format(hours, mins, secs)
        else "%d:%02d".format(mins, secs)
    }

    // endregion

    private fun finishWithError(message: String) {
        CustomToast.show(this, "Error", message)
        finish()
    }
}