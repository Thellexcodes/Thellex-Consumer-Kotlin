package com.thellex.pay.features.wallet.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.features.wallet.model.AssetTotalDto
import com.thellex.pay.features.wallet.model.WalletState
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
import com.thellex.pay.shared.CenteredTopBar
import com.thellex.pay.shared.IconDisplayer

data class CryptoOption(
    val name: String,
    val ticker: String,
    val amount: String,
    val usdValue: String,
    val localValue: String,
    val iconUrl: String
)

fun WalletState.toCryptoOptions(): List<CryptoOption> {
    return assetTotals.map { (symbol, asset) ->
        CryptoOption(
            name = symbol.uppercase(),
            ticker = symbol.uppercase(),
            amount = "${asset.total} ${symbol.uppercase()}",
            usdValue = "${asset.valueInUsd} USD".uppercase(),
            localValue = "${asset.valueInLocal}",
            iconUrl = asset.logo
        )
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun WalletScreen(
    navController: NavHostController,
    walletState: WalletState? = null
) {
    // Convert walletState to CryptoOption reactively
    val cryptos by remember(walletState) {
        mutableStateOf(walletState?.toCryptoOptions() ?: emptyList())
    }

    // Compute total USD balance safely
    val totalUsdBalance by remember(cryptos) {
        mutableDoubleStateOf(
            cryptos.sumOf { crypto ->
                crypto.usdValue.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
            }
        )
    }

    AppGradientBackground {
        Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
            ) {
                CenteredTopBar(
                    title = "",
                    onBackClick = { navController.popBackStack() }
                )

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(28.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TOTAL BALANCE",
                                color = White,
                                fontSize = 10.sp,
                                fontFamily = KumbhSansFontFamily,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconDisplayer(
                                ticker = "",
                                iconUrl = "",
                                fallbackRes = R.drawable.icon_eye_open,
                                modifier = Modifier.width(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Display formatted total balance
                            Text(
                                text = "$totalUsdBalance USD".uppercase(),
                                color = SteelBlueGrey,
                                fontSize = 24.sp,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Light,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Your Assets",
                                    color = Color.White,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Light,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        items(cryptos.size) { index ->
                            val crypto = cryptos[index]
                            CryptoCard(
                                crypto = crypto,
                                onClick = {
                                    navController.navigate(
                                        "${ComposeRoutes.AssetDetail.route}/${crypto.ticker.lowercase()}"
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoCard(
    crypto: CryptoOption,
    onClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBlue)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                IconDisplayer(
                    ticker = crypto.ticker,
                    iconUrl = crypto.iconUrl,
                    fallbackRes = R.drawable.icon_avatar
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = crypto.amount,
                    color = White,
                    fontSize = 10.sp,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = crypto.usdValue,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = crypto.ticker,
                color = White,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}


// Route Composable (for navigation graph)
@Composable
fun WalletScreenRoute(
    navController: NavHostController
) {
    val application = LocalContext.current.applicationContext as Application
    val walletFactory = WalletManagerModelFactory(application)
    val walletViewModel: WalletManagerViewModel = viewModel(factory = walletFactory)
    val walletState = walletViewModel.walletBalance.value!!

    WalletScreen(
        navController = navController,
        walletState = walletState
    )
}

// Preview
@Preview(showBackground = true)
@Composable
fun WalletScreenPreview() {
    WalletScreen(
        navController = rememberNavController(),
        walletState = WalletState(
            totalInUsd = 4.79,
            assetTotals = mapOf(
                "usdc" to AssetTotalDto(1.35, 0.0, 1.35, "https://example.com/usdc.png"),
                "xlm" to AssetTotalDto(1.0, 0.0, 1.0, "https://example.com/xlm.png")
            ),
            wallets = emptyMap()
        )
    )
}
