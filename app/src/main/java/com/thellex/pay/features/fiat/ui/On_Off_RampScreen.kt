package com.thellex.pay.features.fiat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.BrightSkyBlue
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.DeepNavy
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.Gray
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.data.enums.OnOffRampAction
import com.thellex.pay.features.wallet.model.AssetTotalDto
import com.thellex.pay.features.wallet.model.WalletBalanceDto
import com.thellex.pay.features.wallet.ui.WalletScreen
import com.thellex.pay.shared.Accordion
import com.thellex.pay.shared.CenteredTopBar
import com.thellex.pay.shared.DropdownField
import com.thellex.pay.shared.IconDisplayer
import com.thellex.pay.shared.InfoCard
import com.thellex.pay.shared.InfoCardType
import com.thellex.pay.shared.MaxButton
import com.thellex.pay.shared.PrimaryButton
import com.thellex.pay.shared.SendInputField

@Composable
fun PendingTransactionsRow(
    count: Int,
    onViewClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrightSkyBlue)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$count pending transactions".uppercase(),
                color = DarkBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = KumbhSansFontFamily
            )

            Row(
                modifier = Modifier.clickable { onViewClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "VIEW",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
                IconDisplayer(
                    ticker = "",
                    iconUrl = "",
                    fallbackRes = R.drawable.icon_arror_right,
                    modifier = Modifier.width(22.dp).scale(0.7f)
                )
            }
        }
    }
}


@Composable
fun On_Off_RampScreen(
    navController: NavController,
    action: OnOffRampAction
) {
    AppGradientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
            ) {
                CenteredTopBar(
                    title = "BUY CRYPTO",
                    onBackClick = { navController?.popBackStack() }
                )

                PendingTransactionsRow(
                    count = 3,
                    onViewClick = {}
                )
                Spacer(modifier = Modifier.height(17.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NETWORK",
                        color = SteelBlueGrey,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = KumbhSansFontFamily
                    )

                    Spacer(modifier = Modifier.weight(0.3f))

                    DropdownField(
                        placeholder = "Select Network",
                        selected = "Ethereum",
                        showLeadingIcon = true,
                        leadingIcon = {
                            IconDisplayer(
                                ticker = "",
                                iconUrl = "",
                                fallbackRes = R.drawable.icon_avatar,
                                modifier = Modifier.width(18.dp).height(18.dp)
                            )
                        },
                        modifier = Modifier.weight(0.4f),
                        onClick = {},
                        enabled = true
                    )
                }

                Spacer(modifier = Modifier.height(17.dp))

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepNavy)
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Column {
                            Text(
                                text = "AMOUNT",
                                color = White,
                                fontSize = 10.sp,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Light
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SendInputField(
                                    modifier = Modifier.weight(1f),
                                    value = "",
                                    onValueChange = { },
                                    placeholder = "Enter Amount",
                                    trailingIcon = {}
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBlue)
                                        .padding(horizontal = 5.dp),
                                    contentAlignment = Alignment.Center
                                ){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        IconDisplayer(
                                            ticker = "",
                                            iconUrl = "https://assets.coingecko.com/coins/images/6319/standard/usdc.png",
                                            fallbackRes = R.drawable.icon_usd
                                        )
                                        Text(
                                            text = "USD",
                                            color = White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Light,
                                            fontFamily = KumbhSansFontFamily
                                        )

                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select token",
                                            tint = Color.White,
                                            modifier = Modifier.height(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row{
                                    Text(
                                        text = "Available Balance : ",
                                        color = White,
                                        fontFamily = KumbhSansFontFamily,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "111111",
                                        color = White,
                                        fontSize = 10.sp,
                                        fontFamily = KumbhSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                MaxButton(
                                    modifier = Modifier.width(41.dp).height(22.dp),
                                    onClick = { }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepNavy)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "AMOUNT",
                                color = White,
                                fontSize = 10.sp,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Light
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SendInputField(
                                    modifier = Modifier.weight(1f),
                                    value = "",
                                    onValueChange = { },
                                    placeholder = "Enter Amount",
                                    trailingIcon = {}
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBlue)
                                        .padding(horizontal = 5.dp),
                                    contentAlignment = Alignment.Center
                                ){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        IconDisplayer(
                                            ticker = "",
                                            iconUrl = "https://assets.coingecko.com/coins/images/6319/standard/usdc.png",
                                            fallbackRes = R.drawable.icon_usd
                                        )
                                        Text(
                                            text = "USD",
                                            color = White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Light,
                                            fontFamily = KumbhSansFontFamily
                                        )

                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select token",
                                            tint = Color.White,
                                            modifier = Modifier.height(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column (
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ){
                        Text(
                            text = "REASON",
                            color = SteelBlueGrey,
                            fontFamily = KumbhSansFontFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )

                        DropdownField(
                            placeholder = "Select Reason",
                            selected = "",
                            showLeadingIcon = false,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {},
                            enabled = true,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Accordion(
                        title = "Summary",
                        initiallyExpanded = true
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Service fee",
                                    fontFamily = KumbhSansFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = SteelBlueGrey
                                )
                                Text(
                                    text = "$15.00",
                                    fontFamily = KumbhSansFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = SteelBlueGrey
                                )
                            }

                            Spacer(modifier = Modifier.height(15.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement =  Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = GoldenYellow,
                                            modifier = Modifier.height(12.dp).width(12.dp)
                                        )

                                        Text(
                                            text = "Rate",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }

                                    Row {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = DarkBlue,
                                                    shape = RoundedCornerShape(7.dp)
                                                )
                                                .padding(vertical = 8.dp, horizontal = 10.dp)
                                        ) {
                                            Text(
                                                text = "00:45",
                                                fontSize = 10.sp,
                                                color = White,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = KumbhSansFontFamily,
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "= 3,477.94 NGN/USDT",
                                    color = White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,

                                )
                            }
                            Spacer(modifier = Modifier.height(15.dp))
                            SpentProgressBar(spent = 2000f, total = 40000f)
                            Spacer(modifier = Modifier.height(15.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        modifier = Modifier,
                        text = "CONFIRM",
                        onClick = {},
                        enabled = true
                    )
                }

            }
        }
    }
}


@Composable
fun On_Off_RampScreenRoute(
    navController: NavController,
    action: OnOffRampAction
) {
    On_Off_RampScreen(navController, action = action )
}

@Composable
fun SpentProgressBar(
    spent: Float,
    total: Float
) {
    val progress = spent / total

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBlue)
                .padding(16.dp)
        ) {
            // Row with spent / total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Spent")
                Text(text = "$${spent.toInt()}/$${total.toInt()}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun On_Off_RampScreenPreview() {
    On_Off_RampScreen(
        navController = rememberNavController(),
        action = OnOffRampAction.FIAT_TO_CRYPTO_ON_RAMP
    )
}
