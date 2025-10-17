package com.thellex.pay.features.fiat

import CustomTopAppBar
import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.PaddedWrapper
import com.thellex.pay.data.model.PaymentStatusEnum
import com.thellex.pay.data.model.TransactionTypeEnum
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory

//class FiatRapTransactionsActivity : AppCompatActivity() {
//    private lateinit var topBar: Helpers.TopAppBarController
//    private lateinit var binding: ActivityFiatRampTransactionsBinding
//    private lateinit var userViewModel: UserViewModel
//
//    private var allTransactions: List<IFiatCryptoRampTransactionsDto> = emptyList()
//    private lateinit var adapter: RampTransactionsAdapter
//
//    private val animationDuration = 300L
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityFiatRampTransactionsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        ActivityTracker.add(this)
//        // System bar setup
//        disableDecorFitsSystemWindows()
//        setTransparentStatusBarWithWhiteIcons()
//        binding.main.applyAdvancedSystemBarInsets()
//
//        // Top bar
//        topBar = Helpers.setupTopAppBar(
//            activity = this,
//            rootView = findViewById(R.id.include_top_app_bar),
//            title = "Ramp Transactions"
//        )
//
//        // ViewModel setup
//        userViewModel = ViewModelProvider(
//            this,
//            UserViewModelFactory(applicationContext)
//        )[UserViewModel::class.java]
//
//        setupTabs()
//        setupRecyclerView()
//        observeUser()
//    }
//
//    private fun observeUser() {
//        userViewModel.authResult.observe(this) { userDto ->
//            userDto?.fiatCryptoRampTransactions?.let { transactions ->
//                allTransactions = transactions
//                filterTransactions(binding.tabLayout.selectedTabPosition)
//            }
//        }
//    }
//
//    private fun setupTabs() {
//        val tabTitles = listOf("Buy", "Sell")
//        val darkBlue = ContextCompat.getColor(binding.root.context, R.color.darkBlue)
//        val midnight = ContextCompat.getColor(binding.root.context, R.color.midnight)
//        val white = ContextCompat.getColor(binding.root.context, R.color.white)
//
//        tabTitles.forEach { title ->
//            val tab = binding.tabLayout.newTab()
//            tab.customView = layoutInflater.inflate(R.layout.ramp_custom_tab, null).apply {
//                findViewById<TextView>(R.id.tabText).text = title
//
//                // Set rounded drawable background with midnight color
//                val bg = GradientDrawable().apply {
//                    cornerRadius = resources.getDimension(R.dimen.dp_4)
//                    setColor(midnight)
//                    setStroke(1.dpToPx(), ContextCompat.getColor(context, R.color.transparent))
//                }
//                background = bg
//
//                findViewById<TextView>(R.id.tabText).setTextColor(white)
//            }
//            binding.tabLayout.addTab(tab)
//        }
//
//        fun animateBackgroundDrawableColor(
//            drawable: GradientDrawable,
//            fromColor: Int,
//            toColor: Int
//        ) {
//            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
//            colorAnimation.duration = animationDuration
//            colorAnimation.addUpdateListener { animator ->
//                drawable.setColor(animator.animatedValue as Int)
//            }
//            colorAnimation.start()
//        }
//
//        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                tab.customView?.let { customView ->
//                    val bg = customView.background as? GradientDrawable ?: return
//                    val currentColor = bg.color?.defaultColor ?: midnight
//                    animateBackgroundDrawableColor(bg, currentColor, darkBlue)
//
//                    val textView = customView.findViewById<TextView>(R.id.tabText)
//                    textView.setTextColor(white)
//                    customView.isSelected = true
//                }
//                filterTransactions(tab.position)
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab) {
//                tab.customView?.let { customView ->
//                    val bg = customView.background as? GradientDrawable ?: return
//                    val currentColor = bg.color?.defaultColor ?: darkBlue
//                    animateBackgroundDrawableColor(bg, currentColor, midnight)
//
//                    val textView = customView.findViewById<TextView>(R.id.tabText)
//                    textView.setTextColor(white)
//                    customView.isSelected = false
//                }
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab) {}
//        })
//
//        // Select and highlight the first tab instantly on load with no animation
//        binding.tabLayout.getTabAt(0)?.let { firstTab ->
//            firstTab.select()
//            firstTab.customView?.let { customView ->
//                val bg = customView.background as? GradientDrawable ?: return@let
//                bg.setColor(darkBlue)
//                customView.findViewById<TextView>(R.id.tabText).setTextColor(white)
//                customView.isSelected = true
//            }
//        }
//    }
//
//    private fun filterTransactions(tabPosition: Int) {
//        val filtered = when (tabPosition) {
//            0 -> allTransactions.filter { it.transactionType == TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT }
//            1 -> allTransactions.filter { it.transactionType == TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL }
//            else -> allTransactions
//        }.sortedByDescending { it.createdAt }
//
//        Log.d("TAGY", "all tttt $filtered")
//
//        adapter.submitList(filtered)
//    }
//
//    private fun setupRecyclerView() {
//        adapter = RampTransactionsAdapter()
//        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
//        binding.transactionsRecyclerView.adapter = adapter
//        binding.transactionsRecyclerView.addItemDecoration(VerticalSpaceItemDecoration(25))
//    }
//}
//
//class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
//    override fun getItemOffsets(
//        outRect: android.graphics.Rect, view: android.view.View, parent: RecyclerView, state: RecyclerView.State
//    ) {
//        if (parent.getChildAdapterPosition(view) != state.itemCount - 1) {
//            outRect.bottom = verticalSpaceHeight
//        } else {
//            outRect.bottom = 0
//        }
//    }
//}
//
//// Extension function to convert dp to pixels
//fun Int.dpToPx(): Int =
//    (this * Resources.getSystem().displayMetrics.density).toInt()



