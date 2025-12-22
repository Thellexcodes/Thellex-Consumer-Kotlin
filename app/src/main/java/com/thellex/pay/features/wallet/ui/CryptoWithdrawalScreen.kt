package com.thellex.pay.features.wallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.BrightSkyBlue
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.DeepNavy
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.utils.Helpers.isValidAmount
import com.thellex.pay.data.model.ChainInfo
import com.thellex.pay.data.model.TokenInfo
import com.thellex.pay.shared.AppFullWidthModal
import com.thellex.pay.shared.DropdownField
import com.thellex.pay.shared.IconDisplayer
import com.thellex.pay.shared.InfoCard
import com.thellex.pay.shared.InfoCardType
import com.thellex.pay.shared.MaxButton
import com.thellex.pay.shared.NetworkSelectionContent
import com.thellex.pay.shared.PrimaryButton
import com.thellex.pay.shared.SendInputField

val supportedTokens = listOf(
    TokenInfo(
        id = "USDT",
        symbol = "USDT",
        iconRes = R.drawable.icon_usdt
    ),
    TokenInfo(
        id = "USDC",
        symbol = "USDC",
        iconRes = R.drawable.icon_usdc
    ),
    TokenInfo(
        id = "USD",
        symbol = "USD",
        iconRes = R.drawable.icon_usd
    )
)

val supportedChains = listOf(
    ChainInfo(
        id = "BEP20",
        displayName = "BNB Smart Chain (BEP 20)",
        fee = "0.00 USDT",
        minimumWithdrawal = "10 USDT",
        arrivalTime = "≈ 1 min"
    ),
    ChainInfo(
        id = "TRC20",
        displayName = "Tron (TRC 20)",
        fee = "1.00 USDT",
        minimumWithdrawal = "10 USDT",
        arrivalTime = "≈ 2 mins"
    ),
    ChainInfo(
        id = "ERC20",
        displayName = "Ethereum (ERC 20)",
        fee = "5.00 USDT",
        minimumWithdrawal = "20 USDT",
        arrivalTime = "≈ 5 mins"
    ),
    ChainInfo(
        id = "POLYGON",
        displayName = "Polygon PoS",
        fee = "0.10 USDT",
        minimumWithdrawal = "5 USDT",
        arrivalTime = "≈ 1 min"
    )
)


@Composable
fun ChainItem(
    chain: ChainInfo,
    onClick: (ChainInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DarkBlue,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick(chain) }
            .padding(16.dp)
    ) {
        Text(
            text = chain.displayName,
            color = Color.White,
            fontFamily = KumbhSansFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Fee ${chain.fee}",
            color = SteelBlueGrey,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
        )

        Text(
            text = "Minimum Withdrawal ${chain.minimumWithdrawal}",
            color = SteelBlueGrey,
            fontSize = 10.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Normal,
        )

        Text(
            text = "Arrival Time ${chain.arrivalTime}",
            color = SteelBlueGrey,
            fontSize = 10.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
fun TokenItem(
    token: TokenInfo,
    selected: Boolean,
    onClick: (TokenInfo) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DarkBlue,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick(token) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Asset Icon
        Image(
            painter = painterResource(token.iconRes),
            contentDescription = token.symbol,
            modifier = Modifier.height(32.dp).width(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Token Symbol
        Text(
            text = token.symbol,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        // Selected Icon
        if (selected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun TokenSelectionContent(
    tokens: List<TokenInfo>,
    selectedTokenId: String?,
    onTokenSelected: (TokenInfo) -> Unit
) {
    Column {
        tokens.forEach { token ->
            TokenItem(
                token = token,
                selected = token.id == selectedTokenId,
                onClick = { selected ->
                    onTokenSelected(selected)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


@SuppressLint("ContextCastToActivity")
@Composable
fun CryptoWithdrawalScreen(
    navController: NavHostController,
) {
    var showNetworkModal by remember { mutableStateOf(false) }
    var walletAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val selectedNetwork = remember { mutableStateOf("Select Network") }
    var selectedTokenId by rememberSaveable { mutableStateOf<String?>(null) }
    var showTokenModal by remember { mutableStateOf(false) }

    AppGradientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
            ) {
                val activity = LocalContext.current as? Activity

                IconButton(onClick = { activity?.finish() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SEND TO",
                        color = Color.White,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Image(
                        painter = painterResource(id = R.drawable.icon_qr_code),
                        contentDescription = "QR Code",
                        modifier = Modifier.height(32.dp).width(32.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    SendInputField(
                        modifier = Modifier.fillMaxWidth(),
                        value = walletAddress,
                        onValueChange = { walletAddress = it },
                        placeholder = "Enter Wallet Address",
                        trailingIcon = { }
                    )

                    Spacer(modifier = Modifier.height(19.dp))

                    DropdownField(
                        placeholder = "Select Network",
                        selected = selectedNetwork.value.ifBlank { null },
                        onClick = { showNetworkModal = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                                    value = amount,
                                    onValueChange = { input ->
                                        if (input.isValidAmount()) {
                                            amount = input
                                        }
                                    },
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
                                        modifier = Modifier.clickable { showTokenModal = true },
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

                            Spacer(modifier = Modifier.height(15.dp))

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
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "CONFIRM",
                        onClick = {  },
                        enabled = true
                    )
                }
            }

            // Modal is placed here - at the top level after Scaffold
            AppFullWidthModal(
                show = showNetworkModal,
                onDismiss = { showNetworkModal = false },
                title = "Select Network"
            ) {
                Column {
                    InfoCard(text = "Ensure that the network matches the address and the deposit platform or assets may be lost.", type = InfoCardType.WARNING)
                    Spacer(modifier = Modifier.height(20.dp))
                    NetworkSelectionContent(
                        chains = supportedChains,
                        onChainSelected = { chain -> }
                    )
                }
            }

            AppFullWidthModal(
                show = showTokenModal,
                onDismiss = { showTokenModal = false },
                title = "Select Asset"
            ) {
                TokenSelectionContent(
                    tokens = supportedTokens,
                    selectedTokenId = selectedTokenId,
                    onTokenSelected = { token -> selectedTokenId = token.id }
                )
            }

        }
    }
}

@Composable
fun CryptoWithdrawalScreenRoute(navController: NavHostController){
    CryptoWithdrawalScreen(
        navController = rememberNavController()
    )
}

@Preview(showBackground = true)
@Composable
fun CryptoWithdrawalScreenPreview() {
    CryptoWithdrawalScreen(
        navController = rememberNavController()
    )
}