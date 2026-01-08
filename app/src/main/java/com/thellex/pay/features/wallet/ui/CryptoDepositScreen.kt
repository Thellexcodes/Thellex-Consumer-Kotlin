package com.thellex.pay.features.wallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.Transparent
import com.thellex.pay.core.decorators.White
import com.thellex.pay.data.datastore.getBaseSettingsCache
import com.thellex.pay.data.model.ChainInfoDto
import com.thellex.pay.data.model.TokenInfo
import com.thellex.pay.features.wallet.model.GroupedWalletAssetDto
import com.thellex.pay.features.wallet.model.WalletBalanceDto
import com.thellex.pay.features.wallet.model.WalletDto
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
import com.thellex.pay.shared.AddressCopyButton
import com.thellex.pay.shared.AppFullWidthModal
import com.thellex.pay.shared.CenteredTopBar
import com.thellex.pay.shared.IconDisplayer
import com.thellex.pay.shared.InfoCard
import com.thellex.pay.shared.InfoCardType
import com.thellex.pay.shared.NetworkSelectionContent
import com.thellex.pay.shared.TokenSelectionContent

@Composable
fun CryptoDepositScreenRoute(
    navController: NavController,
    ticker: String
) {
    val application = LocalContext.current.applicationContext as Application
    val walletFactory = WalletManagerModelFactory(application)
    val walletViewModel: WalletManagerViewModel = viewModel(factory = walletFactory)
    val walletState = walletViewModel.walletBalance.value!!

    CryptoDepositScreen(
        navController = navController,
        walletState = walletState
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000814)
@Composable
fun CryptoDepositScreenPreview() {
    // Simulate a realistic wallet state for preview
//
//    CryptoDepositScreen(
//        navController = rememberNavController(),
//        walletState = previewWalletState
//    )
}

@SuppressLint("ContextCastToActivity")
@Composable
fun CryptoDepositScreen(
    navController: NavController,
    walletState: WalletBalanceDto? = null
) {
    val isPreview = LocalInspectionMode.current
    val application = LocalContext.current.applicationContext as Application

    var tokenIconUrl by remember { mutableStateOf<String?>(null) }
    var selectedToken by remember { mutableStateOf<TokenInfo?>(null) }

    var selectedChain by remember { mutableStateOf<ChainInfoDto?>(null) }
    var supportedChains by remember { mutableStateOf<List<ChainInfoDto>>(emptyList()) }

    var showNetworkModal by remember { mutableStateOf(false) }
    var selectedTokenId by rememberSaveable { mutableStateOf<String?>(null) }
    var chainAssets by remember { mutableStateOf<List<GroupedWalletAssetDto>>(emptyList()) }

    // Initialize walletAddress safely
    val initialWalletAddress = remember(walletState) {
        if (isPreview || walletState == null) {
            "0x80fDHUhsueiojwi8380jfhjwheu8389efjkkdfjiok"
        } else {
            walletState.wallets.values.firstOrNull()?.address.orEmpty()
        }
    }
    var walletAddress by remember { mutableStateOf(initialWalletAddress) }

    /* ───── Ticker from navigation ───── */
    val ticker = if (isPreview) {
        "BTC"
    } else {
        navController.currentBackStackEntry
            ?.arguments
            ?.getString("ticker")
            ?.uppercase()
            ?: "BTC"
    }

    /* ───── Load token + icon from cache ───── */
    LaunchedEffect(ticker) {
        if (isPreview) return@LaunchedEffect

        val cache = application.getBaseSettingsCache() ?: return@LaunchedEffect

        tokenIconUrl = cache.depositTokens
            .firstOrNull { it.ticker.equals(ticker, ignoreCase = true) }
            ?.iconUrl

        selectedToken = cache.chains
            .flatMap { it.supportedTokens }
            .firstOrNull { it.symbol.name.equals(ticker, ignoreCase = true) }

        selectedTokenId = selectedToken?.symbol?.name
    }

    /* ───── Filter chains by selected token ───── */
    LaunchedEffect(selectedToken) {
        if (isPreview) return@LaunchedEffect

        val cache = application.getBaseSettingsCache() ?: return@LaunchedEffect

        val validChains = selectedToken?.let { token ->
            cache.chains.filter { chain ->
                chain.supportedTokens.any { it.symbol == token.symbol }
            }
        }.orEmpty()

        supportedChains = validChains

        if (selectedChain == null || selectedChain !in validChains) {
            selectedChain = validChains.firstOrNull()
        }
    }

    /* ───── Resolve wallet assets and wallet address for selected chain/token ───── */
    LaunchedEffect(walletState, selectedChain, selectedToken) {
        if (walletState == null || selectedChain == null || selectedToken == null) {
            chainAssets = emptyList()
            walletAddress = ""
            return@LaunchedEffect
        }

        val networkKey = selectedChain!!.id.name.lowercase()

        // Filter wallets for this network
        val walletsForNetwork = walletState.wallets.values
            .filter { it.network.equals(networkKey, ignoreCase = true) }

        if (walletsForNetwork.isEmpty()) {
            chainAssets = emptyList()
            walletAddress = ""
            return@LaunchedEffect
        }

        // Find wallet that has the selected token
        val matchingWallet = walletsForNetwork.firstOrNull { wallet ->
            wallet.assets.any { it.assetCode.equals(selectedToken!!.symbol.name, ignoreCase = true) }
        }

        // Ensure one wallet per chain; fallback to first wallet if none has the token
        val selectedWallet = matchingWallet ?: walletsForNetwork.firstOrNull()

        walletAddress = selectedWallet?.address.orEmpty()
        chainAssets = selectedWallet?.assets.orEmpty()
    }

    /* ───── QR Code bitmap ───── */
    val qrBitmap = remember(walletAddress) {
        if (walletAddress.isBlank()) null
        else generateQrCode(walletAddress)
    }

    AppGradientBackground {
        Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {

                CenteredTopBar(
                    title = "",
                    onBackClick = { navController.popBackStack() }
                )

                Spacer(Modifier.height(24.dp))

                /* ───── Token + Network Row ───── */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconDisplayer(
                            ticker = ticker,
                            iconUrl = tokenIconUrl
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(text = ticker, color = White)
                    }

                    Spacer(Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBlue)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.clickable { showNetworkModal = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedChain?.let { chain ->
                                IconDisplayer(
                                    ticker = chain.id.name.uppercase(),
                                    iconUrl = chain.iconUrl
                                )

                                Text(
                                    text = chain.displayName,
                                    color = White,
                                    fontSize = 10.sp,
                                    fontFamily = KumbhSansFontFamily,
                                    fontWeight = FontWeight.Light
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Network",
                                tint = Color.White,
                                modifier = Modifier.height(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Scan code to continue process",
                    color = White,
                    fontSize = 14.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )

                Spacer(Modifier.height(24.dp))

                /* ───── QR Container ───── */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E2138)),
                    contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .padding(18.dp)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        qrBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Wallet QR",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .width(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2138)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconDisplayer(
                            ticker = ticker,
                            iconUrl = tokenIconUrl,
                            modifier = Modifier
                                .height(32.dp)
                                .width(32.dp),
                            fallbackRes = R.drawable.icon_avatar
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* ───── Wallet Address ───── */
                Column {
                    Text(
                        text = "WALLET ADDRESS",
                        color = White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(6.dp))

                    AddressCopyButton(
                        address = walletAddress,
                        iconTint = White,
                        modifier = Modifier.width(18.dp).height(18.dp),
                    )
                }
            }

            AppFullWidthModal(
                show = showNetworkModal,
                onDismiss = { showNetworkModal = false },
                title = "Select Network"
            ) {
                Column {
                    InfoCard(
                        text = "Ensure that the network matches the address or assets may be lost.",
                        type = InfoCardType.WARNING
                    )

                    Spacer(Modifier.height(20.dp))

                    NetworkSelectionContent(
                        chains = supportedChains,
                        onChainSelected = {
                            selectedChain = it
                            showNetworkModal = false
                        }
                    )
                }
            }
        }
    }
}

fun generateQrCode(text: String, size: Int = 512): Bitmap {
    val hints = mapOf(
        EncodeHintType.MARGIN to 0,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
    )

    val bitMatrix = MultiFormatWriter().encode(
        text,
        BarcodeFormat.QR_CODE,
        size,
        size,
        hints
    )

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) Color.White.toArgb()
                else Color.Transparent.toArgb()
            )
        }
    }

    return bitmap
}