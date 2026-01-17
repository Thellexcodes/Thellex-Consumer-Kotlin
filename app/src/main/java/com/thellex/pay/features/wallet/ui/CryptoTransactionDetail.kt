package com.thellex.pay.features.wallet.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.PinkRed
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import com.thellex.pay.shared.CenteredTopBar
import com.thellex.pay.shared.PrimaryButton

@Composable
fun CryptoTransactionDetail(
    navController: NavHostController,
    transaction: CryptoTransactionSummary? = null
) {
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
                    title = "summary",
                    onBackClick = { navController.popBackStack() }
                )

                // ===== CONTENT AREA (Takes Remaining Height) =====
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {

                    Spacer(modifier = Modifier.height(54.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DEPOSIT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = KumbhSansFontFamily,
                            color = SteelBlueGrey
                        )
                        Text(
                            text = "${transaction?.amount} USDC",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = KumbhSansFontFamily,
                            color = White
                        )
                        Text(
                            text = "${transaction?.valueInLocal} Denomiation(NGN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = KumbhSansFontFamily,
                            color = White
                        )
                    }

                    Spacer(modifier = Modifier.height(54.dp))

                    TransactionSummaryList(
                        showTitle = true,
                        transaction = transaction!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // ===== BOTTOM ACTION =====
                PrimaryButton(
                    text = "Share",
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CryptoTransactionDetailRoute(
    navController: NavHostController,
    transaction: CryptoTransactionSummary? = null
) {
    Log.d("DetailRoute", "Transaction object passed to Route: $transaction")
    if (transaction == null) {
        Text("Transaction details not available", color = PinkRed)
    } else {
         CryptoTransactionDetail(navController, transaction)
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionDetailPreview(){
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

    CryptoTransactionDetail(
        navController = rememberNavController(),
        transaction = dummyTransaction
    )
}