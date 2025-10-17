package com.thellex.pay.features.fiat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.thellex.pay.R
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.ComposeHostActivity
import com.thellex.pay.core.utils.CustomToast
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.getIconResIdForToken
import com.thellex.pay.core.utils.Helpers.setLoading
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.core.utils.Helpers.truncateToTwoDecimals
import com.thellex.pay.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.pay.data.model.PaymentStatusEnum
import com.thellex.pay.data.model.reasonList
import com.thellex.pay.data.viewModels.rates.RateViewModel
import com.thellex.pay.databinding.ActivityCryptoToFiatOffRampBinding
import com.thellex.pay.databinding.DialogReasonSelectionBinding
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.fiat.adapters.ReasonSelectionAdapter
import com.thellex.pay.features.fiat.adapters.TokenSelectionBottomSheet
import com.thellex.pay.features.fiat.model.CryptoToFiatViewModel
import com.thellex.pay.features.wallet.model.WalletDto
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
import com.thellex.pay.settings.minimumAmountInFiat
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Locale

class CryptoToFiatOffRampActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: ActivityCryptoToFiatOffRampBinding
    private var selectedToken: WalletDto? = null
    private var ratesRefreshHandler: Handler? = null
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private var fiatCode: String = "NGN"
    private val rateModel: RateViewModel by viewModels()
    private lateinit var cryptoToFiatViewModel: CryptoToFiatViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCryptoToFiatOffRampBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        ActivityTracker.finishActivity(FiatToCryptoOnRampActivity::class.java)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.include_top_app_bar),
            title = "SPEND YOUR CRYPTO"
        )

        // Disable nextButton by default until rates are fetched
        binding.nextButton.isEnabled = false

        setupViewModel()
        setupUiListener()
        setupAmountInputListeners()
        setDefaultToken()
        observeUser()
        restoreSavedData()
        observeRates()

        supportFragmentManager.setFragmentResultListener(
            TokenSelectionBottomSheet.RESULT_KEY,
            this
        ) { _, bundle ->
            bundle.getString(TokenSelectionBottomSheet.TOKEN_KEY)?.let { json ->
                selectedToken = Gson().fromJson(json, WalletDto::class.java)
                selectedToken?.let { updateTokenSpinner(it) }
            }
        }

        rateModel.startPolling()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeRates() {
        lifecycleScope.launch {
            rateModel.currentRates.collectLatest { rates ->
                rates.rates
                if (rates.rates.isNotEmpty()) {
                    cryptoToFiatViewModel.updateRate(rates.rates)
                    binding.textRateValue.text = "${cryptoToFiatViewModel.currentRate.value?.sell ?: 0.0}%"
                    binding.nextButton.setLoading(false)
                    calculateAndDisplayPrice()
                } else {
                    updateRatePriceText()
                    binding.nextButton.setLoading(true)
                    cryptoToFiatViewModel.updateRate(emptyList())
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun restoreSavedData() {
        // Restore payment reason
        cryptoToFiatViewModel.paymentReason.value?.let { reason ->
            binding.edittextPaymentReason.setText(reason)
        }

        // Restore fiat code
        cryptoToFiatViewModel.fiatCode.value?.let { code ->
            fiatCode = code
        }

        // Restore fiat amount and recalculate UI if valid
        cryptoToFiatViewModel.netFiatAmount.value?.let { amount ->
            if (amount > minimumAmountInFiat) {
                binding.edittextFiatAmount.setText("{${amount.truncateToTwoDecimals()}}")
                calculateAndDisplayPrice(cryptoChanged = false)
            }
        }

        // Restore rate and fee
        cryptoToFiatViewModel.currentRate.value?.let {
            cryptoToFiatViewModel.feePercentage.value?.let { fee ->
                binding.nextButton.setLoading(false)
                calculateAndDisplayPrice()
            }
        }

        // Restore token-related data (sourceAddress, assetCode, network)
        cryptoToFiatViewModel.sourceAddress.value?.let { address ->
            if (selectedToken?.address == address) {
                // Token still matches, no need to update
                return@let
            }
            // Reselect token if it matches wallet data
            walletManagerViewModel.walletBalance.value?.wallets?.values?.find { it.address == address }
                ?.let { updateTokenSpinner(it) }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateRatePriceText() {
        val tokenSymbol = selectedToken?.assetCode?.name?.uppercase(Locale.getDefault()) ?: "TOKEN"
        binding.textRateValue.text = "≅ 0.00 ${fiatCode.uppercase()}/$tokenSymbol"
    }

    private fun setupAmountInputListeners() {
        val errorDrawable = ContextCompat.getDrawable(this, R.drawable.bg_edittext_error)
        val normalDrawable = ContextCompat.getDrawable(this, R.drawable.bg_edittext_normal)

        binding.edittextCryptoAmount.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdating && binding.edittextCryptoAmount.hasFocus()) {
                    isUpdating = true
                    val cryptoAmount = s.toString().toDoubleOrNull() ?: 0.0
                    val (fiatAmount, feeFiat, feeUSD) = calculateFiatFromCrypto(cryptoAmount, cryptoToFiatViewModel.currentRate.value?.feeDivisor!!)
                    val background = if (fiatAmount < minimumAmountInFiat) errorDrawable else normalDrawable
                    cryptoToFiatViewModel.updateAmounts(feeFiat = feeFiat, feeUSD = feeUSD, fiatAmount = 149.352, cryptoAmount = cryptoAmount, fiatAmount)
                    binding.edittextCryptoAmount.background = background
                    binding.edittextFiatAmount.background = background
                    calculateAndDisplayPrice(cryptoChanged = true)
                    isUpdating = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edittextFiatAmount.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdating && binding.edittextFiatAmount.hasFocus()) {
                    isUpdating = true
                    val fiatAmount = s.toString().toDoubleOrNull() ?: 0.0
                    val background = if (fiatAmount < minimumAmountInFiat) errorDrawable else normalDrawable
                    binding.edittextFiatAmount.background = background
                    binding.edittextCryptoAmount.background = background
                    calculateAndDisplayPrice(cryptoChanged = false)
                    isUpdating = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        binding.edittextPaymentReason.setOnClickListener {
            showReasonSelectionBottomSheet(binding.edittextPaymentReason)
        }
    }

    private fun updateWalletInfo() {
        selectedToken?.let { token ->
            binding.textCryptoWalletName.text = "${token.assetCode.name.uppercase(Locale.getDefault())} WALLET"
            binding.cryptoIcon.setImageResource(getIconResIdForToken(token.assetCode.toString()))
            val formattedBalance = token.totalBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toPlainString()
            binding.textCryptoBalance.text = "$formattedBalance ${token.assetCode.name.uppercase(Locale.getDefault())}"

            binding.textAvailableBalance.text = "Available: $formattedBalance ${token.assetCode.name.uppercase()}"

            binding.buttonMax.setOnClickListener {
                selectedToken?.let {
                    val maxAmount = it.totalBalance
                    val editableAmount = Editable.Factory.getInstance().newEditable(maxAmount.toString())
                    binding.edittextCryptoAmount.text = editableAmount
                } ?: run {
                    CustomToast.show(this, "Warning", "Balance not available")
                }
            }

        } ?: run {
            binding.textCryptoWalletName.text = "No Wallet Selected"
            binding.cryptoIcon.setImageResource(getIconResIdForToken(""))
            binding.textCryptoBalance.text = "0.00"
        }
    }

    fun updateTokenSpinner(token: WalletDto) {
        selectedToken = token
        binding.textCryptoCurrency.text = token.assetCode.name.uppercase(Locale.getDefault())
        binding.assetIcon.setImageResource(getIconResIdForToken(token.assetCode.toString()))
        binding.edittextFiatAmount.setText("")
        binding.edittextCryptoAmount.setText("")
        updateWalletInfo()
        updateRatePriceText()
    }

    private fun setDefaultToken() {
        val walletBalance = walletManagerViewModel.walletBalance.value
        val defaultToken = walletBalance?.wallets?.get("usdc") ?: walletBalance?.wallets?.values?.firstOrNull()

        defaultToken?.let {
            updateTokenSpinner(it)
        } ?: run {
            updateRatePriceText()
            updateWalletInfo()
        }
    }

    private fun calculateFiatFromCrypto(cryptoAmount: Double, feeDivisor: Double): Triple<Double, Double, Double> {
        // Get current sell rate
        val rate = cryptoToFiatViewModel.currentRate.value?.sell ?: 0.0
        // Fee percentage in decimal
        val feePercentage = cryptoToFiatViewModel.feePercentage.value

        if (cryptoAmount <= 0.0 || rate <= 0.0) return Triple(0.0, 0.0, 0.0)

        // Total fiat before fee
        val totalFiat = cryptoAmount * rate
        // Fee in fiat
        val feeFiat = totalFiat * feePercentage?.toDouble()!!
        // Net fiat user receives
        val netFiat = totalFiat - feeFiat
        // Fee in crypto asset
        val feeCrypto = cryptoAmount * feePercentage

        return Triple(netFiat, feeFiat, feeCrypto)
    }

//    private fun calculateCryptoFromFiat(fiatAmount: Double, feeDivisor: Double): Double {
//        // Crypto = Fiat / (Buy Rate * (1 + Fee Percentage))
//        val rate = cryptoToFiatViewModel.currentRate.value?.sell ?: 1.0
//        val feePercentage = cryptoToFiatViewModel.fee.value?.div(feeDivisor) ?: 0.0
//        return if (fiatAmount <= 0.0 || rate <= 0.0 || (1.0 + feePercentage) <= 0.0) 0.0 else fiatAmount / (rate * (1.0 + feePercentage))
//    }
    @SuppressLint("SetTextI18n")
    private fun calculateAndDisplayPrice(cryptoChanged: Boolean = false) {
        val fiatText = binding.edittextFiatAmount.text.toString().trim()
        val cryptoText = binding.edittextCryptoAmount.text.toString().trim()
        val tokenSymbol = selectedToken?.assetCode?.name?.uppercase(Locale.getDefault()) ?: "TOKEN"
        val currentRate = cryptoToFiatViewModel.currentRate.value
        val fiatCode = cryptoToFiatViewModel.fiatCode.value ?: "NGN"

        // Show base rate if no input
        if (fiatText.isEmpty() && cryptoText.isEmpty()) {
            binding.textRateValue.text = "≅ ${currentRate?.sell ?: "--"} $fiatCode/$tokenSymbol"
            binding.edittextFiatAmount.text?.clear()
            binding.edittextCryptoAmount.text?.clear()
            return
        }

        try {
            if (cryptoChanged) {
                val cryptoAmount = cryptoText.toDoubleOrNull() ?: 0.0
                if (cryptoAmount <= 0.0) {
                    if (!binding.edittextFiatAmount.hasFocus()) binding.edittextFiatAmount.text?.clear()
                    return
                }

                val feeDivisor = currentRate?.feeDivisor ?: return
                val (fiatAmount, feeFiat, feeUSD) = calculateFiatFromCrypto(cryptoAmount, feeDivisor)

                if (fiatAmount <= minimumAmountInFiat) {
                    if (!binding.edittextFiatAmount.hasFocus()) binding.edittextFiatAmount.text?.clear()
                    return
                }

                if (!binding.edittextFiatAmount.hasFocus()) {
                    val formattedFiat = fiatAmount.truncateToTwoDecimals()
                    binding.edittextFiatAmount.setText("$formattedFiat")
                }
            }
            // else → fiat input branch (can be added later if needed)
        } catch (e: Exception) {
            CustomToast.show(this, "Input", "Invalid input", Toast.LENGTH_SHORT)
        }
    }

    private fun setupUiListener() {
        binding.nextButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val fiatAmountStr = binding.edittextFiatAmount.text.toString().trim()
                    val cryptoAmountStr = binding.edittextCryptoAmount.text.toString().trim()
                    val reason = binding.edittextPaymentReason.text.toString().trim()

                    val fiatAmount = fiatAmountStr.toDoubleOrNull()
                    if (fiatAmount == null || fiatAmount <= minimumAmountInFiat) {
                        CustomToast.show(this@CryptoToFiatOffRampActivity, "Warning", "Please enter a valid amount above $minimumAmountInFiat $fiatCode")
                        return@launch
                    }

                    val cryptoAmount = cryptoAmountStr.toDoubleOrNull()
                    if (cryptoAmount == null || cryptoAmount <= 0.0) {
                        CustomToast.show(this@CryptoToFiatOffRampActivity, "Warning", "Invalid crypto amount")
                        return@launch
                    }

                    val selectedToken = selectedToken
                    if (selectedToken == null || selectedToken.address.isEmpty()) {
                        CustomToast.show(this@CryptoToFiatOffRampActivity, "Error", "No wallet address available")
                        return@launch
                    }

                    if (cryptoToFiatViewModel.currentRate.value == null) {
                        CustomToast.show(this@CryptoToFiatOffRampActivity, "Error", "Rates not available")
                        return@launch
                    }

                    // Validate crypto amount against wallet balance
                    if (cryptoAmount > selectedToken.totalBalance) {
                        CustomToast.show(this@CryptoToFiatOffRampActivity, "Error", "Insufficient balance in ${selectedToken.assetCode.name.uppercase()}")
                        return@launch
                    }

                    if (reason.isEmpty()) {
                        CustomToast.show(this@CryptoToFiatOffRampActivity, "Warning", "Please select a reason")
                        return@launch
                    }

                    binding.nextButton.setLoading(true)

                    // Save data to ViewModel
                    cryptoToFiatViewModel.setRampData(
                        paymentReason = reason.lowercase(),
                        network = selectedToken.network.name,
                        sourceAddress = selectedToken.address,
                        assetCode = selectedToken.assetCode.toString(),
                        country = userViewModel.authResult.value?.kyc?.country ?: "ng",
                        fiatCode = fiatCode,
                    )

                    val intent = Intent(this@CryptoToFiatOffRampActivity, PaymentMethodActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@CryptoToFiatOffRampActivity, "Error starting payment method", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.nextButton.setLoading(false)
                }
            }
        }

        binding.cryptoCurrencySelector.setOnClickListener {
            val walletBalance = walletManagerViewModel.walletBalance.value
            if (walletBalance == null) {
                Toast.makeText(this, "No wallet data available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            TokenSelectionBottomSheet.newInstance(walletBalance)
                .show(supportFragmentManager, TokenSelectionBottomSheet.TAG)
        }
    }

    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        cryptoToFiatViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CryptoToFiatViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeUser() {
        userViewModel.authResult.observe(this) { userDto ->
            if (userDto?.bankAccounts.isNullOrEmpty()) {
                val intent = Intent(this, PaymentMethodActivity::class.java)
                startActivity(intent)
                finish()
            }
            updatePendingTransactionsUI(userDto?.fiatCryptoRampTransactions)
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePendingTransactionsUI(transactions: List<IFiatCryptoRampTransactionsDto>?) {
        val now = Instant.now()
        val transactionList = transactions ?: emptyList()

        if (transactionList.isEmpty()) {
            binding.layoutPendingTransactionsWrapper.visibility = View.VISIBLE
            binding.textPendingTransactionsCount.text = "Ramp Transaction History"
            binding.iconPendingClock.visibility = View.GONE
            return
        }

        val pendingTransactions = transactionList.filter {
            try {
                val expiresInstant = Instant.parse(it.expiresAt)
                !it.seen && it.paymentStatus != PaymentStatusEnum.Complete && expiresInstant.isAfter(now)
            } catch (e: DateTimeParseException) {
                false
            }
        }

        binding.layoutPendingTransactionsWrapper.visibility = View.VISIBLE
        if (pendingTransactions.isNotEmpty()) {
            val count = pendingTransactions.size
            binding.textPendingTransactionsCount.text = if (count == 1) "1 PENDING TRANSACTION" else "$count PENDING TRANSACTIONS"
            binding.iconPendingClock.visibility = View.VISIBLE
        } else {
            binding.textPendingTransactionsCount.text = "Funding & Spending History"
            binding.iconPendingClock.visibility = View.GONE
        }

        binding.layoutPendingTransactionsWrapper.setOnClickListener {
            val intent = ComposeHostActivity.newIntent(this, ComposeRoutes.RampTransactions.route)
            startActivity(intent)
        }
    }

    private fun showReasonSelectionBottomSheet(targetEditText: EditText) {
        val dialogBinding = DialogReasonSelectionBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogBinding.root)
        val adapter = ReasonSelectionAdapter(reasonList) { selectedItem ->
            targetEditText.setText(selectedItem)
            bottomSheetDialog.dismiss()
        }
        dialogBinding.recyclerviewReasonList.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerviewReasonList.adapter = adapter
        bottomSheetDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        ratesRefreshHandler?.removeCallbacksAndMessages(null)
        ratesRefreshHandler = null
    }

    companion object {
        private const val TAG = "CryptoToFiatOffRampActivity"
    }
}