package com.thellex.pay.shared

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.BrightSkyBlue
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.PinkRed
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.core.utils.Helpers.formatTransactionTimeHumanReadable
import com.thellex.pay.core.utils.Helpers.getStatusIconResId
import com.thellex.pay.data.model.BaseSettingsViewModel
import com.thellex.pay.data.model.BaseSettingsViewModelFactory
import com.thellex.pay.data.model.ITransactionHistoryDto
import com.thellex.pay.data.model.PaymentStatusEnum
import com.thellex.pay.data.model.TransactionTypeEnum
import com.thellex.pay.data.model.findChainAndAssetIcons
import com.thellex.pay.data.model.findChainIconForNetwork
import com.thellex.pay.data.model.toCryptoTransactionSummary
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import kotlinx.serialization.json.Json

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionItem(
    navController: NavController,
    transaction: ITransactionHistoryDto
) {
    val context = LocalContext.current

    val baseSettingsVM: BaseSettingsViewModel = viewModel(
        factory = BaseSettingsViewModelFactory(context)
    )

    val baseSettings by baseSettingsVM
        .baseSettings
        .collectAsState()

    val icons = remember(baseSettings, transaction) {
        baseSettings?.findChainAndAssetIcons(
            network = transaction.paymentNetwork,
            assetSymbol = transaction.assetCode.name
        )
    }

    val assetIcon = icons?.assetIconUrl

    val formattedTime = remember(transaction.createdAt) {
        formatTransactionTimeHumanReadable(transaction.createdAt)
    }

    val route = remember(transaction) {
        val cryptoSummary = transaction.toCryptoTransactionSummary(
           transaction = transaction
        )
        val jsonString = Json.encodeToString(cryptoSummary)
        val encoded = Uri.encode(jsonString)
        "${ComposeRoutes.CryptoTransactionDetail.route}/$encoded"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                navController.navigate(route)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(31.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(DarkBlue)
                ) {
                    IconDisplayer(
                        ticker = transaction.assetCode.name.uppercase(),
                        iconUrl = assetIcon,
                        modifier = Modifier
                            .size(31.dp)
                            .align(Alignment.Center)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 0.dp, y = (2).dp)
                        .clip(CircleShape)
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = getStatusIconResId(transaction.transactionType.name)),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.assetCode.name.uppercase(),
                        color = White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = KumbhSansFontFamily
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = transaction.paymentNetwork.name.uppercase(),
                        modifier = Modifier
                            .background(
                                color = DarkBlue,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        color = SteelBlueGrey,
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formattedTime,
                    color = SteelBlueGrey,
                    fontSize = 10.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.amount,
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = OutfitFontFamily
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.hourglass),
                        contentDescription = null,
                        tint = PinkRed,
                        modifier = Modifier.size(10.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = transaction.paymentStatus.name.uppercase(),
                        color = PinkRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Transaction Item - Light", showBackground = true)
@Preview(name = "Transaction Item - Dark", backgroundColor = 0xFF121212, showBackground = true)
@Composable
fun TransactionItemPreview() {
    Column(
        modifier = Modifier
            .background(Color(0xFF121212))
            .padding(16.dp)
            .width(360.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        val sampleTransaction = ITransactionHistoryDto(
            id = "1",
            transactionId = "tx123456",
            assetCode = TokenEnum.usdt,
            paymentNetwork = SupportedBlockchainEnum.stellar,
            amount = "12.50 USDC",
            createdAt = "2026-01-10T15:45:00Z",
            paymentStatus = PaymentStatusEnum.Processing,
            transactionType = TransactionTypeEnum.CRYPTO_WITHDRAWAL,
            transactionDirection = "OUT",
            fee = "0.01 USDC",
            feeLevel = "LOW",
            blockchainTxId = "btx987654",
            reason = "Test transaction",
            sourceAddress = "GABC123SOURCE",
            destinationAddress = "GXYZ789DEST",
            rampID = "ramp123",
            mainFiatAmount = 12.50,
            mainAssetAmount = 12.50,
            transactionMessage = "Payment for services",
            valueInLocal = 12.50,
            valueInUsd = 12.50,
            event = "TRANSFER"
        )

        TransactionItem(
            navController = rememberNavController(),
            transaction = sampleTransaction
        )
    }
}