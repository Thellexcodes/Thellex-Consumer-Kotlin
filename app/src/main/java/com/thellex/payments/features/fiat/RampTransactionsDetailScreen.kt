package com.thellex.payments.features.fiat

import CustomTopAppBar
import android.app.Application
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.thellex.payments.R
import com.thellex.payments.core.decorators.BrightSkyBlue
import com.thellex.payments.core.decorators.GoldenYellow
import com.thellex.payments.core.decorators.KumbhSansFontFamily
import com.thellex.payments.core.decorators.Midnight
import com.thellex.payments.core.decorators.OutfitFontFamily
import com.thellex.payments.core.decorators.SteelBlueGrey
import com.thellex.payments.core.decorators.White
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.convertToLocalTime
import com.thellex.payments.core.utils.Helpers.truncateToTwoDecimals
import com.thellex.payments.core.utils.PaddedWrapper
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.settings.FiatTickers

data class TransactionDetail(
    val rampId: String,
    val description: String,
    val paymentStatusEnum: PaymentStatusEnum,
    val statusColor: Color,
    val timestamp: String,
    val assetCode: String,
    val transactionType: TransactionTypeEnum,
    val mainFiatAmount: Double,
    val netFiatAmount: Double,
    val netCryptoAmount: Double,
)

// Transaction Detail Screen
@Composable
fun RampTransactionDetailScreen(
    navController: NavHostController,
    rampId: String?
) {
    val application = LocalContext.current.applicationContext as Application
    val factory = UserViewModelFactory(application)
    val userViewModel: UserViewModel = viewModel(factory = factory)
    val authResult by userViewModel.authResult.observeAsState()

    val transaction = authResult?.fiatCryptoRampTransactions?.find { it.id == rampId }

    // Map to TransactionDetail
    val transactionDetail = transaction?.let {
        TransactionDetail(
            rampId = it.id,
            description = it.transactionMessage ?: "No description",
            paymentStatusEnum = it.paymentStatus,
            statusColor = SteelBlueGrey,
            timestamp = convertToLocalTime(it.createdAt),
            assetCode = it.recipientInfo.assetCode,
            transactionType = it.transactionType,
            mainFiatAmount = it.mainFiatAmount,
            netFiatAmount = it.netFiatAmount,
            netCryptoAmount = it.netCryptoAmount,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CustomTopAppBar(
                title = "",
                onBackClick = { navController.popBackStack() },
                backgroundColor = Color.Transparent
            )
        },
        content = { paddingValues ->
            PaddedWrapper(paddingValues = paddingValues) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    transactionDetail?.let { detail ->
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            item {
                                    when(transaction.transactionType){
                                        TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT -> {

                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(GoldenYellow, shape = RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        detail.paymentStatusEnum.value.uppercase(),
                                                        fontSize = 10.sp,
                                                        fontFamily = KumbhSansFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        color = White
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    detail.description.uppercase(),
                                                    fontSize = 12.sp,
                                                    fontFamily = KumbhSansFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SteelBlueGrey
                                                )
                                                Text(
                                                    Helpers.formatRampAmount(transaction.netCryptoAmount.truncateToTwoDecimals(), transaction.recipientInfo.assetCode.uppercase())
                                                        .uppercase(),
                                                    fontSize = 32.sp,
                                                    fontFamily = OutfitFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = White
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    "${FiatTickers.getByCodeOrCountry("ngn")?.symbol} ${detail.mainFiatAmount}".uppercase(),
                                                    fontSize = 14.sp,
                                                    fontFamily = KumbhSansFontFamily,
                                                    fontWeight = FontWeight.Normal,
                                                    color = White
                                                )
                                                Spacer(Modifier.height(4.dp))
//                                                Text(
//                                                    "2 jul, 3:07pm".uppercase(),
//                                                    fontSize = 10.sp,
//                                                    fontFamily = KumbhSansFontFamily,
//                                                    fontWeight = FontWeight.Light,
//                                                    color = White
//                                                )
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Transaction Details Header
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "transaction details".uppercase(),
                                                    fontSize = 14.sp,
                                                    fontFamily = KumbhSansFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = White
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Column(
                                                modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SteelBlueGrey, shape = RoundedCornerShape(8.dp))
                                                .padding(16.dp)
                                            ) {
                                                val details = listOf(
                                                    "Transaction Type" to "Fiat-Crypto",
                                                    "Amount Sent" to Helpers.formatRampAmount(
                                                        transaction.netCryptoAmount,
                                                        transaction.recipientInfo.assetCode.uppercase()
                                                    ),
                                                    "Amount Sent (NGN)" to "${
                                                        FiatTickers.getByCodeOrCountry(
                                                            "ngn"
                                                        )?.symbol
                                                    } ${transaction.mainFiatAmount.truncateToTwoDecimals()}",
                                                    "Service Fee" to Helpers.formatFees(
                                                        transaction.serviceFeeAmountLocal,
                                                        transaction.serviceFeeAmountUSD
                                                    ),
                                                    "Network" to transaction.recipientInfo.network.uppercase(),
                                                    "Reason" to transaction.paymentReason.uppercase()
                                                )

                                                details.forEach { (label, value) ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            label,
                                                            fontSize = 12.sp,
                                                            fontFamily = KumbhSansFontFamily,
                                                            fontWeight = FontWeight.Medium,
                                                            color = White
                                                        )

//                                                        if (label == "Rate") {
//                                                            Text(
//                                                                text = "${FiatTickers.getByCodeOrCountry("ngn")?.symbol}$value",
//                                                                fontSize = 12.sp,
//                                                                fontFamily = KumbhSansFontFamily,
//                                                                fontWeight = FontWeight.Bold,
//                                                                color = White
//                                                            )
//                                                        } else
                                                        if (label == "Network") {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Image(
                                                                    painter = painterResource(id = Helpers.getIconResIdForBlockchain(value)),
                                                                    contentDescription = "Stellar Logo",
                                                                    modifier = Modifier
                                                                        .size(16.dp)
                                                                        .padding(end = 4.dp)
                                                                )
                                                                Text(
                                                                    value,
                                                                    fontSize = 12.sp,
                                                                    fontFamily = KumbhSansFontFamily,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = White
                                                                )
                                                            }
                                                        } else if (label == "Reason") {
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(
                                                                        BrightSkyBlue,
                                                                        shape = RoundedCornerShape(4.dp)
                                                                    )
                                                                    .padding(
                                                                        horizontal = 8.dp,
                                                                        vertical = 4.dp
                                                                    )
                                                            ) {
                                                                Text(
                                                                    value,
                                                                    fontSize = 12.sp,
                                                                    fontFamily = KumbhSansFontFamily,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = White
                                                                )
                                                            }
                                                        } else {
                                                            Text(
                                                                value.uppercase(),
                                                                fontSize = 12.sp,
                                                                fontFamily = KumbhSansFontFamily,
                                                                fontWeight = FontWeight.Bold,
                                                                color = White
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                }

                                                // Bank Payment Details Card
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Midnight,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(16.dp)
                                                ) {
                                                    Text(
                                                        "bank payment details".uppercase(),
                                                        color = White
                                                    )
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    val bankDetails = listOf(
                                                        "Amount to purchase (₦)" to "${transaction.mainFiatAmount.truncateToTwoDecimals()}",
                                                        "Bank Name" to transaction.bankInfo.bankName,
                                                        "Account Number" to transaction.bankInfo.accountNumber,
                                                        "Account Name" to transaction.bankInfo.accountHolder
                                                    )
                                                    bankDetails.forEach { (label, value) ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                label,
                                                                fontSize = 12.sp,
                                                                fontFamily = KumbhSansFontFamily,
                                                                fontWeight = FontWeight.Medium,
                                                                color = White
                                                            )
                                                            Text(
                                                                value,
                                                                fontSize = 12.sp,
                                                                fontFamily = KumbhSansFontFamily,
                                                                fontWeight = FontWeight.Bold,
                                                                color = White
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(10.dp))
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Midnight,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(16.dp)
                                                ) {
                                                    Text(
                                                        "recipient’s details".uppercase(),
                                                        color = White
                                                    )
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    val recipientDetails = listOf(
                                                        "Wallet Address" to Helpers.abbreviateAddress(
                                                            transaction.recipientInfo.destinationAddress,
                                                            startLength = 6,
                                                            endLength = 6
                                                        ),
                                                    )
                                                    recipientDetails.forEach { (label, value) ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                label,
                                                                fontSize = 12.sp,
                                                                fontFamily = KumbhSansFontFamily,
                                                                fontWeight = FontWeight.Medium,
                                                                color = White
                                                            )
                                                            Text(
                                                                value,
                                                                fontSize = 12.sp,
                                                                fontFamily = KumbhSansFontFamily,
                                                                fontWeight = FontWeight.Bold,
                                                                color = SteelBlueGrey
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(10.dp))
                                                    }
                                                }
                                            }
                                    }
                                   TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> {
                                       Column(
                                           modifier = Modifier.fillMaxWidth(),
                                           horizontalAlignment = Alignment.CenterHorizontally
                                       ) {
                                           Box(
                                               modifier = Modifier
                                                   .background(GoldenYellow, shape = RoundedCornerShape(6.dp))
                                                   .padding(horizontal = 8.dp, vertical = 4.dp)
                                           ) {
                                               Text(
                                                   detail.paymentStatusEnum.value.uppercase(),
                                                   fontSize = 10.sp,
                                                   fontFamily = KumbhSansFontFamily,
                                                   fontWeight = FontWeight.Bold,
                                                   color = White
                                               )
                                           }
                                           Spacer(Modifier.height(4.dp))
                                           Text(
                                               detail.description.uppercase(),
                                               fontSize = 12.sp,
                                               fontFamily = KumbhSansFontFamily,
                                               fontWeight = FontWeight.Bold,
                                               color = SteelBlueGrey
                                           )
                                           Text(
                                               Helpers.formatRampAmount(transaction.netCryptoAmount.truncateToTwoDecimals(), transaction.recipientInfo.assetCode)
                                                   .uppercase(),
                                               fontSize = 32.sp,
                                               fontFamily = OutfitFontFamily,
                                               fontWeight = FontWeight.Bold,
                                               color = White
                                           )
                                           Spacer(Modifier.height(2.dp))
                                           Text(
                                               "${FiatTickers.getByCodeOrCountry("ngn")?.symbol} ${detail.netFiatAmount}".uppercase(),
                                               fontSize = 14.sp,
                                               fontFamily = KumbhSansFontFamily,
                                               fontWeight = FontWeight.Normal,
                                               color = White
                                           )
                                           Spacer(Modifier.height(4.dp))
//                                           Text(
//                                               "2 jul, 3:07pm".uppercase(),
//                                               fontSize = 10.sp,
//                                               fontFamily = KumbhSansFontFamily,
//                                               fontWeight = FontWeight.Light,
//                                               color = White
//                                           )
                                       }

                                       Spacer(modifier = Modifier.height(16.dp))

                                       // Transaction Details Header
                                       Box(
                                           modifier = Modifier
                                               .fillMaxWidth(),
                                           contentAlignment = Alignment.Center
                                       ) {
                                           Text(
                                               "transaction details".uppercase(),
                                               fontSize = 14.sp,
                                               fontFamily = KumbhSansFontFamily,
                                               fontWeight = FontWeight.Bold,
                                               color = White
                                           )
                                       }
                                       Spacer(modifier = Modifier.height(16.dp))
                                       Column(
                                           modifier = Modifier
                                               .fillMaxWidth()
                                               .background(SteelBlueGrey, shape = RoundedCornerShape(8.dp))
                                               .padding(16.dp)
                                       ) {
                                           val details = listOf(
                                               "Transaction Type" to "Crypto-Fiat",
                                               "Amount Sent" to Helpers.formatRampAmount(
                                                   transaction.netCryptoAmount,
                                                   transaction.recipientInfo.assetCode.uppercase()
                                               ),
//                                               "Rate" to  "${transaction.rate}",
                                               "Amount to Receive (NGN)" to "${
                                                   FiatTickers.getByCodeOrCountry(
                                                       "ngn"
                                                   )?.symbol
                                               } ${transaction.netFiatAmount.truncateToTwoDecimals()}",
                                               "Service Fee" to Helpers.formatFees(
                                                   transaction.serviceFeeAmountLocal,
                                                   transaction.serviceFeeAmountUSD
                                               ),
                                               "Network" to transaction.recipientInfo.network.uppercase(),
                                               "Reason" to transaction.paymentReason.uppercase()
                                           )

                                           details.forEach { (label, value) ->
                                               Row(
                                                   modifier = Modifier.fillMaxWidth(),
                                                   horizontalArrangement = Arrangement.SpaceBetween,
                                                   verticalAlignment = Alignment.CenterVertically
                                               ) {
                                                   Text(
                                                       label,
                                                       fontSize = 12.sp,
                                                       fontFamily = KumbhSansFontFamily,
                                                       fontWeight = FontWeight.Medium,
                                                       color = White
                                                   )
//                                                   if (label == "Rate") {
//                                                       Text(
//                                                           text = "${FiatTickers.getByCodeOrCountry("ngn")?.symbol} $value",
//                                                           fontSize = 12.sp,
//                                                           fontFamily = KumbhSansFontFamily,
//                                                           fontWeight = FontWeight.Bold,
//                                                           color = White
//                                                       )
//                                                   } else
                                                   if (label == "Network") {
                                                       Row(verticalAlignment = Alignment.CenterVertically) {
                                                           Image(
                                                               painter = painterResource(id = Helpers.getIconResIdForBlockchain(value)),
                                                               contentDescription = "",
                                                               modifier = Modifier
                                                                   .size(16.dp)
                                                                   .padding(end = 4.dp)
                                                           )
                                                           Text(
                                                               value,
                                                               fontSize = 12.sp,
                                                               fontFamily = KumbhSansFontFamily,
                                                               fontWeight = FontWeight.Bold,
                                                               color = White
                                                           )
                                                       }
                                                   } else if (label == "Reason") {
                                                       Box(
                                                           modifier = Modifier
                                                               .background(
                                                                   BrightSkyBlue,
                                                                   shape = RoundedCornerShape(4.dp)
                                                               )
                                                               .padding(
                                                                   horizontal = 8.dp,
                                                                   vertical = 4.dp
                                                               )
                                                       ) {
                                                           Text(
                                                               value,
                                                               fontSize = 12.sp,
                                                               fontFamily = KumbhSansFontFamily,
                                                               fontWeight = FontWeight.Bold,
                                                               color = White
                                                           )
                                                       }
                                                   } else {
                                                       Text(
                                                           value.uppercase(),
                                                           fontSize = 12.sp,
                                                           fontFamily = KumbhSansFontFamily,
                                                           fontWeight = FontWeight.Bold,
                                                           color = White
                                                       )
                                                   }
                                               }
                                               Spacer(modifier = Modifier.height(12.dp))
                                           }

                                           // Bank Payment Details Card
                                           Column(
                                               modifier = Modifier
                                                   .fillMaxWidth()
                                                   .background(
                                                       Midnight,
                                                       shape = RoundedCornerShape(8.dp)
                                                   )
                                                   .padding(16.dp)
                                           ) {
                                               Text(
                                                   "bank payment details".uppercase(),
                                                   color = White
                                               )
                                               Spacer(modifier = Modifier.height(12.dp))
                                               val bankDetails = listOf(
                                                   "Amount to Receive (₦)" to "${transaction.netFiatAmount.truncateToTwoDecimals()}",
                                                   "Bank Name" to transaction.bankInfo.bankName,
                                                   "Account Number" to transaction.bankInfo.accountNumber,
                                                   "Account Name" to transaction.bankInfo.accountHolder
                                               )
                                               bankDetails.forEach { (label, value) ->
                                                   Row(
                                                       modifier = Modifier.fillMaxWidth(),
                                                       horizontalArrangement = Arrangement.SpaceBetween,
                                                       verticalAlignment = Alignment.CenterVertically
                                                   ) {
                                                       Text(
                                                           label,
                                                           fontSize = 12.sp,
                                                           fontFamily = KumbhSansFontFamily,
                                                           fontWeight = FontWeight.Medium,
                                                           color = White
                                                       )
                                                       Text(
                                                           value,
                                                           fontSize = 12.sp,
                                                           fontFamily = KumbhSansFontFamily,
                                                           fontWeight = FontWeight.Bold,
                                                           color = White
                                                       )
                                                   }
                                                   Spacer(modifier = Modifier.height(10.dp))
                                               }
                                           }

                                           Spacer(modifier = Modifier.height(12.dp))

                                           Column(
                                               modifier = Modifier
                                                   .fillMaxWidth()
                                                   .background(
                                                       Midnight,
                                                       shape = RoundedCornerShape(8.dp)
                                                   )
                                                   .padding(16.dp)
                                           ) {
                                               Text(
                                                   "recipient’s details".uppercase(),
                                                   color = White
                                               )
                                               Spacer(modifier = Modifier.height(12.dp))
                                               val recipientDetails = listOf(
                                                   "Wallet Address" to Helpers.abbreviateAddress(
                                                       transaction.recipientInfo.destinationAddress,
                                                       startLength = 6,
                                                       endLength = 6
                                                   ),
                                               )
                                               recipientDetails.forEach { (label, value) ->
                                                   Row(
                                                       modifier = Modifier.fillMaxWidth(),
                                                       horizontalArrangement = Arrangement.SpaceBetween,
                                                       verticalAlignment = Alignment.CenterVertically
                                                   ) {
                                                       Text(
                                                           label,
                                                           fontSize = 12.sp,
                                                           fontFamily = KumbhSansFontFamily,
                                                           fontWeight = FontWeight.Medium,
                                                           color = White
                                                       )
                                                       Text(
                                                           value,
                                                           fontSize = 12.sp,
                                                           fontFamily = KumbhSansFontFamily,
                                                           fontWeight = FontWeight.Bold,
                                                           color = SteelBlueGrey
                                                       )
                                                   }
                                                   Spacer(modifier = Modifier.height(10.dp))
                                               }
                                           }
                                       }
                                   }
                                   else -> { }
                                }

                            }
                        }
                    } ?: run {
                        // Loading or error state
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            // CircularProgressIndicator(color = Orange)
                        }
                    }
                }
            }
        }
    )
}