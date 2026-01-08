package com.thellex.pay.features.wallet.ui

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.BrightSkyBlue
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.utils.Helpers.truncateMiddle
import com.thellex.pay.data.model.CreateRequestPaymentDto
import com.thellex.pay.features.auth.viewModel.UserRepository
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.network.services.ApiClient
import com.thellex.pay.settings.PaymentType
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import com.thellex.pay.shared.BackIconButton
import com.thellex.pay.shared.CenteredTopBar
import com.thellex.pay.shared.InfoCard
import com.thellex.pay.shared.InfoCardType
import com.thellex.pay.shared.PrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class CryptoTransactionSummary(
    val amount: String,
    @Serializable val assetCode: TokenEnum,
    val valueInUsd: Double? = null,
    val valueInLocal: Double? = null,
    val sourceAddress: String,
    val fundUid: String,
    @Serializable val network: SupportedBlockchainEnum,
    val networkName: String,
    val networkFee: Double,
    val reason: String
)

@Composable
fun TransactionSummaryList(
    showTitle: Boolean? = false,
    transaction: CryptoTransactionSummary,
    modifier: Modifier = Modifier
) {
   val items = listOf(
        "Recipient" to transaction.fundUid.truncateMiddle(),
        "Network" to "${transaction.network}".uppercase(),
        "Network Fee" to "${transaction.networkFee} ${transaction.assetCode.name.uppercase()}",
        "Reason" to transaction.reason.uppercase()
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkBlue)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        if(showTitle!!) {
            Text(
                text = "transaction details".uppercase(),
                fontFamily = KumbhSansFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }

        items.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    color = SteelBlueGrey,
                    fontSize = 12.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )

                if (label == "Reason") {
                    if (label == "Reason") {

                        val reasonText = value
                            .toString()
                            .takeIf { it.isNotBlank() }
                            ?.uppercase()
                            ?: "REASON / BILLS"

                        Box(
                            modifier = Modifier
                                .background(
                                    color = BrightSkyBlue,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = reasonText,
                                color = White,
                                fontSize = 10.sp,
                                fontFamily = KumbhSansFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

//                    Box(
//                        modifier = Modifier
//                            .background(
//                                color = BrightSkyBlue,
//                                shape = RoundedCornerShape(6.dp)
//                            )
//                            .padding(horizontal = 8.dp, vertical = 4.dp)
//                    ) {
//                        Text(
//                            text = value.toString(),
//                            color = White,
//                            fontSize = 10.sp,
//                            fontFamily = KumbhSansFontFamily,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
                } else {
                    Text(
                        text = value.toString(),
                        color = SteelBlueGrey,
                        fontSize = 12.sp,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun CryptoWithdrawalReview(
    navController: NavController,
    transaction: CryptoTransactionSummary? = null,
    authToken: String? = ""
) {
    if (transaction == null || authToken == null) return

    val application = LocalContext.current.applicationContext as Application

    val coroutineScope = rememberCoroutineScope()

    AppGradientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
            ) {

                CenteredTopBar(
                    title = "Confirm Order",
                    onBackClick = { navController.popBackStack() }
                )

                Spacer(modifier = Modifier.height(88.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Amount section
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, GoldenYellow, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "AMOUNT", color = White)
                        }

                        Spacer(modifier = Modifier.height(13.dp))

                        Text(
                            text = "${transaction.amount} ${transaction.assetCode}".uppercase(),
                            color = White,
                            fontSize = 32.sp,
                            fontFamily = KumbhSansFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(9.dp))

                        Text(
                            text = "USD ${transaction.valueInUsd ?: 0.0}",
                            color = SteelBlueGrey,
                            fontSize = 16.sp,
                            fontFamily = KumbhSansFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TransactionSummaryList(
                        transaction = transaction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Info card sits above the button
                InfoCard(
                    text = "Ensure that the address is correct and on the same network. Transactions cannot be cancelled.",
                    type = InfoCardType.INFO,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // ───── BOTTOM BUTTON ─────
                PrimaryButton(
                    text = "CONFIRM",
                    onClick = {
                        coroutineScope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    Log.d("CryptoWithdrawal", "Payload: ${transaction.network}")

                                    val sourceAddress = transaction.sourceAddress
                                        ?: throw IllegalStateException("sourceAddress is null")

                                    val fundUid = transaction.fundUid
                                        ?: throw IllegalStateException("fundUid is null")

                                    val payload = CreateRequestPaymentDto(
                                        paymentType = PaymentType.WITHDRAW_CRYPTO,
                                        assetCode = transaction.assetCode,
                                        amount = transaction.amount,
                                        network = transaction.network,
                                        sourceAddress = sourceAddress,
                                        fundUid = fundUid
                                    )

                                    val response = ApiClient
                                        .getAuthenticatedPaymentApi(application, authToken)
                                        .withdrawCrypto(payload)

                                    if (!response.isSuccessful) {
                                        throw IllegalStateException(
                                            "Withdrawal failed ${response.code()} ${response.errorBody()?.string()}"
                                        )
                                    }

                                    val intent = Intent(application, TransactionSuccessActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }

                                    application.startActivity(intent)
                                }

                            } catch (e: Exception) {
                                Log.e("CryptoWithdrawal", "Withdrawal failed", e)
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CryptoWithdrawalReviewRoute(
    navController: NavController,
    transaction: CryptoTransactionSummary? = null
) {

    val application = LocalContext.current.applicationContext as Application
    val factory = UserViewModelFactory(application)
    val userViewModel: UserViewModel = viewModel(factory = factory)
    val authToken by userViewModel.token.observeAsState()

    CryptoWithdrawalReview(navController, transaction, authToken)
}

@Preview(showBackground = true)
@Composable
fun CryptoWithdrawalReviewPreview() {
    val dummyTransaction = CryptoTransactionSummary(
        amount = "1 USDT",
        assetCode = TokenEnum.usdt,
        valueInUsd = 1.0,
        valueInLocal = 500.0,
        sourceAddress = "0xA1B2...9F3E",
        fundUid = "0xA1B2...9F3E",
        networkName = "Ethereum",
        networkFee = 0.003,
        network = SupportedBlockchainEnum.ethereum,
        reason = "BILLS"
    )

    CryptoWithdrawalReview(
        navController = rememberNavController(),
        transaction = dummyTransaction
    )
}
