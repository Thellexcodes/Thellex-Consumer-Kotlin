package com.thellex.pay.features.wallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.net.Uri
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.res.painterResource
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
import com.thellex.pay.core.decorators.DeepNavy
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.PinkRed
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.core.utils.Helpers.isValidAmount
import com.thellex.pay.core.utils.isValidWalletAddress
import com.thellex.pay.data.datastore.getBaseSettingsCache
import com.thellex.pay.data.model.ChainInfoDto
import com.thellex.pay.data.model.TokenInfo
import com.thellex.pay.features.wallet.model.GroupedWalletAssetDto
import com.thellex.pay.features.wallet.model.WalletBalanceDto
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
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
import kotlinx.serialization.json.Json

@SuppressLint("ContextCastToActivity")
@Composable
fun CryptoWithdrawalScreen(
    navController: NavHostController,
    walletState: WalletBalanceDto? = null
) {

    var showNetworkModal by remember { mutableStateOf(false) }
    var showTokenModal by remember { mutableStateOf(false) }

    var fundUid by remember { mutableStateOf("") }
    var isWalletValid by remember { mutableStateOf(true) }

    var amount by remember { mutableStateOf("") }
    var sourceAddress by remember { mutableStateOf("") }

    var supportedChains by remember { mutableStateOf<List<ChainInfoDto>>(emptyList()) }
    var selectedChain by remember { mutableStateOf<ChainInfoDto?>(null) }

    val supportedTokens by remember {
        derivedStateOf { selectedChain?.supportedTokens.orEmpty() }
    }

    var selectedToken by remember { mutableStateOf<TokenInfo?>(null) }
    var selectedTokenId by rememberSaveable { mutableStateOf<String?>(null) }

    val application = LocalContext.current.applicationContext as Application
    var chainAssets by remember { mutableStateOf<List<GroupedWalletAssetDto>>(emptyList()) }

    var availableBalance by remember { mutableDoubleStateOf(0.0) }

    var isAmountValid by remember { mutableStateOf(true) }

    var hasTyped by remember { mutableStateOf(false) }

    LaunchedEffect(walletState, selectedChain, selectedToken) {

        if (walletState == null || selectedChain == null) {
            chainAssets = emptyList()
            availableBalance = 0.0
            sourceAddress = ""
            return@LaunchedEffect
        }

        val networkKey = selectedChain!!.id.name.lowercase()

        // 1. Get all wallets for the selected network
        val walletsForNetwork = walletState.wallets.values
            .filter { it.network.equals(networkKey, ignoreCase = true) }

        if (walletsForNetwork.isEmpty()) {
            chainAssets = emptyList()
            availableBalance = 0.0
            sourceAddress = ""
            return@LaunchedEffect
        }

        // 2. If token is selected, find wallet that supports that token
        val matchingWallet = selectedToken?.let { token ->
            walletsForNetwork.firstOrNull { wallet ->
                wallet.assets.any { asset ->
                    asset.assetCode.equals(token.symbol.name, ignoreCase = true)
                }
            }
        }

        // 3. Fallback: first wallet for the network (ony if no token yet)
        val selectedWallet = matchingWallet ?: walletsForNetwork.first()

        // 4. Set source address
        sourceAddress = selectedWallet.address

        // 5. Load assets for that specific wallet
        chainAssets = selectedWallet.assets.orEmpty()

        // 6. Resolve available balance for selected token
        availableBalance = selectedToken?.let { token ->
            selectedWallet.assets
                .firstOrNull { it.assetCode.equals(token.symbol.name, ignoreCase = true) }
                ?.balance
                ?: 0.0
        } ?: 0.0
    }

    LaunchedEffect(Unit) {
        val cache = application.getBaseSettingsCache() ?: return@LaunchedEffect

        val chains = cache.chains
        supportedChains = chains

        if (chains.isNotEmpty()) {
            val chain = chains.first()
            selectedChain = chain

            val token = chain.supportedTokens.firstOrNull()
            if (token != null) {
                selectedToken = token
                selectedTokenId = token.symbol.name
            }
        }
    }

    LaunchedEffect(selectedChain) {
        selectedChain?.supportedTokens?.firstOrNull()?.let { token ->
            selectedToken = token
            selectedTokenId = token.symbol.name
        }
    }

    LaunchedEffect(amount, selectedChain) {
        if (selectedChain != null) {
            val minWithdrawal = selectedChain!!.minimumWithdrawal.toDouble()
            isAmountValid = amount.toDoubleOrNull()?.let { it >= minWithdrawal } ?: false
        } else {
            isAmountValid = true
        }
    }

    LaunchedEffect(amount, selectedChain, hasTyped) {
        if (!hasTyped) {
            isAmountValid = true
            return@LaunchedEffect
        }

        isAmountValid = selectedChain?.let { chain ->
            amount.toDoubleOrNull()?.let { it >= chain.minimumWithdrawal } ?: false
        } ?: true
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
                ChainInfoDto(
                    id = SupportedBlockchainEnum.bep20,
                    displayName = "BNB Smart Chain (BEP 20)",
                    fee = 2.0,
                    minimumWithdrawal = 10,
                    arrivalTime = "≈ 1 min",
                    supportedTokens = previewTokens,
                    iconUrl = ""
                )
            )

            supportedChains = previewChains
            selectedChain = previewChains.first()
            selectedToken = previewTokens.first()
            selectedTokenId = previewTokens.first().symbol.name
        } else {
            val cache = application.getBaseSettingsCache()
            val chains = cache?.chains.orEmpty()

            supportedChains = chains

            if (chains.isNotEmpty()) {
                val chain = chains.first()
                selectedChain = chain

                val token = chain.supportedTokens.firstOrNull()
                if (token != null) {
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
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = if (isWalletValid) SteelBlueGrey else PinkRed,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        value = fundUid,
                        onValueChange = {
                            fundUid = it
                            isWalletValid = isValidWalletAddress(fundUid, selectedChain?.id)
                        },
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = 1.dp,
                                            color = if (isAmountValid) SteelBlueGrey else PinkRed,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    value = amount,
                                    onValueChange = { input ->
                                        if (input.isValidAmount()) {
                                            amount = input
                                            hasTyped = true
                                            isAmountValid = input.toDoubleOrNull()?.let { it <= availableBalance } ?: false
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
                                        text = "$availableBalance".uppercase(),
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
                                    onClick = {
                                        amount = "$availableBalance"
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "WITHDRAW",
                        onClick = {
                            isWalletValid = isValidWalletAddress(fundUid, selectedChain?.id)

                            if (!isWalletValid) {
                                Log.d("CryptoWithdrawal", "Invalid wallet address: $fundUid")
                                return@PrimaryButton
                            }

                            hasTyped = true
                            isAmountValid = amount.toDoubleOrNull()?.let { it <= availableBalance } ?: false
                            if (!isAmountValid) return@PrimaryButton

                            try {
                                val transactionSummary = CryptoTransactionSummary(
                                    amount = amount,
                                    valueInUsd = availableBalance,
                                    valueInLocal = 0.0,
                                    sourceAddress = sourceAddress,
                                    fundUid = fundUid,
                                    network = selectedChain!!.id,
                                    networkFee = selectedChain!!.fee,
                                    assetCode = selectedToken!!.symbol,
                                    networkName = selectedChain!!.displayName,
                                    reason = "BILL"
                                )

                                val transactionJson = Json.encodeToString(transactionSummary)
                                val encodedTransaction = Uri.encode(transactionJson)

                                val route = "${ComposeRoutes.CryptoWithdrawalReview.route}?transaction=$encodedTransaction"

                                if (route.length > 4000) {
                                    throw IllegalArgumentException("Transaction route too long: ${route.length} chars")
                                }

                                navController.navigate(route)

                            } catch (e: Exception) {
                                Log.e(
                                    "CryptoWithdrawal",
                                    "WITHDRAWAL_NAVIGATION_FAILED",
                                    e
                                )
                                Log.d(
                                    "CryptoWithdrawal",
                                    "Amount: $amount, Token: ${selectedToken?.symbol}, Chain: ${selectedChain?.displayName}"
                                )
                            }
                        },
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
    val application = LocalContext.current.applicationContext as Application
    val walletFactory = WalletManagerModelFactory(application)
    val walletViewModel: WalletManagerViewModel = viewModel(factory = walletFactory)
    val walletState = walletViewModel.walletBalance.value!!

    CryptoWithdrawalScreen(navController = navController, walletState)
}

/**
 * Preview
 */
@Preview(showBackground = true)
@Composable
fun CryptoWithdrawalScreenPreview() {
    CryptoWithdrawalScreen(navController = rememberNavController())
}
