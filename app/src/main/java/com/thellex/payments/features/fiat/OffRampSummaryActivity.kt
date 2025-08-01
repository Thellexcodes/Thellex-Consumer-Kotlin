package com.thellex.payments.features.fiat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.capitalizeFirst
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.roundToTwoDecimals
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.CryptoToFiatOffRampRequestDto
import com.thellex.payments.data.model.IBankInfoRequestDto
import com.thellex.payments.databinding.ActivityRampSummaryBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.fiat.model.CryptoToFiatViewModel
import com.thellex.payments.features.pos.ui.POSHomeActivity
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.settings.FiatEnum
import com.thellex.payments.settings.FiatTickers
import com.thellex.payments.settings.SupportedBlockchainEnum
import com.thellex.payments.settings.TokensEnum
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.math.BigDecimal

@RequiresApi(Build.VERSION_CODES.O)
class OffRampSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRampSummaryBinding
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var cryptoToFiatViewModel: CryptoToFiatViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRampSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.rampSummaryRootLayout.applyAdvancedSystemBarInsets()

        setupTopBar()
        setupViewModels()
        setupListeners()
        setupTransactionSummary()
        observeLiveUpdates()
    }

    private fun setupTopBar() {
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.offramp_top_app_bar),
            title = "SUMMARY"
        )
    }

    private fun setupViewModels() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        val parentActivity = findActivity(CryptoToFiatOffRampActivity::class.java)
        if (parentActivity == null) {
            startActivity(Intent(this, CryptoToFiatOffRampActivity::class.java))
            finish()
            return
        }

        cryptoToFiatViewModel = ViewModelProvider(
            parentActivity,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CryptoToFiatViewModel::class.java]
    }

    private fun setupListeners() {
        binding.offrampSubmitButton.setOnClickListener {
            makeOffRampRequest()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTransactionSummary() = with(binding.offrampTransactionDetails) {
        val viewModel = cryptoToFiatViewModel

        rampTransactionReasonValue.text = viewModel.paymentReason.value?.uppercase().orEmpty()
        rampBlockchainNetworkName.text = viewModel.network.value?.uppercase().orEmpty()
        rampBlockchainNetworkIcon.setImageResource(
            Helpers.getIconResIdForBlockchain(viewModel.network.value.orEmpty())
        )

        viewModel.assetCode.value?.let { assetCode ->

            viewModel.currentRate.value?.buy?.let { rate ->
                val fiatCode = FiatTickers.getByCodeOrCountry("ngn")?.currencyCode.orEmpty()
                rampExchangeRateValue.text = "$rate $fiatCode/${assetCode.uppercase()}"
            }
        }
    }

    private fun observeLiveUpdates() {
        cryptoToFiatViewModel.fiatAmount.observe(this) {
            updateFiatAndCryptoAmount(it)
        }

        cryptoToFiatViewModel.bankInfo.observe(this) {
            updateBankInfo(it)
        }

        cryptoToFiatViewModel.currentRate.observe(this) {
            setupTransactionSummary()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateFiatAndCryptoAmount(amount: Double?) {
        val fiatCode = cryptoToFiatViewModel.fiatCode.value ?: "NGN"
        val rate = cryptoToFiatViewModel.currentRate.value
        val fee = cryptoToFiatViewModel.fee.value ?: 0.0
        val feeDivisor = rate?.feeDivisor ?: 100.0  // Fallback to 100 if feeDivisor is not available

        if (amount != null && amount > 0.0) {
            binding.offrampFiatAmount.text = "${amount.roundToTwoDecimals()} $fiatCode"

            val cryptoAmount = if (rate != null) {
                (amount / (rate.buy * (1.0 - (fee / feeDivisor)))).roundToTwoDecimals()
            } else {
                BigDecimal.ZERO
            }

            val assetCode = cryptoToFiatViewModel.assetCode.value?.uppercase()
            binding.offrampUsdAmount.text = "$cryptoAmount $assetCode"
        }
    }

    private fun updateBankInfo(bank: IBankInfoRequestDto?) = with(binding.offrampBankAccountInfo) {
        rampBankAccountNumber.text = bank?.accountNumber ?: "N/A"
        rampBankName.text = bank?.bankName?.capitalizeFirst() ?: "N/A"
        rampAccountHolderName.text = bank?.accountHolder ?: "N/A"
    }

    private fun makeOffRampRequest() {
        binding.offrampSubmitButton.setSubmitting(true)

        lifecycleScope.launch {
            try {
                val token = withTimeoutOrNull(5000) {
                    userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                }

                if (token.isNullOrBlank()) {
                    CustomToast.show(this@OffRampSummaryActivity, "Authentication Error", "Token not available.")
                    return@launch
                }

                val request = buildOffRampRequest() ?: return@launch

                val response = ApiClient.getAuthenticatedPaymentApi(token).cryptoToFiatOffRamp(request)

                response.body()?.result?.let { result ->
                    userViewModel.addFiatCryptoRampTransaction(result)
                    ActivityTracker.finishActivity(PaymentMethodActivity::class.java)
                    startActivity(Intent(this@OffRampSummaryActivity, POSHomeActivity::class.java))
                } ?: CustomToast.show(this@OffRampSummaryActivity, "Error", "Unexpected response")

            } catch (e: Exception) {
                Log.e("RampSummaryActivity", "Exception occurred: ${e.message}", e)
                CustomToast.show(this@OffRampSummaryActivity, "Error", "An unexpected error occurred.")
            }
        }
    }

    private fun buildOffRampRequest(): CryptoToFiatOffRampRequestDto? {
        val vm = cryptoToFiatViewModel

        val reason = vm.paymentReason.value
        val network = vm.network.value
        val source = vm.sourceAddress.value
        val asset = vm.assetCode.value
        val country = vm.country.value
        val fiat = vm.fiatCode.value
        val amount = vm.fiatAmount.value
        val bank = vm.bankInfo.value
        val cryptoAmount = vm.mainAssetAmount.value

        if (listOf(reason, network, source, asset, country, fiat).any { it.isNullOrBlank() } || amount == null || bank == null) {
            CustomToast.show(this, "Missing Data", "Please complete all fields.")
            return null
        }

        val fiatEnum = FiatEnum.fromCode(fiat!!)
        val blockchainEnum = SupportedBlockchainEnum.fromValue(network!!)
        val tokenEnum = runCatching { TokensEnum.valueOf(asset!!.lowercase()) }.getOrNull()

        if (fiatEnum == null || blockchainEnum == null || tokenEnum == null) {
            CustomToast.show(this, "Invalid Input", "Currency or network format is invalid.")
            return null
        }

        return CryptoToFiatOffRampRequestDto(
            paymentReason = reason!!,
            network = blockchainEnum,
            sourceAddress = source!!,
            assetCode = tokenEnum,
            country = country!!,
            fiatCode = fiatEnum,
            userAmount = amount,
            bankInfo = bank,
            mainAssetAmount = cryptoAmount
        )
    }

    private fun findActivity(activityClass: Class<out AppCompatActivity>): AppCompatActivity? {
        return ActivityTracker.getActivities().find { it::class.java == activityClass } as? AppCompatActivity
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityTracker.remove(this)
    }
}
