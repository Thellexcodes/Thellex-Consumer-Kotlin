package com.thellex.payments.features.fiat

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.databinding.ActivityFiatRampTransactionsDetailBinding // Adjust package
import com.thellex.payments.core.utils.ActivityTracker // Adjust package
import com.thellex.payments.core.utils.Helpers // Adjust package
import android.util.Log
import android.view.View
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.roundToTwoDecimals
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.settings.FiatTickers

class FiatRampTransactionsDetailActivity : AppCompatActivity() {
    private lateinit var transactionId: String
    private lateinit var binding: ActivityFiatRampTransactionsDetailBinding
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiatRampTransactionsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        transactionId = intent.getStringExtra("ramp_id") ?: ""
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.rampTransactionDetailRoot.applyAdvancedSystemBarInsets()

        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = binding.rampTopAppBar.root,
            title = ""
        )

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        observeUser()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(transaction: IFiatCryptoRampTransactionsDto) {
        Log.d("Transaction", "this is fiat ramp transaction: $transaction")

        when (transaction.transactionType) {
            TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT -> {
                binding.onRampTransactionDetails.root.visibility = View.VISIBLE
                binding.offRampTransactionDetails.root.visibility = View.GONE
                with(binding.onRampTransactionDetails) {
                    onRampTransactionTypeValue.text = "DEPOSIT"
                }
            }
            TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> {
                binding.rampFiatAmount.text = "${transaction.recipientInfo.assetCode.uppercase()} ${transaction.mainAssetAmount.roundToTwoDecimals()}"
                binding.onRampTransactionDetails.root.visibility = View.GONE
                binding.offRampTransactionDetails.root.visibility = View.VISIBLE
                with(binding.offRampTransactionDetails) {
                    rampAmountSentValue.text = "${transaction.mainAssetAmount}"
                    rampServiceFeeValue.text = "${FiatTickers.getByCodeOrCountry("ngn")?.symbol}${transaction.serviceFeeAmountLocal.roundToTwoDecimals()} | ${
                        FiatTickers.getByCodeOrCountry("usd")?.symbol
                    }${transaction.serviceFeeAmountUSD.roundToTwoDecimals()}"
                    rampSenderAddressValue.text = Helpers.abbreviateAddress(transaction.recipientInfo.sourceAddress, startLength = 6, endLength = 6)
                    rampReceiverAmountValue.text = "${FiatTickers.getByCodeOrCountry("ngn")?.symbol}${transaction.netFiatAmount.roundToTwoDecimals()}"
                    rampReceiverAccountNumberValue.text = transaction.bankInfo.accountNumber
                    rampReceiverBankNameValue.text = transaction.bankInfo.bankName
                    rampReceiverAccountNameValue.text = transaction.bankInfo.accountHolder
                }
            }
            else -> {
                Log.e("FiatRampTransaction", "Unsupported transaction type: ${transaction.transactionType}")
                binding.onRampTransactionDetails.root.visibility = View.GONE
                binding.offRampTransactionDetails.root.visibility = View.GONE
            }
        }
    }

    private fun observeUser() {
        userViewModel.authResult.observe(this) { user ->
                val transaction = user?.fiatCryptoRampTransactions?.find { txn -> txn.id == transactionId }
                if (transaction != null) {
                    updateUI(transaction)
                } else {
                    binding.onRampTransactionDetails.root.visibility = View.GONE
                    binding.offRampTransactionDetails.root.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityTracker.remove(this)
    }
}