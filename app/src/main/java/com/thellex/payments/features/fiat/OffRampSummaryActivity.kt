package com.thellex.payments.features.fiat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.capitalizeFirst
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.core.utils.Helpers.truncateToTwoDecimals
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
            owner = findActivity(CryptoToFiatOffRampActivity::class.java)
                ?: run {
                    startActivity(Intent(this, CryptoToFiatOffRampActivity::class.java))
                    finish()
                    return@setupViewModels
                },
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
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

        rampServiceFeeValue.text = "${viewModel.feeFiat.value} NGN | ${viewModel.feeUSD.value} USD"

        rampBlockchainNetworkIcon.setImageResource(
            Helpers.getIconResIdForBlockchain(viewModel.network.value.orEmpty())
        )
        rampBlockchainNetworkName.text = viewModel.network.value?.uppercase().orEmpty()
        rampTransactionReasonValue.text = viewModel.paymentReason.value?.uppercase().orEmpty()

        viewModel.assetCode.value?.let { assetCode ->
            viewModel.currentRate.value?.sell?.let { rate ->
                val fiatCode = FiatTickers.getByCodeOrCountry("ngn")?.currencyCode.orEmpty()
                rampExchangeRateValue.text = "$rate $fiatCode/${assetCode.uppercase()}"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeLiveUpdates() {
        cryptoToFiatViewModel.mainAssetAmount.observe(this){
            val assetCode = cryptoToFiatViewModel.assetCode.value?.uppercase()
            binding.offrampUsdAmount.text = "${it.truncateToTwoDecimals()} $assetCode"
        }

        cryptoToFiatViewModel.fiatAmount.observe(this) {
            val fiatCode = cryptoToFiatViewModel.fiatCode.value ?: "NGN"
            binding.offrampFiatAmount.text = "${it.truncateToTwoDecimals()} $fiatCode"
        }

        cryptoToFiatViewModel.bankInfo.observe(this) {
            updateBankInfo(it)
        }

        cryptoToFiatViewModel.currentRate.observe(this) {
            setupTransactionSummary()
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
            val context = this@OffRampSummaryActivity

            try {
                val token = withTimeoutOrNull(5000) {
                    userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                }

                if (token.isNullOrBlank()) {
                    CustomToast.show(context, "Authentication Error", "Token not available.")
                    return@launch
                }

                val request = buildOffRampRequest() ?: return@launch

                val api = ApiClient.getAuthenticatedPaymentApi(token)
                val response = api.cryptoToFiatOffRamp(request)

                val result = response.body()?.result
                if (result != null) {
                    result.let { txn ->
                        userViewModel.addFiatCryptoRampTransaction(txn)
                        userViewModel.addTransaction(txn.transaction!!)
                    }
//                    ActivityTracker.finishActivity(PaymentMethodActivity::class.java)
                    startActivity(Intent(context, POSHomeActivity::class.java))
                } else {
                    CustomToast.show(context, "Error", "Unexpected response")
                }
            } catch (e: Exception) {
                Log.e("OffRampRequest", "Error during off-ramp request: ${e.message}", e)
                CustomToast.show(context, "Error", "An unexpected error occurred.")
            } finally {
                binding.offrampSubmitButton.setSubmitting(false)
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
        val amount = vm.mainFiatAmount.value
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

    companion object {
        private const val TAG = "OffRampSummaryActivity"
    }
}
