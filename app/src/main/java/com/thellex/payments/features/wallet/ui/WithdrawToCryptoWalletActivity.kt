package com.thellex.payments.features.wallet.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.ErrorHandler
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.data.enums.UserErrorEnum
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
    }

    private fun observeWalletBalance() {
        walletManagerViewModel.walletBalance.observe(this) { balance ->
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
        val amount = binding.withdrawAmountEditText.text.toString().trim()
        if (amount.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        val tokenToUse = selectedToken ?: run {
            Toast.makeText(this, "No token selected for network", Toast.LENGTH_SHORT).show()
            return
        }

        val walletAddress = binding.withdrawCryptoWalletEdittextWalletAddress.text.toString().trim()
        if (walletAddress.isEmpty()) {
            Toast.makeText(this, "Enter wallet address", Toast.LENGTH_SHORT).show()
            return
        }

        val requestDto = CreateRequestPaymentDto(
            paymentType = PaymentType.WITHDRAW_CRYPTO,
            assetCode = selectedToken!!.assetCode,
            amount = amount,
            network = selectedToken!!.network,
            sourceAddress = selectedToken!!.address,
            fundUid = walletAddress,
        )

        lifecycleScope.launch {
            try {
                val response = ApiClient.getAuthenticatedPaymentApi(cachedToken.toString()).withdrawCrypto(requestDto)
                if (response.result != null) {
                    CustomToast.show(this@WithdrawToCryptoWalletActivity, message = "Withdrawal submitted")
                    Log.i(TAG, "Withdrawal successful: ${response.result}")
                }
            } catch (e: Exception) {
                val errorMessage = Helpers.getErrorMessageFromException(e)
                val userError = UserErrorEnum.fromCode(errorMessage)
                ErrorHandler.handle(this@WithdrawToCryptoWalletActivity, "Error", userError)
                Log.e(TAG, "Network error during withdrawal: $errorMessage", e)
            }
        }

    }

}
