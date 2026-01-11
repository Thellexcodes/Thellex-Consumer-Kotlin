import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.BrightSkyBlue
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.PinkRed
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.Transparent
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.data.enums.OnOffRampAction
import com.thellex.pay.data.enums.RoleEnum
import com.thellex.pay.data.model.AdminData
import com.thellex.pay.data.model.BaseSettingsViewModel
import com.thellex.pay.data.model.BaseSettingsViewModelFactory
import com.thellex.pay.data.model.DepositTokenDto
import com.thellex.pay.data.model.ITransactionHistoryDto
import com.thellex.pay.data.model.PaymentStatusEnum
import com.thellex.pay.data.model.TransactionTypeEnum
import com.thellex.pay.data.model.UserEntity
import com.thellex.pay.data.repository.BaseSettingsRepository
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.wallet.model.WalletState
import com.thellex.pay.features.wallet.utils.WalletManagerModelFactory
import com.thellex.pay.features.wallet.utils.WalletManagerViewModel
import com.thellex.pay.network.services.ApiClient
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import com.thellex.pay.shared.AppFullWidthModal
import com.thellex.pay.shared.AssetChip
import com.thellex.pay.shared.IconTextButton
import com.thellex.pay.shared.NoTransactionsPlaceholder
import com.thellex.pay.shared.NotificationIconWithBadge
import com.thellex.pay.shared.NotificationIconWithBadgePreview
import com.thellex.pay.shared.TotalBalanceSection
import com.thellex.pay.shared.TransactionItem
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.http.Body

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    navController: NavController,
    walletState: WalletState? =  null,
    authResult: UserEntity? = null,
    supportedTokens: List<DepositTokenDto>,
    transactionHistoryList:  List<ITransactionHistoryDto>
    ){
    var selectedTab by remember { mutableStateOf(WalletTab.DEPOSIT) }

    AppGradientBackground {
        Scaffold(
            bottomBar = { BottomNavigationBar() },
            modifier = Modifier.fillMaxSize().background(color = PinkRed)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
            ){
                TopBar(
                    navController,
                    authResult = authResult
                )
                Spacer(modifier = Modifier.height(24.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Midnight)
                    ,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                item { BalanceSection(walletState) }
                item { ActionButtons(navController) }
                item { ViewAssets(navController, supportedTokens) }
                item { RecentTransactionsHeader() }
                item {
                    DepositWithdrawTabs(
                        navController = navController,
                        transactions =  transactionHistoryList,
                        selectedTab = selectedTab,
                        onTabChanged = { selectedTab = it }
                    )
                }

                when (selectedTab) {
                    WalletTab.DEPOSIT -> {
                        depositItems(
                            navController = navController,
                            transactions = transactionHistoryList
                        )
                    }

                    WalletTab.WITHDRAW -> {
                        withdrawItems(
                            navController = navController,
                            transactions = transactionHistoryList
                        )
                    }
                }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    navController: NavController,
    authResult: UserEntity? = null
) {
    val notificationCount = authResult?.notifications?.size ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon_default_avatar),
            contentDescription = "Logo",
            modifier = Modifier.height(24.dp).width(24.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            NotificationIconWithBadge(
                count = notificationCount,
                onClick = {
                    navController.navigate(ComposeRoutes.Notifications.route)
                }
          )
        }
    }
}


@Composable
fun BalanceSection(walletState: WalletState?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TotalBalanceSection(balance = "USD ${walletState?.totalInUsd}")

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBlue)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_usd),
                contentDescription = "USD",
                modifier = Modifier.height(16.dp).width(16.dp),
                tint = Color.Unspecified
            )

            Icon(
                painter = painterResource(id = R.drawable.icon_arrow_down),
                contentDescription = "Toggle",
                modifier = Modifier.width(16.dp).height(16.dp),
                tint = White
            )
        }
    }
}

