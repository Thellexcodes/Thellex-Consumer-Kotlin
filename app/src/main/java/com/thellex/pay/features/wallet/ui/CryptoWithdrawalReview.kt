package com.thellex.pay.features.wallet.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.shared.BackIconButton
import com.thellex.pay.shared.CenteredTopBar
import com.thellex.pay.shared.InfoCard
import com.thellex.pay.shared.InfoCardType
import com.thellex.pay.shared.PrimaryButton

data class SummaryItem(
    val label: String,
    val value: String
)

val transactionSummaryItems = listOf(
    SummaryItem("Recipient", "0xA1B2...9F3E"),
    SummaryItem("Network", "Ethereum"),
    SummaryItem("Amount", "120 USDT"),
    SummaryItem("Network Fee", "0.003 ETH")
)

@Composable
fun TransactionSummaryList(
    items: List<SummaryItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkBlue)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(31.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.label,
                    color = White,
                    fontSize = 12.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )

                Text(
                    text = item.value,
                    color = White,
                    fontSize = 12.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CryptoWithdrawalReview(navController: NavController) {
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

                // ───── TOP CONTENT ─────
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = GoldenYellow,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "AMOUNT", color = White)
                        }

                        Spacer(modifier = Modifier.height(13.dp))

                        Text(
                            text = "1 USD",
                            color = White,
                            fontSize = 32.sp,
                            fontFamily = KumbhSansFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(9.dp))

                        Text(
                            text = "$0.33",
                            color = SteelBlueGrey,
                            fontSize = 16.sp,
                            fontFamily = KumbhSansFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TransactionSummaryList(
                        items = transactionSummaryItems,
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
                    onClick = {},
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}


@Composable
fun CryptoWithdrawalReviewRoute(navController: NavController) {
    CryptoWithdrawalReview(navController)
}

@Preview(showBackground = true)
@Composable
fun CryptoWithdrawalReviewPreview(){
    CryptoWithdrawalReview(
        navController = rememberNavController()
    )
}