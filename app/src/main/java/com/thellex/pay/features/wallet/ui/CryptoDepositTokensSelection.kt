package com.thellex.pay.features.wallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.data.datastore.getBaseSettingsCache
import com.thellex.pay.shared.CenteredTopBar
import com.thellex.pay.shared.IconDisplayer

data class CryptoItem(
    val id: String,
    val name: String,
    val iconUrl: String
)

@Composable
fun CryptoItemCard(
    crypto: CryptoItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) GoldenYellow else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkBlue)
            .border(
                width = if (selected) 0.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            IconDisplayer(
                ticker = crypto.id,
                iconUrl = crypto.iconUrl,
                fallbackRes = R.drawable.icon_avatar,
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp)
            )

            Spacer(Modifier.width(14.dp))

            Text(
                text = crypto.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.weight(1f))

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB300)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SelectCryptoList(
    cryptos: List<CryptoItem>,
    selectedId: String?,
    onSelect: (CryptoItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        cryptos.forEach { crypto ->
            CryptoItemCard(
                crypto = crypto,
                selected = crypto.id == selectedId,
                onClick = { onSelect(crypto) }
            )
            Spacer(Modifier.height(14.dp))
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun CryptoDepositTokensSelectionScreen(
    navController: NavController
) {
    val activity = LocalContext.current as? Activity
    val application = LocalContext.current.applicationContext as Application
    val isPreview = LocalInspectionMode.current

    var cryptos by remember { mutableStateOf<List<CryptoItem>>(emptyList()) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (isPreview) {
            /* ───── Preview dummy data ───── */
            cryptos = listOf(
                CryptoItem("btc", "Bitcoin", ""),
                CryptoItem("sol", "Solana", ""),
                CryptoItem("lsk", "Lisk", ""),
                CryptoItem("eth", "Ethereum", ""),
                CryptoItem("usdt", "USDT", "")
            )
            selectedId = "btc"
        } else {
            /* ───── Cached supportedTokens ───── */
            val cache = application.getBaseSettingsCache() ?: return@LaunchedEffect

            cryptos = cache.depositTokens.map { token ->
                CryptoItem(
                    id = token.ticker,
                    name = token.name,
                    iconUrl = token.iconUrl
                )
            }

            selectedId = cryptos.firstOrNull()?.id
        }
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
                    onBackClick = { activity?.finish() }
                )

                Spacer(modifier = Modifier.height(31.dp))

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "SELECT CURRENCY",
                        color = White,
                        fontFamily = KumbhSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                SelectCryptoList(
                    cryptos = cryptos,
                    selectedId = selectedId,
                    onSelect = { crypto ->
                        selectedId = crypto.id
                        navController.navigate(
                            "${ComposeRoutes.CryptoDeposit.route}?ticker=${crypto.id}"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CryptoDepositTokensSelectionRoute(
    navController: NavController
) {
    CryptoDepositTokensSelectionScreen(navController)
}

@Preview(showBackground = true)
@Composable
fun CryptoDepositTokensSelectionPreview() {
    CryptoDepositTokensSelectionScreen(
        navController = rememberNavController()
    )
}
