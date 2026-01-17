package com.thellex.pay.features.fiat.ui

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.BrightSkyBlue
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.DeepNavy
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.utils.Helpers.format
import com.thellex.pay.data.datastore.getBaseSettingsCache
import com.thellex.pay.data.enums.OnOffRampAction
import com.thellex.pay.data.model.BaseSettingsViewModel
import com.thellex.pay.data.model.BaseSettingsViewModelFactory
import com.thellex.pay.data.model.ChainInfoDto
import com.thellex.pay.data.model.DepositTokenDto
import com.thellex.pay.data.model.TokenInfo
import com.thellex.pay.features.wallet.model.WalletState
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import com.thellex.pay.shared.Accordion
import com.thellex.pay.shared.AppFullWidthModal
import com.thellex.pay.shared.CenteredTopBar
import com.thellex.pay.shared.CryptoTokenSelectionContent
import com.thellex.pay.shared.DropdownField
import com.thellex.pay.shared.FiatSelectionContent
import com.thellex.pay.shared.IconDisplayer
import com.thellex.pay.shared.InfoCard
import com.thellex.pay.shared.InfoCardType
import com.thellex.pay.shared.MaxButton
import com.thellex.pay.shared.NetworkSelectionContent
import com.thellex.pay.shared.PrimaryButton
import com.thellex.pay.shared.ReasonSelectionModalContent
import com.thellex.pay.shared.SUPPORTED_FIATS
import com.thellex.pay.shared.SendInputField
import com.thellex.pay.shared.TokenSelectionContent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun On_Off_RampScreen(
    navController: NavController,
    action: OnOffRampAction,
    walletState: WalletState? = null
) {
    var isOnRamp by remember { mutableStateOf(action == OnOffRampAction.FIAT_TO_CRYPTO_ON_RAMP) }
    val screenTitle = if (isOnRamp) "Buy Crypto" else "Sell Crypto"

    val context = LocalContext.current
    var selectedChain by remember { mutableStateOf<ChainInfoDto?>(null) }
    var supportedChains by remember { mutableStateOf<List<ChainInfoDto>>(emptyList()) }
    var depositTokens by remember { mutableStateOf<List<DepositTokenDto>>(emptyList()) }

    var fromAmount by remember { mutableStateOf("") }
    var toAmount by remember { mutableStateOf("") }

    var selectedFiat by remember { mutableStateOf<DepositTokenDto?>(null) }
    var selectedCryptoToken by remember { mutableStateOf<TokenInfo?>(null) }

    var showNetworkModal by remember { mutableStateOf(false) }
    var showFiatModal by remember { mutableStateOf(false) }
    var showCryptoModal by remember { mutableStateOf(false) }

    var lastEditedSide by remember { mutableStateOf("") }
    var isCalculating by remember { mutableStateOf(false) }

    // Add this state somewhere in On_Off_RampScreen
    var showReasonModal by remember { mutableStateOf(false) }

    // Current selected reason (you can store it in state)
    var selectedReason by remember { mutableStateOf("BILLS") }

    val mockRateNgnPerUsdt = 3478f   // e.g. 1 USDT = 3478 NGN
    val mockFeePercent = 1.5f        // 1.5% fee example

    // Helper to get current selected tokens' tickers (for rate logic)
    val fromTicker = if (isOnRamp) selectedFiat?.ticker?.uppercase() else selectedCryptoToken?.symbol?.name?.uppercase()
    val toTicker   = if (isOnRamp) selectedCryptoToken?.symbol?.name?.uppercase() else selectedFiat?.ticker?.uppercase()

    val rotation by animateFloatAsState(
        targetValue = if (isOnRamp) 0f else 180f,
        animationSpec = tween(durationMillis = 400),
        label = "swap rotation"
    )

    val application = LocalContext.current.applicationContext as Application

    // Load base settings
    LaunchedEffect(Unit) {
        val cache = application.getBaseSettingsCache() ?: return@LaunchedEffect

        supportedChains = cache.chains
        depositTokens = cache.depositTokens

        if (supportedChains.isNotEmpty()) {
            selectedChain = supportedChains.find { it.id == SupportedBlockchainEnum.bep20} // ← adjust if enum → it.id == SupportedBlockchainEnum.bep20.name
                ?: supportedChains.minByOrNull { it.fee } ?: supportedChains.first()

            selectedCryptoToken = selectedChain?.supportedTokens
                ?.find { it.symbol.name.equals("usdt", ignoreCase = true) }
                ?: selectedChain?.supportedTokens?.firstOrNull()
        }

        if (depositTokens.isNotEmpty()) {
            selectedFiat = depositTokens.find { it.ticker.equals("NGN", ignoreCase = true) }
                ?: depositTokens.find { it.ticker.equals("USD", ignoreCase = true) }
                        ?: depositTokens.firstOrNull()
        }
    }

    LaunchedEffect(selectedChain) {
        selectedChain?.let { chain ->
            selectedCryptoToken = chain.supportedTokens
                .find { it.symbol.name.equals("usdt", ignoreCase = true) }
                ?: chain.supportedTokens.firstOrNull()
        }
    }

    val scope = rememberCoroutineScope()


    // then later

    fun recalculateOpposite() {
        if (fromAmount.isBlank() && toAmount.isBlank()) return
        if (selectedFiat == null || selectedCryptoToken == null) return

        scope.launch {
            isCalculating = true
            delay(400L)

            val rate = mockRateNgnPerUsdt
            val feeMultiplier = 1f - (mockFeePercent / 100f)

            when (lastEditedSide) {
                "from" -> {
                    val input = fromAmount.toFloatOrNull() ?: 0f
                    val afterFee = input * feeMultiplier

                    toAmount = if (isOnRamp) {
                        // fiat → crypto
                        (afterFee / rate).format(6)
                    } else {
                        // crypto → fiat
                        (afterFee * rate).format(2)
                    }
                }

                "to" -> {
                    val desired = toAmount.toFloatOrNull() ?: 0f
                    val beforeFee = if (isOnRamp) {
                        desired * rate
                    } else {
                        desired / rate
                    }

                    fromAmount = (beforeFee / feeMultiplier).format(
                        if (isOnRamp) 2 else 6
                    )
                }
            }

            isCalculating = false
        }
    }

    AppGradientBackground {
        Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepNavy)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                CenteredTopBar(
                    title = screenTitle.uppercase(),
                    onBackClick = { navController.popBackStack() }
                )

                PendingTransactionsRow(count = 3, onViewClick = {})

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedChain != null || !isOnRamp) {
                    SettingsRow(
                        label = "Network",
                        value = selectedChain?.displayName ?: "Select Network",
                        iconUrl = selectedChain?.iconUrl,
                        onClick = { showNetworkModal = true }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                AmountCard(
                    label = if (isOnRamp) "You Pay" else "You Send",
                    amount = fromAmount,
                    onAmountChange = { newValue ->
                        fromAmount = newValue
                        lastEditedSide = "from"
                        // Trigger recalculation
                        recalculateOpposite()
                    },
                    token = if (isOnRamp) selectedFiat else selectedCryptoToken,
                    onTokenClick = { if (isOnRamp) showFiatModal = true else showCryptoModal = true },
                    isEditable = true,
                    isLoading = isCalculating && lastEditedSide == "to"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Swap direction",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .background(DarkBlue.copy(alpha = 0.4f))
                        .clickable {
                            val wasOnRamp = isOnRamp
                            isOnRamp = !isOnRamp

                            // Swap amounts
                            val tempAmount = fromAmount
                            fromAmount = toAmount
                            toAmount = tempAmount

                            // Swap assets (keep current if possible, fallback to default)
                            if (wasOnRamp) {
                                // Was fiat→crypto → now crypto→fiat
                                selectedCryptoToken = selectedCryptoToken
                                    ?: selectedChain?.supportedTokens
                                        ?.find { it.symbol.name.equals("usdt", ignoreCase = true) }
                                            ?: selectedChain?.supportedTokens?.firstOrNull()
                                selectedFiat = selectedFiat ?: depositTokens.find { it.ticker.equals("NGN", ignoreCase = true) }
                            } else {
                                selectedFiat = selectedFiat ?: depositTokens.find { it.ticker.equals("NGN", ignoreCase = true) }
                                selectedCryptoToken = selectedCryptoToken
                                    ?: selectedChain?.supportedTokens
                                        ?.find { it.symbol.name.equals("usdt", ignoreCase = true) }
                                            ?: selectedChain?.supportedTokens?.firstOrNull()
                            }
                        }
                        .padding(8.dp)
                        .graphicsLayer {
                            rotationZ = rotation
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))


                AmountCard(
                    label = if (isOnRamp) "You Receive" else "You Get",
                    amount = toAmount,
                    onAmountChange = { newValue ->
                        toAmount = newValue
                        lastEditedSide = "to"
                        recalculateOpposite()
                    },
                    token = if (isOnRamp) selectedCryptoToken else selectedFiat,
                    onTokenClick = { if (isOnRamp) showCryptoModal = true else showFiatModal = true },
                    isEditable = true,
                    isLoading = isCalculating && lastEditedSide == "from"
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!isOnRamp) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "REASON",
                            color = SteelBlueGrey,
                            fontSize = 10.sp,
                            fontFamily = KumbhSansFontFamily,
                            fontWeight = FontWeight.Light
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DropdownField(
                            placeholder = "Select Reason",
                            selected = selectedReason,
                            onClick = { showReasonModal = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                SummaryContent(
                    isOnRamp = isOnRamp,
                    chain = selectedChain,
                    fromToken = if (isOnRamp) selectedFiat else selectedCryptoToken,
                    toToken = if (isOnRamp) selectedCryptoToken else selectedFiat,
                    amount = if (isOnRamp) fromAmount else toAmount
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = if (isOnRamp) "Buy Now" else "Sell & Withdraw",
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = fromAmount.isNotBlank() && (isOnRamp || selectedChain != null)
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

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

    AppFullWidthModal(
        show = showFiatModal,
        onDismiss = { showFiatModal = false },
        title = if (isOnRamp) "Pay with" else "Receive in"
    ) {
        val supportedFiats = remember(depositTokens) {
            depositTokens.filter {
                it.ticker.uppercase() in SUPPORTED_FIATS
            }.ifEmpty { depositTokens }
        }

        FiatSelectionContent(
            fiats = supportedFiats,
            selectedTicker = selectedFiat?.ticker,
            onSelected = { fiat ->
                selectedFiat = fiat
                showFiatModal = false
            }
        )
    }

    AppFullWidthModal(
        show = showCryptoModal,
        onDismiss = { showCryptoModal = false },
        title = if (isOnRamp) "Receive" else "Sell / Send"
    ) {
        CryptoTokenSelectionContent(
            tokens = selectedChain?.supportedTokens ?: emptyList(),
            selectedSymbol = selectedCryptoToken?.symbol?.name,
            chainName = selectedChain?.displayName ?: "",
            onSelected = { token ->
                selectedCryptoToken = token
                showCryptoModal = false
            }
        )
    }

    AppFullWidthModal(
        show = showReasonModal,
        onDismiss = { showReasonModal = false },
        title = "Reason for Withdrawal"
    ) {
        ReasonSelectionModalContent(
            selectedReason = selectedReason,
            onReasonSelected = { finalReason ->
                selectedReason = finalReason
            },
            onDismiss = { showReasonModal = false }
        )
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
fun AmountCard(
    label: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    token: Any?,
    onTokenClick: () -> Unit,
    isEditable: Boolean = true,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Midnight)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = label,
                color = White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    SendInputField(
                        modifier = Modifier.fillMaxWidth(),
                        value = amount,
                        onValueChange = { newText ->
                            val filtered = newText
                                .replace(",", ".")
                                .filter { it.isDigit() || it == '.' }

                            if (filtered.count { it == '.' } <= 1) {
                                onAmountChange(filtered)
                            }
                        },
                        placeholder = "0.00",
                        enabled = isEditable && !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            autoCorrect = false
                        ),
                        keyboardActions = KeyboardActions(onDone = {  }),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoading) Color.Gray else White
                        )
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                TokenSelectorButton(
                    token = token,
                    onClick = onTokenClick
                )
            }
        }
    }
}