@Composable
fun ActionButtons(navController: NavController) {
    var showWithdrawModal by remember { mutableStateOf(false) }
    var showRequestModal by remember { mutableStateOf(false) }

    // --- Withdraw Modal ---
    if (showWithdrawModal) {
        AppFullWidthModal(
            onDismiss = { showWithdrawModal = false },
            title = "Withdraw Options",
            show = showWithdrawModal
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        showWithdrawModal = false
                        navController.navigate(
                            "${ComposeRoutes.OnOffRamp.route}/${OnOffRampAction.CRYPTO_TO_FIAT_OFF_RAMP.name}"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) {
                    Text(
                        text = "Withdraw to Bank",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        showWithdrawModal = false
                        navController?.navigate(ComposeRoutes.CryptoWithdrawal.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) {
                    Text(
                        text = "Withdraw to Another Wallet",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // --- Request/Deposit Modal ---
    if (showRequestModal) {
        AppFullWidthModal(
            onDismiss = { showRequestModal = false },
            title = "Deposit Options",
            show = showRequestModal
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        // Handle Request/Deposit from Bank
                        showRequestModal = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) {
                    Text(
                        text = "Deposit from Bank",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        // Handle Request/Deposit from Another Wallet
                        showRequestModal = false
                        navController.navigate(ComposeRoutes.CryptoDeposit.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) {
                    Text(
                        text = "Deposit from Another Wallet",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // --- Action Buttons Row ---
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconTextButton(
            text = "WITHDRAW",
            icon = painterResource(id = R.drawable.icon_send_new),
            backgroundColor = GoldenYellow,
            iconRotation = 250f,
            onClick = { showWithdrawModal = true },
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = KumbhSansFontFamily
            )
        )

        IconTextButton(
            text = "REQUEST",
            icon = painterResource(id = R.drawable.icon_request),
            backgroundColor = BrightSkyBlue,
            onClick = { showRequestModal = true },
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = KumbhSansFontFamily
            )
        )
    }
}

@Composable
private fun AssetStack(
    tokens: List<DepositTokenDto>
) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .wrapContentWidth(unbounded = true)
    ) {
        tokens
            .take(4)
            .reversed()
            .forEachIndexed { index, token ->
            AssetChip(
                label = token.ticker,
                iconUrl = token.iconUrl,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .offset(x = (-14 * index).dp)
                    .zIndex((tokens.size - index).toFloat())
            )
        }
    }
}

@Composable
fun ViewAssets(
    navController: NavController,
    supportedTokens: List<DepositTokenDto>
) {
    Box(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(7.dp))
                .background(DarkBlue)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    Log.d("Base", "clicking")
                    navController.navigate(ComposeRoutes.WalletHome.route){
                        launchSingleTop = true
                    }
                }
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VIEW ASSETS",
                color = White,
                fontSize = 12.sp,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Bold
            )

            AssetStack(tokens = supportedTokens)
        }
    }
}

@Composable
fun RecentTransactionsHeader() {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RECENT TRANSACTION",
                color = White,
                fontSize = 12.sp,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

enum class WalletTab {
    DEPOSIT,
    WITHDRAW
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DepositWithdrawTabs(
    navController: NavController,
    modifier: Modifier = Modifier,
    transactions: List<ITransactionHistoryDto>,
    selectedTab: WalletTab,
    onTabChanged: (WalletTab) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        Log.d("Base", "transactions is $transactions")

        Row(modifier = Modifier.fillMaxWidth()) {
            WalletTabItem(
                text = "DEPOSIT",
                isSelected = selectedTab == WalletTab.DEPOSIT,
                onClick = { onTabChanged(WalletTab.DEPOSIT) },
                modifier = Modifier.weight(1f)
            )

            WalletTabItem(
                text = "WITHDRAW",
                isSelected = selectedTab == WalletTab.WITHDRAW,
                onClick = { onTabChanged(WalletTab.WITHDRAW) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun WalletTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) DarkBlue else Transparent,
                shape = RoundedCornerShape(5.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) White else SteelBlueGrey,
            fontSize = 10.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun LazyListScope.withdrawItems(
    navController: NavController,
    transactions: List<ITransactionHistoryDto>
) {
    val items = transactions.filter {
        it.transactionType == TransactionTypeEnum.CRYPTO_WITHDRAWAL
    }

    if (items.isEmpty()) {
        item { NoTransactionsPlaceholder() }
        return
    }

    items(
        items = items,
        key = { it.id }
    ) { transaction ->
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            TransactionItem(navController, transaction)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun LazyListScope.depositItems(
    navController: NavController,
    transactions: List<ITransactionHistoryDto>
) {
    val items = transactions.filter {
        it.transactionType == TransactionTypeEnum.CRYPTO_DEPOSIT
    }

    if (items.isEmpty()) {
        item { NoTransactionsPlaceholder() }
        return
    }

    items(
        items = items,
        key = { it.id }
    ) { transaction ->
        Box(modifier = Modifier.padding(horizontal = 16.dp)){
            TransactionItem(navController, transaction)
        }
    }
}

@Composable
fun BottomNavigationBar() {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Black,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            BottomNavItem(
                label = "Home",
                iconRes = R.drawable.icon_home,
                isSelected = true,
                modifier = Modifier.weight(1f)
            )

            BottomNavItem(
                label = "Cash",
                iconRes = R.drawable.icon_cash,
                isSelected = false,
                modifier = Modifier.weight(1f)
            )

            BottomNavItem(
                label = "POS",
                iconRes = R.drawable.icon_pos,
                isSelected = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { /* TODO */ },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) GoldenYellow else SteelBlueGrey
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Normal,
            color = if (isSelected) GoldenYellow else SteelBlueGrey
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardRoute(
    navController: NavController,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val baseSettingsRepo = remember {
        BaseSettingsRepository(context)
    }

    val baseSettingsVM: BaseSettingsViewModel = viewModel(
        factory = BaseSettingsViewModelFactory(context)
    )

    val baseSettings by baseSettingsVM
        .baseSettings
        .collectAsState()

    //Wallet VM
    val walletFactory =  WalletManagerModelFactory(application)
    val walletModel: WalletManagerViewModel = viewModel(factory = walletFactory)

    // User VM
    val userFactory = UserViewModelFactory(application)
    val userViewModel: UserViewModel = viewModel(factory = userFactory)

    val authToken by userViewModel.token.observeAsState()
    val authResult by userViewModel.authResult.observeAsState()
    val adminData by userViewModel.adminData.observeAsState()
    val walletState by walletModel.walletBalance.observeAsState()

    LaunchedEffect(authToken, authResult) {
        if (authToken.isNullOrBlank() || authResult == null) return@LaunchedEffect

        try {
            val token = authToken!!

            // APIs
            val adminApi = ApiClient.getAuthenticatedAdminApi(context, token)
            val userApi = ApiClient.getAuthenticatedUserApi(context, token)
            val settingsApi = ApiClient.getAppApi(context, token)

            val currentUser = authResult!!
            val currentAdminData = adminData ?: AdminData()

            coroutineScope {
                // Concurrent calls
                val adminRampDeferred =
                    if (currentUser.role == RoleEnum.SUPER_ADMIN) {
                        async { adminApi.fetchAllRampTransactions() }
                    } else null

                val userRampDeferred = async { userApi.fetchRampTransactions() }
                val userTxnHistoryDeferred = async { userApi.fetchTransactionHistory() }
                val userNotificationsDeferred = async { userApi.fetchNotifications() }

                val updatedAdminData = try {
                    val adminRampTransactions = adminRampDeferred?.await()?.result
                    currentAdminData.copy(rampTransactions = adminRampTransactions)
                } catch (e: Exception) {
                    Log.e("DashboardRoute", "Admin ramp fetch failed", e)
                    currentAdminData
                }

                // User data
                val userRampTransactions = runCatching {
                    userRampDeferred.await().result?.data ?: emptyList()
                }.getOrDefault(emptyList())

                val userTxnHistory = runCatching {
                    userTxnHistoryDeferred.await().result?.data ?: emptyList()
                }.getOrDefault(emptyList())

                val userNotifications = runCatching {
                    userNotificationsDeferred.await().result?.data ?: emptyList()
                }.getOrDefault(emptyList())

                // Base settings (fire & forget)
                launch {
                    baseSettingsRepo.getBaseSettings(token)

                    walletModel.loadWallet( tokenProvider = { token } ) }

                // Merge user
                val mergedUser = currentUser.copy(
                    fiatCryptoRampTransactions = userRampTransactions,
                    transactionHistory = userTxnHistory,
                    notifications = userNotifications
                )

                // Save to VM
                userViewModel.saveAdminResult(updatedAdminData)
                userViewModel.saveAuthResult(mergedUser)
            }
        } catch (e: Exception) {
            Log.e("DashboardRoute", "Error loading app data", e)
        }
    }

    val transactionHistoryUiList by remember(authResult) {
        derivedStateOf {
            authResult?.transactionHistory.orEmpty()
        }
    }

    DashboardScreen(
        navController = navController,
        walletState = walletState,
        authResult = authResult,
        supportedTokens = baseSettings?.depositTokens.orEmpty(),
        transactionHistoryList = transactionHistoryUiList
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    val previewTokens = listOf(
        DepositTokenDto(
            name = "USD Coin",
            ticker = "USDC",
            iconUrl = "https://cryptologos.cc/logos/usd-coin-usdc-logo.png"
        ),
        DepositTokenDto(
            name = "Tether USD",
            ticker = "USDT",
            iconUrl = "https://cryptologos.cc/logos/tether-usdt-logo.png"
        )
    )

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


    DashboardScreen(
        navController = rememberNavController(),
        supportedTokens = previewTokens,
        transactionHistoryList = listOf(sampleTransaction)
    )
}
