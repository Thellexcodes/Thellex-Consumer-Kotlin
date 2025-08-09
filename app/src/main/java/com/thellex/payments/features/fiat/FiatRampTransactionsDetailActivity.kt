package com.thellex.payments.features.fiat

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.databinding.ActivityFiatRampTransactionsDetailBinding // Adjust package
import com.thellex.payments.core.utils.ActivityTracker // Adjust package
import com.thellex.payments.core.utils.Helpers // Adjust package
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.formatTimestamp
import com.thellex.payments.core.utils.Helpers.roundToTwoDecimals
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.settings.FiatTickers

class FiatRampTransactionsDetailActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "FiatRampTransactionsDetail"
        private const val EXTRA_RAMP_ID = "ramp_id"
    }

    private lateinit var rampID: String
    private lateinit var binding: ActivityFiatRampTransactionsDetailBinding
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiatRampTransactionsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Validate rampID
        rampID = intent.getStringExtra(EXTRA_RAMP_ID) ?: run {
            finish()
            return
        }

        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.rampTransactionDetailRoot.applyAdvancedSystemBarInsets()

        // Initialize top bar
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = binding.rampTopAppBar.root,
            title = ""
        )

        // Initialize ViewModel
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
//        userViewModel.refreshAuthResult(this)
        observeUser()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(transaction: IFiatCryptoRampTransactionsDto) {
        // Update timestamp
        binding.rampTimestamp.text = formatTimestamp(transaction.createdAt)

        // Update status with color
        val statusEnum = Helpers.mapToTransactionStatus(transaction.paymentStatus.toString())
        binding.rampStatusLabel.apply {
            text = statusEnum.toString().uppercase()
            background = ContextCompat.getDrawable(context, R.drawable.status_background)?.apply {
                setTint(Helpers.getStatusColor(context, statusEnum))
            }
        }

        // Common formatting utilities
        fun formatAmount(amount: Double, currency: String): String = "$currency ${amount.roundToTwoDecimals()}"
        fun formatFees(localFee: Double, usdFee: Double): String =
            "${FiatTickers.getByCodeOrCountry("ngn")?.symbol}${localFee.roundToTwoDecimals()} | " +
                    "${FiatTickers.getByCodeOrCountry("usd")?.symbol}${usdFee.roundToTwoDecimals()}"

        when (transaction.transactionType) {
            TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT -> {
                binding.rampActionLabel.text = "YOU ARE BUYING"
                binding.rampAmount.text = formatAmount(transaction.netCryptoAmount, transaction.recipientInfo.assetCode.uppercase())
                binding.onRampTransactionDetails.root.visibility = View.VISIBLE
                binding.offRampTransactionDetails.root.visibility = View.GONE

                with(binding.onRampTransactionDetails) {
                    onRampTransactionTypeValue.text = "DEPOSIT"
                    onRampAmountSentValue.text = transaction.mainFiatAmount.roundToTwoDecimals().toString()
                    onRampReasonValue.text = formatAmount(transaction.netCryptoAmount, transaction.recipientInfo.assetCode.uppercase())
                    onRampAmountReceivedValue.text = transaction.netCryptoAmount.roundToTwoDecimals().toString()
                    onRampServiceFeeValue.text = formatFees(transaction.serviceFeeAmountLocal, transaction.serviceFeeAmountUSD)
                    onRampCryptoAddressValue.text = Helpers.abbreviateAddress(transaction.recipientInfo.destinationAddress, startLength = 6, endLength = 6)
                    onRampBankAccountValue.text = transaction.bankInfo.accountNumber
                    onRampBankNameValue.text = transaction.bankInfo.bankName
                    onRampBankAccountNameValue.text = transaction.bankInfo.accountHolder
                }
            }
            TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> {
                binding.rampActionLabel.text = "YOU ARE SPENDING"
                binding.rampAmount.text = formatAmount(transaction.mainAssetAmount, transaction.recipientInfo.assetCode.uppercase())
                binding.onRampTransactionDetails.root.visibility = View.GONE
                binding.offRampTransactionDetails.root.visibility = View.VISIBLE

                with(binding.offRampTransactionDetails) {
                    rampAmountSentValue.text = transaction.mainAssetAmount.roundToTwoDecimals().toString()
                    rampServiceFeeValue.text = formatFees(transaction.serviceFeeAmountLocal, transaction.serviceFeeAmountUSD)
                    rampSenderAddressValue.text = Helpers.abbreviateAddress(transaction.recipientInfo.sourceAddress, startLength = 6, endLength = 6)
                    rampReceiverAmountValue.text = formatAmount(transaction.netFiatAmount, FiatTickers.getByCodeOrCountry("ngn")?.symbol ?: "")
                    rampReceiverAccountNumberValue.text = transaction.bankInfo.accountNumber
                    rampReceiverBankNameValue.text = transaction.bankInfo.bankName
                    rampReceiverAccountNameValue.text = transaction.bankInfo.accountHolder
                }
            }
            else -> {
                Log.w(TAG, "Unknown transaction type: ${transaction.transactionType}")
                binding.onRampTransactionDetails.root.visibility = View.GONE
                binding.offRampTransactionDetails.root.visibility = View.GONE
            }
        }
    }

    private fun observeUser() {
        userViewModel.authResult.observe(this) { user ->
            if (user == null) {
                Log.w(TAG, "User is null, cannot display transaction details for rampID: $rampID")
                binding.onRampTransactionDetails.root.visibility = View.GONE
                binding.offRampTransactionDetails.root.visibility = View.GONE
                return@observe
            }

            Log.d(TAG, "Ramp Txns: ${user.fiatCryptoRampTransactions}")

            val transaction = user.fiatCryptoRampTransactions.find { it.id == rampID }
            if (transaction != null) {
                Log.d(TAG, "Found transaction for rampID: $rampID")
                updateUI(transaction)
            } else {
                Log.w(TAG, "Transaction not found for rampID: $rampID")
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