// Sample data class for transactions
data class RampTransaction(
    val rampId: String,
    val iconRes: Int,
    val description: String,
    val timestamp: String,
    val amount: String,
    val status: PaymentStatusEnum,
    val statusColor: Color,
    val type: String
)

// Transaction Item
@Composable
fun RampTransactionItem(transaction: RampTransaction, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onClick(transaction.rampId) }
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            painter = painterResource(id = transaction.iconRes),
            contentDescription = transaction.description,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )

        // Description and Timestamp
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 31.dp)
        ) {
            Text(
                text = transaction.description,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = transaction.timestamp,
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Normal
            )
        }

        // Amount and Status
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = transaction.amount,
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.status.value.uppercase(),
                color = transaction.statusColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Light,
                fontFamily = KumbhSansFontFamily
            )
        }
    }
}

@Composable
fun RampTransactionsScreen(
    navigation: NavHostController,
)  {
    val application = LocalContext.current.applicationContext as Application
    val factory = UserViewModelFactory(application)
    val userViewModel: UserViewModel = viewModel(factory = factory)
    val authResult by userViewModel.authResult.observeAsState()

    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("REQUEST", "WITHDRAW")

    val transactions = authResult?.fiatCryptoRampTransactions?.mapNotNull { transaction ->
        try {
            RampTransaction(
                rampId = transaction.id,
                iconRes = Helpers.getIconResIdForToken(transaction.recipientInfo.assetCode),
                description = transaction.transactionMessage ?: "Unknown transaction",
                timestamp = Helpers.convertToLocalTime(transaction.createdAt),
                amount = "${transaction.netCryptoAmount ?: transaction.mainAssetAmount} ${transaction.recipientInfo.assetCode.uppercase() ?: "USD"}",
                status = transaction.paymentStatus,
                statusColor =  Helpers.determinePaymentStatusColor(transaction.paymentStatus),
                type = when (transaction.transactionType) {
                    TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT -> "REQUEST"
                    TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> "WITHDRAW"
                    else -> "REQUEST"
                }
            )
        } catch (e: Exception) {
            null // Skip invalid transactions
        }
    } ?: emptyList()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CustomTopAppBar(
                title = "TRANSACTIONS",
                backgroundColor = Color.Transparent,
                onBackClick ={ navigation.popBackStack() }
            )
        },
        content = { paddingValues ->
            PaddedWrapper(paddingValues = paddingValues) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = White,
                    indicator = { /* no underline */ },
                    divider = {} // remove bottom divider
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            selectedContentColor = White,
                            unselectedContentColor = SteelBlueGrey,
                            modifier = Modifier
                                .background(
                                    if (selectedTabIndex == index) DarkBlue else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    title,
                                    fontSize = 10.sp,
                                    fontFamily = KumbhSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            }
                        }
                    }
                }

                val filteredTransactions = when (selectedTabIndex) {
                    0 -> transactions.filter { it.type == "REQUEST" }
                    1 -> transactions.filter { it.type == "WITHDRAW" }
                    else -> emptyList()
                }

                LazyColumn {
                    filteredTransactions.groupBy { it.timestamp.split(",")[0].trim() }.forEach { (date, transactionsByDate) ->
                        item {
                            Spacer(modifier = Modifier.height(26.dp))
                            Text(
                                text = date,
                                color = Color.White,
                                fontFamily = KumbhSansFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        items(transactionsByDate) { transaction ->
                            RampTransactionItem(transaction) { rampId ->
                                try {
                                    navigation.navigate("${ComposeRoutes.RampTransactionDetail.route}/$rampId")
                                    Log.d("TXN", "Navigated to detail with rampId: $rampId")
                                } catch (e: Exception) {
                                    Log.e("TXN", "Navigation failed: Unexpected error - ${e.message}")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
