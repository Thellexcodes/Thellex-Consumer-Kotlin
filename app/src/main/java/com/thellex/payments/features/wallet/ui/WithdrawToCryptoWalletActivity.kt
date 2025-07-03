package com.thellex.payments.features.wallet.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.thellex.payments.R
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.ErrorHandler
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.data.enums.PaymentErrorEnum
import com.thellex.payments.data.model.CreateRequestPaymentDto
import com.thellex.payments.databinding.ActivityWithdrawToCryptoWalletBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.pos.fragments.NetworkSelectionTopSheet
import com.thellex.payments.features.pos.fragments.NetworkTokenSelectionTopSheet
import com.thellex.payments.features.wallet.model.WalletDto
import com.thellex.payments.features.wallet.utils.WalletManagerModelFactory
import com.thellex.payments.features.wallet.utils.WalletManagerViewModel
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.settings.PaymentType
import com.thellex.payments.settings.SupportedBlockchainEnum
import kotlinx.coroutines.launch
import okio.blackholeSink
import java.util.Locale
import retrofit2.HttpException

class WithdrawToCryptoWalletActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TAG"
    }

    private lateinit var binding: ActivityWithdrawToCryptoWalletBinding
    private lateinit var userModel: UserViewModel
    private lateinit var walletManagerViewModel: WalletManagerViewModel
    private var cachedToken: String? = null

    private var selectedNetwork: SupportedBlockchainEnum? = SupportedBlockchainEnum.matic
    private var selectedToken: WalletDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawToCryptoWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        walletManagerViewModel = ViewModelProvider(
            this,
            WalletManagerModelFactory(applicationContext)
        )[WalletManagerViewModel::class.java]

        userModel.token.observe(this) { token ->
            cachedToken = token
        }

        observeWalletBalance()
        setupClickListeners()
        setupLiveValidation()

        binding.withdrawAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()?.toDoubleOrNull()
                val availableBalance = walletManagerViewModel.walletBalance.value?.totalInUsd ?: 0.0

                if (input != null && input > availableBalance) {
                    binding.withdrawAmountEditText.error = "Insufficient balance"
                } else {
                    binding.withdrawAmountEditText.error = null
                }
            }
        })

        binding.buttonMax.setOnClickListener {
            walletManagerViewModel.walletBalance.value?.let { balance ->
                val maxAmount = balance.totalInUsd.toString()
                binding.withdrawAmountEditText.setText(maxAmount)
            } ?: run {
                CustomToast.show(this, message = "Balance not available")
            }
        }

    }

    private fun observeWalletBalance() {
        walletManagerViewModel.walletBalance.observe(this) { balance ->
            val formatted = Helpers.formatBalance(balance.totalInUsd.toString())
            binding.textAvailableBalance.text = "Available: $formatted USD"
            val firstToken = balance.wallets.values.firstOrNull()
            firstToken?.let {
                updateSelectedNetworkAndToken(it)
                updateSpinnerUI(it)
                updateNetworkUI(it.network)
            }
        }
    }

    private fun setupClickListeners() {
        binding.withdrawCryptoWalletEdittextAmount.setOnClickListener {
            showNetworkSelection()
        }

        binding.withdrawSpinner.setOnClickListener {
            selectedNetwork?.let {
                showTokenSelectionForNetwork(it)
            } ?: Toast.makeText(this, "Please select a network first", Toast.LENGTH_SHORT).show()
        }

        binding.withdrawBtn.setOnClickListener {
            handleSubmitButtonClick()
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
            updateSelectedNetworkAndToken(selectedWallet)
            updateNetworkUI(selectedWallet.network)
            updateSpinnerUI(selectedWallet)
            showTokenSelectionForNetwork(selectedWallet.network)
            Log.d(TAG, "Network selected: ${selectedWallet.network.name}")
        }
    }

    private fun showTokenSelectionForNetwork(network: SupportedBlockchainEnum) {
        val wallets = walletManagerViewModel.walletBalance.value ?: return
        val tokensForNetwork = wallets.wallets.values.filter { it.network == network }
        if (tokensForNetwork.isEmpty()) {
            Toast.makeText(this, "No tokens found for the selected network", Toast.LENGTH_SHORT).show()
            return
        }
        val tokenSheet = NetworkTokenSelectionTopSheet(wallets.wallets, network) { token ->
            selectedToken = token
            updateSpinnerUI(token)
        }
        tokenSheet.show(supportFragmentManager, "NetworkTokenSelectionBottomSheet")
    }

    private fun updateSelectedNetworkAndToken(walletDto: WalletDto) {
        selectedNetwork = walletDto.network
        selectedToken = walletDto
    }

    private fun updateSpinnerUI(token: WalletDto) {
        val assetName = token.assetCode.name.uppercase(Locale.getDefault())
        binding.withdrawSpinnerText.text = assetName
        binding.withdrawSpinnerTickerIcon.setImageResource(Helpers.getIconResIdForToken(token.assetCode.name))
    }

    private fun updateNetworkUI(network: SupportedBlockchainEnum) {
        binding.withdrawCryptoWalletEdittextAmount.setText(Helpers.getDisplayNameForNetwork(network.name))
    }

    private fun handleSubmitButtonClick() {
        val amountStr = binding.withdrawAmountEditText.text.toString().trim()
        val walletAddress = binding.withdrawCryptoWalletEdittextWalletAddress.text.toString().trim()

        val isAmountValid = amountStr.isNotEmpty()
        val isWalletValid = Helpers.isValidEvmAddress(walletAddress)

        // Update background resources for inputs based on validation
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

        val availableBalance = walletManagerViewModel.walletBalance.value?.totalInUsd ?: 0.0
        if (amount > availableBalance) {
            CustomToast.show(this, "Warning", "Insufficient balance")
            return
        }

        val tokenToUse = selectedToken ?: run {
            CustomToast.show(this, "Warning", "No token selected")
            return
        }

        val requestDto = CreateRequestPaymentDto(
            paymentType = PaymentType.WITHDRAW_CRYPTO,
            assetCode = tokenToUse.assetCode,
            amount = amountStr,
            network = tokenToUse.network,
            sourceAddress = tokenToUse.address,
            fundUid = walletAddress,
        )

        lifecycleScope.launch {
            try {
                val response = ApiClient.getAuthenticatedPaymentApi(cachedToken.toString()).withdrawCrypto(requestDto)
                if (response.result != null) {
                    CustomToast.show(this@WithdrawToCryptoWalletActivity, "Success", "Withdrawal submitted")
                    Log.i(TAG, "Withdrawal successful: ${response.result}")
                }
            } catch (e: Exception) {
                val errorMessage = Helpers.getErrorMessageFromException(e)
                val userError = PaymentErrorEnum.fromCode(errorMessage)
                ErrorHandler.handle(this@WithdrawToCryptoWalletActivity, "Error", userError)
                Log.e(TAG, "Network error during withdrawal: $errorMessage", e)
            }
        }
    }
}
