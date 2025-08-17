package com.thellex.payments.features.fiat

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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.getIconResIdForToken
import com.thellex.payments.core.utils.Helpers.setLoading
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.core.utils.reasonList
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.viewModels.rates.RateViewModel
import com.thellex.payments.databinding.ActivityCryptoToFiatOffRampBinding
import com.thellex.payments.databinding.DialogReasonSelectionBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.fiat.adapters.ReasonSelectionAdapter
import com.thellex.payments.features.fiat.adapters.TokenSelectionBottomSheet
import com.thellex.payments.features.fiat.model.CryptoToFiatViewModel
import com.thellex.payments.features.wallet.model.WalletDto
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import com.thellex.payments.settings.minimumAmountInFiat
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
    private lateinit var cryptoToFiatViewModel: CryptoToFiatViewModel
    private lateinit var rateViewModel: RateViewModel

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
        cryptoToFiatViewModel.fiatAmount.value?.let { amount ->
            if (amount > minimumAmountInFiat) {
                binding.edittextFiatAmount.setText(
                    amount.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toPlainString()
                )
                calculateAndDisplayPrice(cryptoChanged = false)
            }
        }

        // Restore rate and fee
        cryptoToFiatViewModel.currentRate.value?.let {
            cryptoToFiatViewModel.fee.value?.let { fee ->
                binding.textPriceValue.text = "${fee}%"
                binding.nextButton.setLoading(false) // Enable button if rate is restored
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

        cryptoToFiatViewModel.fee.observe(this) { fee ->
            binding.textPriceValue.text = "${fee}%"
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeRates() {
        lifecycleScope.launch {
            rateViewModel.rates.collectLatest { rates ->
                if (rates.isNotEmpty()) {
                    cryptoToFiatViewModel.updateRate(rates)
                    binding.textPriceValue.text = "${cryptoToFiatViewModel.fee.value ?: 0.0}%"
                    binding.nextButton.setLoading(false) // Enable button when rates are available
                    calculateAndDisplayPrice()
                } else {
                    updateDefaultPriceText()
                    binding.nextButton.setLoading(true) // Disable button if no rates
                    cryptoToFiatViewModel.updateRate(emptyList()) // Clear rate if none available
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDefaultPriceText() {
        val tokenSymbol = selectedToken?.assetCode?.name?.uppercase(Locale.getDefault()) ?: "TOKEN"
        binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
    }

    private fun setupAmountInputListeners() {
        val errorDrawable = ContextCompat.getDrawable(this, R.drawable.bg_edittext_error)
        val normalDrawable = ContextCompat.getDrawable(this, R.drawable.bg_edittext_normal)

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

        binding.edittextCryptoAmount.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdating && binding.edittextCryptoAmount.hasFocus()) {
                    isUpdating = true
                    val cryptoAmount = s.toString().toDoubleOrNull() ?: 0.0
                    val fiatEquivalent = calculateFiatFromCrypto(cryptoAmount)
                    val background = if (fiatEquivalent < minimumAmountInFiat) errorDrawable else normalDrawable
                    binding.edittextCryptoAmount.background = background
                    binding.edittextFiatAmount.background = background
                    calculateAndDisplayPrice(cryptoChanged = true)
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
        } ?: run {
            binding.textCryptoWalletName.text = "No Wallet Selected"
            binding.cryptoIcon.setImageResource(Helpers.getIconResIdForToken(""))
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
        updateDefaultPriceText()
    }

    private fun setDefaultToken() {
        val walletBalance = walletManagerViewModel.walletBalance.value
        val defaultToken = walletBalance?.wallets?.get("usdc") ?: walletBalance?.wallets?.values?.firstOrNull()

        defaultToken?.let {
            updateTokenSpinner(it)
        } ?: run {
            updateDefaultPriceText()
            updateWalletInfo()
        }
    }

    private fun calculateFiatFromCrypto(cryptoAmount: Double): Double {
        // Fiat = (Crypto * Buy Rate) * (1 - Fee Percentage)
        val rate = cryptoToFiatViewModel.currentRate.value?.buy ?: 0.0
        val feePercentage = cryptoToFiatViewModel.fee.value?.div(100.0) ?: 0.0
        return if (cryptoAmount <= 0.0 || rate <= 0.0) 0.0 else (cryptoAmount * rate) * (1.0 - feePercentage)
    }

    private fun calculateCryptoFromFiat(fiatAmount: Double): Double {
        // Crypto = Fiat / (Buy Rate * (1 - Fee Percentage))
        val rate = cryptoToFiatViewModel.currentRate.value?.buy ?: 1.0
        val feePercentage = cryptoToFiatViewModel.fee.value?.div(100.0) ?: 0.0
        return if (fiatAmount <= 0.0 || rate <= 0.0 || (1.0 - feePercentage) <= 0.0) 0.0 else fiatAmount / (rate * (1.0 - feePercentage))
    }

    @SuppressLint("SetTextI18n")
    private fun calculateAndDisplayPrice(cryptoChanged: Boolean = false) {
        val fiatText = binding.edittextFiatAmount.text.toString().trim()
        val cryptoText = binding.edittextCryptoAmount.text.toString().trim()
        val tokenSymbol = selectedToken?.assetCode?.name?.uppercase(Locale.getDefault()) ?: "TOKEN"

        if (fiatText.isEmpty() && cryptoText.isEmpty()) {
            binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
            binding.edittextFiatAmount.setText("")
            binding.edittextCryptoAmount.setText("")
            return
        }

        try {
            if (!cryptoChanged) {
                // Fiat input changed
                val fiatAmount = fiatText.toDoubleOrNull() ?: 0.0
                if (fiatAmount <= minimumAmountInFiat) {
                    binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
                    if (!binding.edittextCryptoAmount.hasFocus()) binding.edittextCryptoAmount.setText("")
                    return
                }
                val cryptoAmount = calculateCryptoFromFiat(fiatAmount)
                val formattedCrypto = cryptoAmount.toBigDecimal().setScale(6, RoundingMode.HALF_UP).toPlainString()
                if (!binding.edittextCryptoAmount.hasFocus()) {
                    binding.edittextCryptoAmount.setText(formattedCrypto)
                    binding.edittextCryptoAmount.setSelection(formattedCrypto.length)
                }
                binding.textPriceValue.text = "≅ ${fiatAmount.toBigDecimal().setScale(2, RoundingMode.HALF_UP)} $fiatCode"
            } else {
                // Crypto input changed
                val cryptoAmount = cryptoText.toDoubleOrNull() ?: 0.0
                if (cryptoAmount <= 0.0) {
                    binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
                    if (!binding.edittextFiatAmount.hasFocus()) binding.edittextFiatAmount.setText("")
                    return
                }
                val fiatAmount = calculateFiatFromCrypto(cryptoAmount)
                if (fiatAmount <= minimumAmountInFiat) {
                    binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
                    if (!binding.edittextFiatAmount.hasFocus()) binding.edittextFiatAmount.setText("")
                    return
                }
                val formattedFiat = fiatAmount.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toPlainString()
                if (!binding.edittextFiatAmount.hasFocus()) {
                    binding.edittextFiatAmount.setText(formattedFiat)
                    binding.edittextFiatAmount.setSelection(formattedFiat.length)
                }
                binding.textPriceValue.text = "≅ $formattedFiat $fiatCode"
            }
        } catch (e: Exception) {
            binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
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

//                    // Validate crypto amount against wallet balance
//                    if (cryptoAmount > selectedToken.totalBalance) {
//                        CustomToast.show(this@CryptoToFiatOffRampActivity, "Error", "Insufficient balance in ${selectedToken.assetCode.name.uppercase()}")
//                        return@launch
//                    }

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
                        fiatAmount = fiatAmount,
                        mainAssetAmount = cryptoAmount
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

        rateViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[RateViewModel::class.java]
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
            startActivity(Intent(this, FiatRampTransactionsActivity::class.java))
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
        private const val TAG = "TAGY"
    }
}