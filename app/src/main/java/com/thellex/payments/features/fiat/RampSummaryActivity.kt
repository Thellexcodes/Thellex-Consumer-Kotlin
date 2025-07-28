package com.thellex.payments.features.fiat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.capitalizeFirst
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.CryptoToFiatOffRampRequestDto
import com.thellex.payments.data.model.IBankAccountDto
import com.thellex.payments.data.model.IBankInfoRequestDto
import com.thellex.payments.data.viewModels.rates.RateViewModel
import com.thellex.payments.databinding.ActivityRampSummaryBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.fiat.model.CryptoToFiatViewModel
import com.thellex.payments.features.pos.ui.POSHomeActivity
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.settings.FiatEnum
import com.thellex.payments.settings.SupportedBlockchainEnum
import com.thellex.payments.settings.TokensEnum
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.math.BigDecimal
import java.math.RoundingMode

@RequiresApi(Build.VERSION_CODES.O)
class RampSummaryActivity : AppCompatActivity() {

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
        binding.layoutSummaryRoot.applyAdvancedSystemBarInsets()

        setupTopBar()
        setupViewModels()
        setupListeners()
        observeViewModelData()
    }

    private fun setupTopBar() {
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.top_app_bar),
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
        binding.buttonConfirm.setOnClickListener {
            makeOffRampRequest()
        }

        binding.buttonCancel.setOnClickListener {
            cryptoToFiatViewModel.clearData()
            finish()
        }
    }

    private fun observeViewModelData() {
        cryptoToFiatViewModel.fiatAmount.observe(this) { amount ->
            updateFiatAndCryptoAmount(amount)
        }

        cryptoToFiatViewModel.bankInfo.observe(this) { bank ->
            updateBankInfo(bank)
        }

        cryptoToFiatViewModel.fee.observe(this) {
            // You can update a UI fee field here if needed
        }

        cryptoToFiatViewModel.paymentReason.observe(this) {
            // You can update a UI reason field here if needed
        }
    }

    private fun updateFiatAndCryptoAmount(amount: Double?) {
        val fiatCode = cryptoToFiatViewModel.fiatCode.value ?: "NGN"
        if (amount != null && amount > 0.0) {
            binding.valueFiatAmount.text = "${amount.toBigDecimal().setScale(2, RoundingMode.HALF_UP)} $fiatCode"

            val rate = cryptoToFiatViewModel.currentRate.value
            val fee = cryptoToFiatViewModel.fee.value ?: 0.0
            val cryptoAmount = if (rate != null) {
                (amount / (rate.buy * (1.0 - (fee / 100.0))))
                    .toBigDecimal().setScale(6, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO

            val assetCode = cryptoToFiatViewModel.assetCode.value?.uppercase() ?: "USDT"
            binding.valueCryptoAmount.text = "$cryptoAmount $assetCode"
        } else {
            binding.valueFiatAmount.text = "0.00 NGN"
            binding.valueCryptoAmount.text = "0.00 USDT"
        }
    }

    private fun updateBankInfo(bankAccount: IBankInfoRequestDto?) {
        binding.textAccountName.text = bankAccount?.accountHolder ?: "N/A"
        binding.textBankName.text = bankAccount?.bankName?.capitalizeFirst() ?: "N/A"
        binding.textAccountNumber.text = bankAccount?.accountNumber ?: "N/A"
    }

    private fun makeOffRampRequest() {
        binding.buttonConfirm.setSubmitting(true)
        binding.buttonCancel.isEnabled = false

        lifecycleScope.launch {
            try {
                val token = try {
                    withTimeoutOrNull(5000) {
                        userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                    }
                } catch (e: Exception) {
                    null
                }

                if (token.isNullOrBlank()) {
                    CustomToast.show(this@RampSummaryActivity, "Authentication Error", "Token not available.")
                    return@launch
                }

                val request = buildOffRampRequest()
                if (request == null) {
                    CustomToast.show(this@RampSummaryActivity, "Error", "Invalid request")
                    return@launch
                }

                val response = ApiClient.getAuthenticatedPaymentApi(token).cryptoToFiatOffRamp(request)

                response.body()?.result?.let { result ->
                    userViewModel.addFiatCryptoRampTransaction(result)
                    val intent = Intent(this@RampSummaryActivity, POSHomeActivity::class.java)
                    startActivity(intent)
                } ?: run {
                    CustomToast.show(this@RampSummaryActivity, "Error", "Unexpected response")
                }
            } catch (e: Exception) {
                Log.e("RampSummaryActivity", "Exception occurred: ${e.message}", e)
                CustomToast.show(this@RampSummaryActivity, "Error", "An unexpected error occurred.")
            } finally {
                binding.buttonConfirm.setSubmitting(false)
                binding.buttonCancel.isEnabled = true
            }
        }
    }

    private fun buildOffRampRequest(): CryptoToFiatOffRampRequestDto? {
        val reason = cryptoToFiatViewModel.paymentReason.value
        val network = cryptoToFiatViewModel.network.value
        val sourceAddress = cryptoToFiatViewModel.sourceAddress.value
        val assetCode = cryptoToFiatViewModel.assetCode.value
        val country = cryptoToFiatViewModel.country.value
        val fiatCode = cryptoToFiatViewModel.fiatCode.value
        val fiatAmount = cryptoToFiatViewModel.fiatAmount.value
        val bankInfo = cryptoToFiatViewModel.bankInfo.value

        if (reason.isNullOrBlank() || network.isNullOrBlank() || sourceAddress.isNullOrBlank() ||
            assetCode.isNullOrBlank() || country.isNullOrBlank() || fiatCode.isNullOrBlank() ||
            fiatAmount == null || bankInfo == null
        ) {
            CustomToast.show(this, "Missing Data", "Please complete all fields.")
            return null
        }

        val fiatEnum = FiatEnum.fromCode(fiatCode)
        val blockchainEnum = SupportedBlockchainEnum.fromValue(network)
        val tokenEnum = try {
            TokensEnum.valueOf(assetCode.lowercase())
        } catch (e: Exception) {
            null
        }

        if (fiatEnum == null || blockchainEnum == null || tokenEnum == null) {
            CustomToast.show(this, "Invalid Input", "Currency or network format is invalid.")
            return null
        }

        return CryptoToFiatOffRampRequestDto(
            paymentReason = reason,
            network = blockchainEnum,
            sourceAddress = sourceAddress,
            assetCode = tokenEnum,
            country = country,
            fiatCode = fiatEnum,
            userAmount = fiatAmount,
            bankInfo = bankInfo
        )
    }

    private fun findActivity(activityClass: Class<out AppCompatActivity>): AppCompatActivity? {
        return ActivityTracker.getActivities().find { it::class.java == activityClass } as? AppCompatActivity
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityTracker.remove(this)
    }

    private fun String.capitalizeFirst(): String = replaceFirstChar { it.uppercase() }
}