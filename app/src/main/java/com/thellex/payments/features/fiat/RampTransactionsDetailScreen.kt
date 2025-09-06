package com.thellex.payments.features.fiat

import CustomTopAppBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.thellex.payments.core.decorators.DeepNavy
import com.thellex.payments.core.decorators.GrayText
import com.thellex.payments.core.decorators.KumbhSansFontFamily
import com.thellex.payments.core.decorators.Orange
import com.thellex.payments.core.decorators.OutfitFontFamily
import com.thellex.payments.core.utils.Helpers.convertToLocalTime
import com.thellex.payments.data.model.PaymentStatusEnum

//class FiatRampTransactionsDetailActivity : AppCompatActivity() {
//    companion object {
//        private const val TAG = "FiatRampTransactionsDetail"
//        private const val RAMP_ID = "ramp_id"
//    }
//
//    private lateinit var rampID: String
//    private lateinit var binding: ActivityFiatRampTransactionsDetailBinding
//    private lateinit var topBar: Helpers.TopAppBarController
//    private lateinit var userViewModel: UserViewModel
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityFiatRampTransactionsDetailBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Validate rampID
//        rampID = intent.getStringExtra(RAMP_ID) ?: run {
//            Log.e(TAG, "No rampID provided, finishing activity")
//            finish()
//            return
//        }
//
//        ActivityTracker.add(this)
//        disableDecorFitsSystemWindows()
//        setTransparentStatusBarWithWhiteIcons()
//        binding.rampTransactionDetailRoot.applyAdvancedSystemBarInsets()
//
//        // Initialize top bar
//        topBar = Helpers.setupTopAppBar(
//            activity = this,
//            rootView = binding.rampTopAppBar.root,
//            title = ""
//        )
//
//        // Initialize ViewModel
//        userViewModel = ViewModelProvider(
//            this,
//            UserViewModelFactory(applicationContext)
//        )[UserViewModel::class.java]
//
//        observeUser()
//        observeFiatCryptoTransactionUpdate()
//    }
//
//    private fun observeUser() {
//        userViewModel.authResult.observe(this) { user ->
//            if (user == null) {
//                Log.w(TAG, "User is null, cannot display transaction details for rampID: $rampID")
//                binding.onRampTransactionDetails.root.visibility = View.GONE
//                binding.offRampTransactionDetails.root.visibility = View.GONE
//                return@observe
//            }
//
//            // Log all fiatCryptoRampTransactions
//            Log.d(TAG, "All fiatCryptoRampTransactions for user ${user.uid} (rampID: $rampID):")
//            user.fiatCryptoRampTransactions?.forEachIndexed { index, transaction ->
//                Log.d(
//                    TAG,
//                    "Transaction [$index]: id=${transaction.id}, type=${transaction.transactionType?.value ?: "Unknown"}, " +
//                            "status=${transaction.paymentStatus?.toString() ?: "Unknown"}, assetCode=${transaction.recipientInfo?.assetCode ?: "Unknown"}, " +
//                            "amount=${transaction.netCryptoAmount ?: transaction.mainAssetAmount ?: "Unknown"}"
//                )
//            }
//
//            // Try finding the transaction immediately
//            var transaction = user.fiatCryptoRampTransactions?.find { it.id == rampID }
//
//            if (transaction != null) {
//                Log.d(TAG, "Found transaction for rampID: $rampID, type: ${transaction.transactionType?.value ?: "Unknown"}")
//                updateUI(transaction)
//            } else {
//                // Retry with a 500ms delay
//                CoroutineScope(Dispatchers.Main).launch {
//                    Log.d(TAG, "Transaction not found for rampID: $rampID, retrying with 500ms delay")
//                    withTimeoutOrNull(500) {
//                        delay(500)
//                        val updatedUser = userViewModel.authResult.value
//                        if (updatedUser != null) {
//                            transaction = updatedUser.fiatCryptoRampTransactions?.find { it.id == rampID }
//                            if (transaction != null) {
//                                Log.d(TAG, "Found transaction after delay for rampID: $rampID, type: ${transaction!!.transactionType?.value ?: "Unknown"}")
//                                updateUI(transaction!!)
//                            }
//                        }
//                    }
//                    if (transaction == null) {
//                        Log.w(TAG, "Transaction still not found for rampID: $rampID after delay")
//                        binding.onRampTransactionDetails.root.visibility = View.GONE
//                        binding.offRampTransactionDetails.root.visibility = View.GONE
//                    }
//                }
//            }
//        }
//    }
//
//    private fun observeFiatCryptoTransactionUpdate() {
//        EventBus.fiatCryptoTransactionUpdate.observe(this) { transaction ->
//            if (transaction.id == rampID) {
//                Log.d(TAG, "Received EventBus update for rampID: $rampID, type: ${transaction.transactionType?.value ?: "Unknown"}")
//                updateUI(transaction)
//            } else {
//                Log.d(TAG, "Received EventBus update for transaction ${transaction.id}, but rampID is $rampID")
//            }
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun updateUI(transaction: IFiatCryptoRampTransactionsDto) {
//        // Update timestamp
//        binding.rampTimestamp.text = formatTimestamp(transaction.createdAt)
//
//        // Update status with color
//        val statusEnum = Helpers.mapToTransactionStatus(transaction.paymentStatus.toString())
//        binding.rampStatusLabel.apply {
//            text = statusEnum.toString().uppercase()
//            background = ContextCompat.getDrawable(context, R.drawable.status_background)?.apply {
//                setTint(Helpers.getStatusColor(context, statusEnum))
//            }
//        }
//
//        // Common formatting utilities
//        fun formatAmount(amount: Double, currency: String): String = "$currency ${amount.roundToTwoDecimals()}"
//        fun formatFees(localFee: Double, usdFee: Double): String =
//            "${FiatTickers.getByCodeOrCountry("ngn")?.symbol}${localFee.roundToTwoDecimals()} | " +
//                    "${FiatTickers.getByCodeOrCountry("usd")?.symbol}${usdFee.roundToTwoDecimals()}"
//
//        when (transaction.transactionType) {
//            TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT -> {
//                binding.rampActionLabel.text = "YOU ARE BUYING"
//                binding.rampAmount.text = formatAmount(transaction.netCryptoAmount, transaction.recipientInfo.assetCode.uppercase())
//                binding.onRampTransactionDetails.root.visibility = View.VISIBLE
//                binding.offRampTransactionDetails.root.visibility = View.GONE
//
//                with(binding.onRampTransactionDetails) {
//                    onRampTransactionTypeValue.text = "DEPOSIT"
//                    onRampAmountSentValue.text = transaction.mainFiatAmount.roundToTwoDecimals().toString()
//                    onRampReasonValue.text = formatAmount(transaction.netCryptoAmount, transaction.recipientInfo.assetCode.uppercase())
//                    onRampAmountReceivedValue.text = transaction.netCryptoAmount.roundToTwoDecimals().toString()
//                    onRampServiceFeeValue.text = formatFees(transaction.serviceFeeAmountLocal, transaction.serviceFeeAmountUSD)
//                    onRampCryptoAddressValue.text = Helpers.abbreviateAddress(transaction.recipientInfo.destinationAddress, startLength = 6, endLength = 6)
//                    onRampBankAccountValue.text = transaction.bankInfo.accountNumber
//                    onRampBankNameValue.text = transaction.bankInfo.bankName
//                    onRampBankAccountNameValue.text = transaction.bankInfo.accountHolder
//                }
//            }
//            TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> {
//                binding.rampActionLabel.text = "YOU ARE SPENDING"
//                binding.rampAmount.text = formatAmount(transaction.mainAssetAmount, transaction.recipientInfo.assetCode.uppercase())
//                binding.onRampTransactionDetails.root.visibility = View.GONE
//                binding.offRampTransactionDetails.root.visibility = View.VISIBLE
//
//                with(binding.offRampTransactionDetails) {
//                    rampAmountSentValue.text = transaction.mainAssetAmount.roundToTwoDecimals().toString()
//                    rampServiceFeeValue.text = formatFees(transaction.serviceFeeAmountLocal, transaction.serviceFeeAmountUSD)
//                    rampSenderAddressValue.text = Helpers.abbreviateAddress(transaction.recipientInfo.sourceAddress, startLength = 6, endLength = 6)
//                    rampReceiverAmountValue.text = formatAmount(transaction.netFiatAmount, FiatTickers.getByCodeOrCountry("ngn")?.symbol ?: "")
//                    rampReceiverAccountNumberValue.text = transaction.bankInfo.accountNumber
//                    rampReceiverBankNameValue.text = transaction.bankInfo.bankName
//                    rampReceiverAccountNameValue.text = transaction.bankInfo.accountHolder
//                }
//            }
//            else -> {
//                Log.w(TAG, "Unknown transaction type: ${transaction.transactionType}")
//                binding.onRampTransactionDetails.root.visibility = View.GONE
//                binding.offRampTransactionDetails.root.visibility = View.GONE
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        ActivityTracker.remove(this)
//    }
//}

data class TransactionDetail(
    val rampId: String,
    val description: String,
    val amount: String,
    val status: PaymentStatusEnum,
    val statusColor: Color,
    val timestamp: String,
    val assetCode: String
)

// Transaction Detail Screen
@Composable
fun RampTransactionDetailScreen(
    navController: NavHostController,
    rampId: String?
) {
    // Sample ViewModel or data fetching (replace with actual implementation)
    val transactionDetail by produceState<TransactionDetail?>(initialValue = null) {
        // Simulate fetching data based on rampId
        val sampleDetail = rampId?.let {
            TransactionDetail(
                rampId = it,
                description = "Purchase of $it",
                amount = "500.00 USD",
                status = PaymentStatusEnum.Processing,
                statusColor = Orange,
                timestamp = convertToLocalTime("2025-09-06T09:00:00.000Z"),
                assetCode = "USDC"
            )
        }
        value = sampleDetail
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CustomTopAppBar(
                title = "Transaction Detail",
                onBackClick = { navController.popBackStack() }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepNavy)
                    .padding(paddingValues)
            ) {
                transactionDetail?.let { detail ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Transaction ID
                            Text(
                                text = "Transaction ID: ${detail.rampId}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = OutfitFontFamily
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Description
                            Text(
                                text = "Description: ${detail.description}",
                                color = GrayText,
                                fontSize = 14.sp,
                                fontFamily = KumbhSansFontFamily
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Amount",
                                    color = GrayText,
                                    fontSize = 14.sp,
                                    fontFamily = KumbhSansFontFamily
                                )
                                Text(
                                    text = detail.amount,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = OutfitFontFamily
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Status",
                                    color = GrayText,
                                    fontSize = 14.sp,
                                    fontFamily = KumbhSansFontFamily
                                )
                                Text(
                                    text = detail.status.value.uppercase(),
                                    color = detail.statusColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = OutfitFontFamily
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Timestamp
                            Text(
                                text = "Date: ${detail.timestamp}",
                                color = GrayText,
                                fontSize = 14.sp,
                                fontFamily = KumbhSansFontFamily
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Asset Code
                            Text(
                                text = "Asset: ${detail.assetCode}",
                                color = GrayText,
                                fontSize = 14.sp,
                                fontFamily = KumbhSansFontFamily
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                } ?: run {
                    // Loading or error state
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        CircularProgressIndicator(color = Orange)
                    }
                }
            }
        }
    )
}