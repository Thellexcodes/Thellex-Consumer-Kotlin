package com.thellex.pay.features.wallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.util.Log
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalInspectionMode
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
import com.thellex.pay.data.datastore.getSupportedChainsCache
import com.thellex.pay.data.model.ChainInfo
import com.thellex.pay.data.model.TokenInfo
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import com.thellex.pay.shared.AppFullWidthModal
import com.thellex.pay.shared.DropdownField
import com.thellex.pay.shared.IconDisplayer
import com.thellex.pay.shared.InfoCard
import com.thellex.pay.shared.InfoCardType
import com.thellex.pay.shared.MaxButton
import com.thellex.pay.shared.NetworkSelectionContent
import com.thellex.pay.shared.PrimaryButton
import com.thellex.pay.shared.SendInputField
import com.thellex.pay.shared.TokenSelectionContent

@SuppressLint("ContextCastToActivity")
@Composable
fun CryptoWithdrawalScreen(
    navController: NavHostController,
) {
    val TAG = "CryptoWithdrawal"

    var showNetworkModal by remember { mutableStateOf(false) }
    var showTokenModal by remember { mutableStateOf(false) }

    var walletAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    var supportedChains by remember { mutableStateOf<List<ChainInfo>>(emptyList()) }
    var selectedChain by remember { mutableStateOf<ChainInfo?>(null) }

    val supportedTokens by remember {
        derivedStateOf { selectedChain?.supportedTokens.orEmpty() }
    }

    var selectedToken by remember { mutableStateOf<TokenInfo?>(null) }
    var selectedTokenId by rememberSaveable { mutableStateOf<String?>(null) }

    val application = LocalContext.current.applicationContext as Application

    /**
     * Load cached chains
     */
    LaunchedEffect(Unit) {
        val cache = application.getSupportedChainsCache()
        val chains = cache?.second.orEmpty()

        supportedChains = chains

        if (chains.isNotEmpty()) {
            val chain = chains.first()
            selectedChain = chain

            chain.supportedTokens.firstOrNull()?.let { token ->
                selectedToken = token
                selectedTokenId = token.symbol.name
            }
        }
    }

    /**
     * Reset token when chain changes
     */
    LaunchedEffect(selectedChain) {
        selectedChain?.supportedTokens?.firstOrNull()?.let { token ->
            selectedToken = token
            selectedTokenId = token.symbol.name
        }
    }

    val isPreview = LocalInspectionMode.current

    LaunchedEffect(isPreview) {
        if (isPreview) {
            val previewTokens = listOf(
                TokenInfo(
                    symbol = TokenEnum.usdt,
                    name = "Tether USD",
                    decimals = 6,
                    iconDisplay = ""
                ),
                TokenInfo(
                    symbol = TokenEnum.usdc,
                    name = "USD Coin",
                    decimals = 6,
                    iconDisplay = ""
                )
            )

            val previewChains = listOf(
                ChainInfo(
                    id = SupportedBlockchainEnum.bep20,
                    displayName = "BNB Smart Chain (BEP 20)",
                    fee = 2.0,
                    minimumWithdrawal = 10,
                    arrivalTime = "≈ 1 min",
                    supportedTokens = previewTokens
                )
            )

            supportedChains = previewChains
            selectedChain = previewChains.first()
            selectedToken = previewTokens.first()
            selectedTokenId = previewTokens.first().symbol.name
        } else {
            val cache = application.getSupportedChainsCache()
            val chains = cache?.second.orEmpty()

            supportedChains = chains

            if (chains.isNotEmpty()) {
                val chain = chains.first()
                selectedChain = chain

                chain.supportedTokens.firstOrNull()?.let { token ->
                    selectedToken = token
                    selectedTokenId = token.symbol.name
                }
            }
        }
    }


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
                        modifier = Modifier
                            .height(32.dp)
                            .width(32.dp)
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
                        trailingIcon = {}
                    )

                    Spacer(modifier = Modifier.height(19.dp))

                    DropdownField(
                        placeholder = "Select Network",
                        selected = selectedChain?.displayName,
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

                            Row(verticalAlignment = Alignment.CenterVertically) {

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
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.clickable { showTokenModal = true },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        selectedToken?.let { token ->
                                            IconDisplayer(
                                                ticker = token.symbol.name,
                                                iconUrl = token.iconDisplay,
                                            )

                                            Text(
                                                text = token.symbol.name.uppercase(),
                                                color = White,
                                                fontSize = 10.sp,
                                                fontFamily = KumbhSansFontFamily,
                                                fontWeight = FontWeight.Light
                                            )
                                        }

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
                                Row {
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
                                        fontFamily = KumbhSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }

                                MaxButton(
                                    modifier = Modifier
                                        .width(41.dp)
                                        .height(22.dp),
                                    onClick = {}
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "CONFIRM",
                        onClick = {},
                        enabled = selectedChain != null && selectedToken != null && amount.isNotBlank()
                    )
                }
            }

            /**
             * Network modal
             */
            AppFullWidthModal(
                show = showNetworkModal,
                onDismiss = { showNetworkModal = false },
                title = "Select Network"
            ) {
                Column {
                    InfoCard(
                        text = "Ensure that the network matches the address and the deposit platform or assets may be lost.",
                        type = InfoCardType.WARNING
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    NetworkSelectionContent(
                        chains = supportedChains,
                        onChainSelected = { chain ->
                            selectedChain = chain
                            showNetworkModal = false
                        }
                    )
                }
            }

            /**
             * Token modal
             */
            AppFullWidthModal(
                show = showTokenModal,
                onDismiss = { showTokenModal = false },
                title = "Select Asset"
            ) {
                TokenSelectionContent(
                    tokens = supportedTokens,
                    selectedTokenId = selectedTokenId,
                    onTokenSelected = { token ->
                        selectedToken = token
                        selectedTokenId = token.symbol.name
                        showTokenModal = false
                    }
                )
            }
        }
    }
}

/**
 * Route wrapper
 */
@Composable
fun CryptoWithdrawalScreenRoute(navController: NavHostController) {
    CryptoWithdrawalScreen(navController = navController)
}

/**
 * Preview
 */
@Preview(showBackground = true)
@Composable
fun CryptoWithdrawalScreenPreview() {
    CryptoWithdrawalScreen(navController = rememberNavController())
}