@Composable
fun TokenSelectorButton(
    token: Any?,
    onClick: () -> Unit
) {
    val name = when (token) {
        is TokenInfo -> token.symbol
        is DepositTokenDto -> token.ticker
        else -> "Select"
    }
    val iconUrl = when (token) {
        is TokenInfo -> token.iconDisplay
        is DepositTokenDto -> token.iconUrl
        else -> ""
    }

    Log.d("Names", "$name is that")

    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBlue)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconDisplayer(
                ticker = "",
                iconUrl = iconUrl,
                fallbackRes = R.drawable.icon_usd,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = name.toString().uppercase(),
                color = White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Light,
                fontFamily = KumbhSansFontFamily
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SettingsRow(
    label: String,
    value: String,
    iconUrl: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = SteelBlueGrey,
            fontSize = 11.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!iconUrl.isNullOrBlank()) {
                IconDisplayer(
                    ticker = "",
                    iconUrl = iconUrl,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = value,
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.KeyboardArrowDown, null, tint = White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun SummaryContent(
    isOnRamp: Boolean,
    chain: ChainInfoDto?,
    fromToken: Any?,
    toToken: Any?,
    amount: String
) {
    val serviceFeeNgn = 2057.21f
    val serviceFeeUsd = 15.23f
    val spentAmount = 2000f
    val totalLimit = 500000f

    val spentProgress by remember(spentAmount, totalLimit) {
        derivedStateOf { (spentAmount / totalLimit).coerceIn(0f, 1f) }
    }

    Accordion(
        title = "SUMMARY",
        initiallyExpanded = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepNavy)
                .padding(16.dp)
        ) {
            // Service fee row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Service fee",
                    color = SteelBlueGrey,
                    fontSize = 12.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₦${serviceFeeNgn.format(2)}",
                        color = White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = KumbhSansFontFamily
                    )
                    Text(
                        text = "$${serviceFeeUsd.format(2)}",
                        color = SteelBlueGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = KumbhSansFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rate with timer  info icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Rate info",
                        tint = GoldenYellow,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 6.dp)
                    )
                    Text(
                        text = "Rate",
                        color = White,
                        fontSize = 10.sp,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Normal
                    )

                    Box(
                        modifier = Modifier
                            .background(DarkBlue, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "00:59",
                            color = White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "= 3,477.94 NGN/USDT",
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = KumbhSansFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .background(DarkBlue)
            )
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPENT",
                        color = White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = KumbhSansFontFamily
                    )

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = GoldenYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp
                                )
                            ) {
                                append("₦${spentAmount.format(2)} ")
                            }

                            withStyle(
                                SpanStyle(
                                    color = SteelBlueGrey,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            ) {
                                append("/ ₦${totalLimit.format(0)}")
                            }
                        },
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { spentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = GoldenYellow,
                        trackColor = Midnight.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = SteelBlueGrey, fontSize = 13.sp)
        Text(value, color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    Spacer(modifier = Modifier.height(12.dp))
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