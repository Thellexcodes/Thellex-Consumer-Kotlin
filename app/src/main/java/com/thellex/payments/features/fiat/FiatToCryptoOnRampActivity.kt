package com.thellex.payments.features.fiat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.core.utils.reasonList
import com.thellex.payments.data.model.FiatToCryptoOnRampRequestDto
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.databinding.ActivityFiatToCryptoOnRampBinding
import com.thellex.payments.databinding.DialogReasonSelectionBinding
import com.thellex.payments.features.auth.viewModel.UserRepository
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.fiat.adapters.ReasonSelectionAdapter
import com.thellex.payments.features.fiat.adapters.TokenSelectionBottomSheet
import com.thellex.payments.features.wallet.model.IRatesResponseDto
import com.thellex.payments.features.wallet.model.WalletDto
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.settings.FiatEnum
import com.thellex.payments.settings.minimumAmountInFiat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.TimeZone

class FiatToCryptoOnRampActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: ActivityFiatToCryptoOnRampBinding
    private lateinit var userViewModel: UserViewModel
    private val userRepository by lazy { UserRepository.getInstance(applicationContext) }
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private var selectedToken: WalletDto? = null
    private var currentRates: IRatesResponseDto? = null
    private var ratesExpiryTime: Long? = null
    private var ratesRefreshHandler: Handler? = null
    private var refreshRunnable: Runnable? = null
    private var fee: Double = 0.0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFiatToCryptoOnRampBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        ActivityTracker.finishActivity(CryptoToFiatOffRampActivity::class.java)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.include_top_app_bar),
            title = "BUY CRYPTO"
        )

        setupViewModel()
        observeUser()
        setupUiListener()
        setupAmountInputListeners()
        fetchRatesAndScheduleRefresh()

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        setDefaultToken()
        updateDefaultPriceText()
    }

    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeUser() {
        userViewModel.authResult.observe(this) { userDto ->
            updatePendingTransactionsUI(userDto?.fiatCryptoRampTransactions!!)
        }
    }

    private fun setupUiListener() {
        binding.nextButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val fiatAmountStr = binding.edittextFiatAmount.text.toString().trim()
                    val reason = binding.edittextReasonName.text.toString().trim()

                    // Validate reason early
                    if (reason.isEmpty()) {
                        CustomToast.show(this@FiatToCryptoOnRampActivity, "Warning", "Please select a reason")
                        return@launch
                    }

                    // Validate fiat amount early and safely
                    val fiatAmount = fiatAmountStr.toDoubleOrNull()
                    if (fiatAmount == null || fiatAmount <= 0) {
                        CustomToast.show(this@FiatToCryptoOnRampActivity, "Warning", "Please enter a valid amount")
                        return@launch
                    }

                    // Validate required token fields safely
                    val selectedToken = selectedToken
                    if (selectedToken == null || selectedToken.address.isEmpty()) {
                        CustomToast.show(this@FiatToCryptoOnRampActivity, "Error", "No wallet address available")
                        return@launch
                    }

                    binding.nextButton.setSubmitting(true, loadingText = "Requesting")

                    val authToken = userRepository.getToken().first()

                    val onRampRequest = FiatToCryptoOnRampRequestDto(
                        userAmount = fiatAmount,
                        fiatCode = FiatEnum.ngn.code,
                        assetCode = selectedToken.assetCode.name,
                        country = "ng",
                        paymentReason = reason.lowercase(),
                        network = selectedToken.network.toString(),
                        destinationAddress = selectedToken.address
                    )

                    val response = ApiClient.getAuthenticatedPaymentApi(authToken!!).fiatToCryptoOnRamp(onRampRequest)

                    val result = response.body()?.result
                    if(result != null){
//                        Log.d(TAG, "Response from fiatToCryptoOnRamp: ${response.body()?.result}")
                        result.let { txn ->
                            userViewModel.addFiatCryptoRampTransaction(txn)
                        }
                        val intent = Intent(this@FiatToCryptoOnRampActivity, OnRampFiatSummaryActivity::class.java).apply {
                            putExtra("fiatCryptoRampResultJson", Gson().toJson(result))
                        }
                        startActivity(intent)
                    } else  {
                        Log.w(TAG, "No result in response: $response")
                        CustomToast.show(this@FiatToCryptoOnRampActivity, "Error", "Unexpected response")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in fiatToCryptoOnRamp: ${e.message}", e)
                    Toast.makeText(this@FiatToCryptoOnRampActivity, "Request failed", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.nextButton.setSubmitting(false)
                }
            }
        }

        binding.cryptoSpinner.setOnClickListener {
            val walletBalance = walletManagerViewModel.walletBalance.value

            if (walletBalance == null) {
                Toast.makeText(this, "No wallet data available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Set the result listener once
            supportFragmentManager.setFragmentResultListener(
                TokenSelectionBottomSheet.RESULT_KEY,
                this
            ) { _, bundle ->
                bundle.getString(TokenSelectionBottomSheet.TOKEN_KEY)?.let { json ->
                    selectedToken = Gson().fromJson(json, WalletDto::class.java)
                    binding.textCryptoTicker.text = selectedToken?.assetCode?.name?.uppercase()
                    binding.assetIcon.setImageResource(getIconResIdForToken(selectedToken?.assetCode.toString()))
                }
            }

            TokenSelectionBottomSheet.newInstance(walletBalance)
                .show(supportFragmentManager, TokenSelectionBottomSheet.TAG)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePendingTransactionsUI(transactions: List<IFiatCryptoRampTransactionsDto>) {
    val now = Instant.now()

    if (transactions.isEmpty()) {
        // No transactions at all
        binding.layoutUncompletedTransactionsWrapper.visibility = View.VISIBLE
        binding.textPendingTransactionsCount.text = "Funding & Spending History"
        binding.iconPendingClock.visibility = View.GONE
        return
    }

    // Filter pending transactions that have not expired yet
    val pendingTransactions = transactions.filter {
        try {
            val expiresInstant = Instant.parse(it.expiresAt)
            !it.seen && it.paymentStatus != PaymentStatusEnum.Complete && expiresInstant.isAfter(now)
        } catch (e: DateTimeParseException) {
            false
        }
    }

    if (pendingTransactions.isNotEmpty()) {
        // Show count of pending transactions (not expired)
        val count = pendingTransactions.size
        binding.layoutUncompletedTransactionsWrapper.visibility = View.VISIBLE
        binding.textPendingTransactionsCount.text = if (count == 1) "1 PENDING TRANSACTION" else "$count PENDING TRANSACTIONS"
        binding.iconPendingClock.visibility = View.VISIBLE
    } else {
        // Either all transactions are completed or expired -> show transaction history text
        binding.layoutUncompletedTransactionsWrapper.visibility = View.VISIBLE
        binding.textPendingTransactionsCount.text = "Funding & Spending History"
        binding.iconPendingClock.visibility = View.GONE
    }

    binding.layoutUncompletedTransactionsWrapper.setOnClickListener {
        startActivity(Intent(this, FiatRampTransactionsActivity::class.java))
    }
    }

    private fun updateDefaultPriceText() {
        val tokenSymbol = selectedToken?.assetCode?.name?.uppercase(Locale.getDefault()) ?: "TOKEN"
        binding.textPriceValue.text = "≅ 0.00 NGN/$tokenSymbol"
    }

    fun updateTokenSpinner(token: WalletDto) {
        selectedToken = token
        binding.textCryptoTicker.text = token.assetCode.name.uppercase(Locale.getDefault())
        binding.assetIcon.setImageResource(getIconResIdForToken(token.assetCode.toString()))
        binding.fiatSpinner.invalidate()
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
                updateWalletInfo() // Set default UI for balanceOverview
        }
    }

    private fun updateWalletInfo() {
        selectedToken?.let { token ->
            binding.textCryptoWalletName.text = "${token.assetCode.name.uppercase(Locale.getDefault())} WALLET"
            binding.assetFlag.setImageResource(getIconResIdForToken(token.assetCode.toString()))
            val formattedBalance = token.totalBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toPlainString()
            binding.textBalanceAmount.text = "${formattedBalance} ${token.assetCode.name.uppercase(Locale.getDefault())}"
            } ?: run {
            //            binding.textCryptoWalletName.text = "TOKEN Wallet"
            //            binding.assetFlag.setImageResource(getIconResIdForToken())
            //            binding.textBalanceAmount.text = "0.00"
            //            Log.d(TAG, "No selected token, set default wallet info")
            }
        }

    private fun fetchRatesAndScheduleRefresh() {
        lifecycleScope.launch {
            try {
                val authToken = userRepository.getToken().first() ?: throw IllegalStateException("No auth token available")
                val response = ApiClient.getAuthenticatedPaymentApi(authToken).getRates()

                response.result?.let { result ->
                    val ngnRateDto = result.rates.firstOrNull { it.fiatCode.equals(FiatEnum.ngn.code, ignoreCase = true) }
                    if (ngnRateDto == null) {
                        updateDefaultPriceText()
                        return@let
                    }
                    currentRates = result
                    fee = ngnRateDto.rate.fee / ngnRateDto.rate.feeDivisor
                    ratesExpiryTime = parseExpiresAt(result.expiresAt)
                    calculateAndDisplayPrice()
                    scheduleRatesRefresh(ratesExpiryTime ?: (System.currentTimeMillis() + 60000))
                } ?: run {
                    Toast.makeText(this@FiatToCryptoOnRampActivity, "Error fetching rates", Toast.LENGTH_SHORT).show()
                    updateDefaultPriceText()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching rates: ${e.message}", e)
                Toast.makeText(this@FiatToCryptoOnRampActivity, "Error fetching rates", Toast.LENGTH_SHORT).show()
                updateDefaultPriceText()
            }
        }
    }

    private fun parseExpiresAt(expiresAt: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(expiresAt)?.time ?: System.currentTimeMillis() + 60000
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing expiresAt: ${e.message}", e)
            System.currentTimeMillis() + 60000 // Fallback: 1 minute
        }
    }

    private fun scheduleRatesRefresh(expiresAt: Long) {
        val now = System.currentTimeMillis()
        val delayMillis = (expiresAt - now - 5000).coerceAtLeast(1000)

        if (ratesRefreshHandler == null) {
            ratesRefreshHandler = Handler(Looper.getMainLooper())
        }

        refreshRunnable?.let { ratesRefreshHandler?.removeCallbacks(it) }
        refreshRunnable = Runnable { fetchRatesAndScheduleRefresh() }
        ratesRefreshHandler?.postDelayed(refreshRunnable!!, delayMillis)
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
                    val ngnRateDto = currentRates?.rates?.firstOrNull {
                        it.fiatCode.equals(FiatEnum.ngn.name, ignoreCase = true)
                    }

                    val rate = ngnRateDto?.rate
                    val fiatEquivalent = rate?.let {
                        (cryptoAmount * it.buy) + (it.fee / it.feeDivisor)
                    } ?: 0.0

                    val background = if (fiatEquivalent < minimumAmountInFiat) errorDrawable else normalDrawable
                    binding.edittextCryptoAmount.background = background
                    binding.edittextFiatAmount.background = background
                    calculateAndDisplayPrice(cryptoChanged = true)
                    isUpdating = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edittextReasonName.setOnClickListener {
            showReasonSelectionBottomSheet(binding.edittextReasonName)
        }
    }

    private fun calculateAndDisplayPrice(cryptoChanged: Boolean = false) {
        val fiatText = binding.edittextFiatAmount.text.toString()
        val cryptoText = binding.edittextCryptoAmount.text.toString()
        val tokenSymbol = selectedToken?.assetCode?.name?.uppercase(Locale.getDefault()) ?: "TOKEN"
        val ngnRateDto = currentRates?.rates?.firstOrNull { it.fiatCode.equals(FiatEnum.ngn.code, ignoreCase = true) }
        val rate = ngnRateDto?.rate?.buy ?: 0.0
        val fiatCode = ngnRateDto?.fiatCode ?: FiatEnum.ngn.code
        val fee = ngnRateDto?.rate?.fee?.div(ngnRateDto.rate.feeDivisor) ?: 0.0

        if (fiatText.isEmpty() && cryptoText.isEmpty()) {
            binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
            return
        }

        try {
            if (!cryptoChanged) {
                // Fiat input changed
                val fiatAmount = fiatText.toDoubleOrNull() ?: 0.0
                if (fiatAmount <= fee) {
                    if (!binding.edittextCryptoAmount.hasFocus()) binding.edittextCryptoAmount.setText("")
                    binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
                    Toast.makeText(this, "Fiat amount must be greater than fee ($minimumAmountInFiat $fiatCode)", Toast.LENGTH_SHORT).show()
                    return
                }
                val cryptoAmount = (fiatAmount - fee) / rate
                val formattedCrypto = cryptoAmount.takeIf { it.isFinite() }?.toBigDecimal()?.setScale(6, RoundingMode.HALF_UP)?.toPlainString() ?: "0.0"
                if (!binding.edittextCryptoAmount.hasFocus()) {
                    binding.edittextCryptoAmount.setText(formattedCrypto)
                    binding.edittextCryptoAmount.setSelection(formattedCrypto.length)
                }
                binding.textPriceValue.text = "≅ ${fiatAmount.toBigDecimal().setScale(2, RoundingMode.HALF_UP)} $fiatCode/$tokenSymbol"
            } else {
                // Crypto input changed
                val cryptoAmount = cryptoText.toDoubleOrNull() ?: 0.0
                if (cryptoAmount == 0.0) {
                    if (!binding.edittextFiatAmount.hasFocus()) binding.edittextFiatAmount.setText("")
                    binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
                    return
                }
                val fiatAmount = (cryptoAmount * rate) + fee
                val formattedFiat = fiatAmount.takeIf { it.isFinite() }?.toBigDecimal()?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: "0.0"
                if (!binding.edittextFiatAmount.hasFocus()) {
                    binding.edittextFiatAmount.setText(formattedFiat)
                    binding.edittextFiatAmount.setSelection(formattedFiat.length)
                }
                binding.textPriceValue.text = "≅ ${fiatAmount.toBigDecimal().setScale(2, RoundingMode.HALF_UP)} $fiatCode/$tokenSymbol"
            }
        } catch (e: Exception) {
            binding.textPriceValue.text = "≅ 0.00 $fiatCode/$tokenSymbol"
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
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
        refreshRunnable?.let { ratesRefreshHandler?.removeCallbacks(it) }
        ratesRefreshHandler = null
    }

    companion object {
        private const val TAG = "TAGY"
    }
}