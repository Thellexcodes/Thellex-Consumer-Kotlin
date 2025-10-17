package com.thellex.pay.features.wallet.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.thellex.pay.R
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.CustomToast
import com.thellex.pay.core.utils.ErrorHandler
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.data.enums.PaymentErrorEnum
import com.thellex.pay.data.model.CreateRequestPaymentDto
import com.thellex.pay.databinding.ActivityWithdrawToCryptoWalletBinding
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.pos.fragments.NetworkSelectionTopSheet
import com.thellex.pay.features.wallet.adapters.TokenListByNetworkBottomSheet
import com.thellex.pay.features.wallet.model.WalletDto
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
import com.thellex.pay.network.services.ApiClient
import com.thellex.pay.settings.PaymentType
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokensEnum
import kotlinx.coroutines.launch
import java.util.Locale

class WithdrawToCryptoWalletActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: ActivityWithdrawToCryptoWalletBinding
    private lateinit var userModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel

    private var cachedToken: String? = null
    private var defaultToken: TokensEnum = TokensEnum.usdc

    private var selectedNetwork: SupportedBlockchainEnum? = SupportedBlockchainEnum.matic
    private var selectedToken: WalletDto? = null

    // QR Scan result launcher
    private val qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            binding.withdrawCryptoWalletEdittextWalletAddress.setText(result.contents)
        } else {
            CustomToast.show(this, "Info", "QR scan cancelled or failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawToCryptoWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.withdrawCryptoTopAppBar),
            title = "WITHDRAW"
        )

        userModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        // Restore state if available
        if (savedInstanceState != null) {
            val tokenName = savedInstanceState.getString("selectedTokenKey")
            val networkName = savedInstanceState.getString("selectedNetworkKey")

            tokenName?.let { token -> defaultToken = TokensEnum.valueOf(token) }
            networkName?.let { network -> selectedNetwork = SupportedBlockchainEnum.valueOf(network) }
        }

        userModel.token.observe(this) { token ->
            cachedToken = token
        }

        observeAndSetWalletBalance()
        setupClickListeners()
        setupLiveValidation()

        binding.withdrawAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()?.toDoubleOrNull()
                val currentBalance = selectedToken?.totalBalance?: 0.0
                if (input != null && input > currentBalance) {
                    binding.withdrawAmountEditText.error = "Insufficient balance"
                } else {
                    binding.withdrawAmountEditText.error = null
                }
            }
        })

        binding.buttonMax.setOnClickListener {
            selectedToken?.let {
                val maxAmount = it.totalBalance
                val editableAmount = Editable.Factory.getInstance().newEditable(maxAmount.toString())
                binding.withdrawAmountEditText.text = editableAmount
            } ?: run {
                CustomToast.show(this, "Warning", "Balance not available")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        observeAndSetWalletBalance()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("selectedTokenKey", selectedToken?.assetCode?.name)
        outState.putString("selectedNetworkKey", selectedNetwork?.name)
    }

    private fun observeAndSetWalletBalance() {
        walletManagerViewModel.walletBalance.observe(this) { balance ->
            val walletMap = balance?.wallets ?: return@observe

            val defaultWallet = walletMap.values.firstOrNull { it.assetCode == defaultToken }
                ?: walletMap.values.firstOrNull()

            if (selectedToken == null && defaultWallet != null) {
                selectedToken = defaultWallet
                defaultToken = defaultWallet.assetCode
                selectedNetwork = defaultWallet.network
                updateUIWithWallet(defaultWallet)
            } else {
                selectedToken?.let {
                    updateUIWithWallet(it)
                }
            }
        }
    }

    private fun updateUIWithWallet(wallet: WalletDto) {
        val formatted = Helpers.formatBalance(wallet.totalBalance.toString())
        binding.textAvailableBalance.text = "Available: $formatted ${wallet.assetCode.name.uppercase()}"
        updateSpinnerUI(wallet)
        updateNetworkUI(wallet.network)
    }

    private fun setupClickListeners() {
        binding.withdrawCryptoWalletEdittextAmount.setOnClickListener {
            showNetworkSelection()
        }

        binding.tokenSpinner.setOnClickListener {
            showTokenSelectionForNetwork()
        }

        binding.withdrawBtn.setOnClickListener {
            handleSubmitButtonClick()
        }

        binding.qrCodeScanner.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan wallet address QR code")
                setBeepEnabled(false)
                setOrientationLocked(false)
                captureActivity = com.journeyapps.barcodescanner.CaptureActivity::class.java
            }
            qrScannerLauncher.launch(options)
        }
    }

    private fun setupLiveValidation() {
        binding.withdrawAmountEditText.doOnTextChanged { text, _, _, _ ->
            val isValid = !text.isNullOrBlank()
            binding.withdrawAmountEditText.setBackgroundResource(
                if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
            )
        }

        binding.withdrawCryptoWalletEdittextWalletAddress.doOnTextChanged { text, _, _, _ ->
            val isValid = !text.isNullOrBlank()
            binding.withdrawCryptoWalletEdittextWalletAddress.setBackgroundResource(
                if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
            )
        }
    }

    private fun showNetworkSelection() {
        val wallets = walletManagerViewModel.walletBalance.value ?: return
        NetworkSelectionTopSheet.show(supportFragmentManager, wallets.wallets) { selectedWallet ->
            selectedToken = selectedWallet
            selectedNetwork = selectedWallet.network
            defaultToken = selectedWallet.assetCode
            updateUIWithWallet(selectedWallet)
//            showTokenSelectionForNetwork(selectedWallet.network)
        }
    }

    private fun showTokenSelectionForNetwork() {
        val walletBalance = walletManagerViewModel.walletBalance.value ?: return
        val selectedNetwork = this.selectedNetwork ?: return

        supportFragmentManager.setFragmentResultListener(
            TokenListByNetworkBottomSheet.RESULT_KEY,
            this
        ) { _, bundle ->
            val json = bundle.getString(TokenListByNetworkBottomSheet.TOKEN_KEY)
            val token = Gson().fromJson(json, WalletDto::class.java)
            updateUIWithWallet(token)
        }

        val bottomSheet = TokenListByNetworkBottomSheet.newInstance(walletBalance, selectedNetwork)
        bottomSheet.show(supportFragmentManager, TokenListByNetworkBottomSheet.TAG)
    }


    private fun updateSpinnerUI(token: WalletDto) {
        val assetName = token.assetCode.name.uppercase(Locale.getDefault())
        binding.withdrawSpinnerText.text = assetName
        binding.withdrawSpinnerTickerIcon.setImageResource(
            Helpers.getIconResIdForToken(token.assetCode.name)
        )
    }

    private fun updateNetworkUI(network: SupportedBlockchainEnum) {
        binding.withdrawCryptoWalletEdittextAmount.setText(
            SupportedBlockchainEnum.fromValue(network.name)?.let { Helpers.getDisplayNameForNetwork(it) }
        )
    }

    private fun handleSubmitButtonClick() {
        val amountStr = binding.withdrawAmountEditText.text.toString().trim()
        val walletAddress = binding.withdrawCryptoWalletEdittextWalletAddress.text.toString().trim()

        val isAmountValid = amountStr.isNotEmpty()
        val isWalletValid = Helpers.isValidEvmAddress(walletAddress)

        binding.withdrawAmountEditText.setBackgroundResource(
            if (isAmountValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
        )

        binding.withdrawCryptoWalletEdittextWalletAddress.setBackgroundResource(
            if (isWalletValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
        )

        if (!isAmountValid) {
            CustomToast.show(this, "Warning", "Amount is empty")
            return
        }

        if (!isWalletValid) {
            CustomToast.show(this, "Warning", "Invalid wallet address")
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            CustomToast.show(this, "Warning", "Invalid amount format")
            return
        }

        val selected = selectedToken ?: walletManagerViewModel.walletBalance.value?.wallets?.values
            ?.firstOrNull { it.assetCode == defaultToken }

        if (selected == null) {
            CustomToast.show(this, "Warning", "No token selected")
            return
        }

        val requestDto = CreateRequestPaymentDto(
            paymentType = PaymentType.WITHDRAW_CRYPTO,
            assetCode = selected.assetCode,
            amount = amountStr,
            network = selected.network,
            sourceAddress = selected.address,
            fundUid = walletAddress,
        )

        setLoadingState(true)

        lifecycleScope.launch {
            setLoadingState(true)

            try {
                val response = ApiClient.getAuthenticatedPaymentApi(this@WithdrawToCryptoWalletActivity, cachedToken.toString())
                    .withdrawCrypto(requestDto)

                val result = response.body()?.result

                if (response.isSuccessful && result != null) {
                    userModel.addTransaction(result)

                    CustomToast.show(
                        this@WithdrawToCryptoWalletActivity,
                        "Success",
                        "Withdrawal submitted"
                    )

                    Log.i(TAG, "Withdrawal successful: $result")

                    val intent = Intent(
                        this@WithdrawToCryptoWalletActivity,
                        TransactionSuccessActivity::class.java
                    ).apply {
                        putExtra("destinationAddress", result.destinationAddress)
                        putExtra("recipientAmount", "${result.amount} ${result.assetCode.uppercase()}")
                    }

                    startActivity(intent)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Something went wrong"
                    Log.e(TAG, "Withdrawal failed: $errorMsg")
                    ErrorHandler.handle(this@WithdrawToCryptoWalletActivity, "Failed", PaymentErrorEnum.fromCode(errorMsg))
                }
            } catch (e: Exception) {
                val errorMessage = Helpers.getErrorMessageFromException(e)
                val userError = PaymentErrorEnum.fromCode(errorMessage)

                ErrorHandler.handle(this@WithdrawToCryptoWalletActivity, "Error", userError)
                Log.e(TAG, "Network error during withdrawal: $errorMessage", e)
            } finally {
                setLoadingState(false)
            }
        }

    }

    private fun setLoadingState(isLoading: Boolean) {
        // Keep layout enabled so children are visible
        binding.withdrawBtn.isEnabled = true

        // Disable clicks & focus when loading
        binding.withdrawBtn.isClickable = !isLoading
        binding.withdrawBtn.isFocusable = !isLoading

        // Disable input fields
        binding.withdrawAmountEditText.isEnabled = !isLoading
        binding.withdrawCryptoWalletEdittextWalletAddress.isEnabled = !isLoading

        // Show or hide spinner
        binding.withdrawProgress.visibility = if (isLoading) View.VISIBLE else View.GONE

        // Update button text and text color
        binding.withdrawBtnText.text = if (isLoading) "PROCESSING" else getString(R.string.withdraw)
        binding.withdrawBtnText.setTextColor(
            if (isLoading) ContextCompat.getColor(this, R.color.white)
            else ContextCompat.getColor(this, R.color.darkBlue)
        )

        // Change background drawable based on state
        binding.withdrawBtn.setBackgroundResource(
            if (isLoading) R.drawable.button_riple_darkblue else R.drawable.button_ripple_golden_yellow
        )
    }

    companion object {
        private const val TAG = "TAG"
    }

}

