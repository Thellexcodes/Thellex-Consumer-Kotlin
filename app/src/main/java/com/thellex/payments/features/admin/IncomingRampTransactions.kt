package com.thellex.payments.features.admin

import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.thellex.payments.core.decorators.DarkBlue
import com.thellex.payments.core.decorators.GoldenYellow
import com.thellex.payments.core.decorators.Green
import com.thellex.payments.core.decorators.KumbhSansFontFamily
import com.thellex.payments.core.decorators.Midnight
import com.thellex.payments.core.decorators.SteelBlueGrey
import com.thellex.payments.core.decorators.White
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.AdminData
import com.thellex.payments.data.model.AdminRampTransactionDTO
import com.thellex.payments.data.model.AdminRampTransactionsResponse
import com.thellex.payments.data.model.ApproveRampRequest
import com.thellex.payments.data.model.PaginatedResponse
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.data.model.UserPreferences
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.shared.CustomTopAppBar
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// -------------------- Transaction Item --------------------
@Composable
fun TransactionItem(
    transaction: AdminRampTransactionDTO,
    onApproveClick: ((ApproveResult) -> Unit)? = null
) {
    val clipboardManager: androidx.compose.ui.platform.ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBlue, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Status + Created At
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(if (transaction.approved) Green else SteelBlueGrey, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.5.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (transaction.approved) "Approved".uppercase() else "Pending".uppercase(),
                    color = White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = KumbhSansFontFamily
                )
            }

            Text(
                text = transaction.createdAt ?: "N/A",
                color = SteelBlueGrey,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TXID
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("TXID: ", color = White, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Light, fontSize = 14.sp)
            Text(transaction.txnID, color = White, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Amount
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Amount", color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontSize = 12.sp)
            Text("${transaction.mainCryptoAmount} USDT", color = GoldenYellow, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // User
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("User ID", color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontSize = 12.sp)
            Text(transaction.userUID.toString(), color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Light, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Type
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Type", color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .background(SteelBlueGrey, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.5.dp, vertical = 3.dp)
            ) {
                Text(transaction.transactionType.name.replace("_", " "), color = White, fontSize = 10.sp, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Wallet Address (copyable)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Wallet Address", color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .background(SteelBlueGrey, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.5.dp, vertical = 3.dp)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(transaction.recipientInfo.destinationAddress))
                        CustomToast.show(context, "Copied","Address copied!")
                    }
            ) {
                Text(transaction.recipientInfo.destinationAddress.uppercase(), color = White, fontSize = 10.sp, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Network
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Network", color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .background(SteelBlueGrey, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.5.dp, vertical = 3.dp)
            ) {
                Text(transaction.recipientInfo.network.uppercase(), color = White, fontSize = 10.sp, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Approve Button
        Button(
            onClick = { onApproveClick?.invoke(ApproveResult(approved = true, txId = transaction.txnID, sequenceId = transaction.sequenceId)) },
            enabled = !transaction.approved, // dynamically disabled
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow)
        ) {
            Text("APPROVE".uppercase(), fontFamily = KumbhSansFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------- Transactions List --------------------
@Composable
fun TransactionsList(
    transactions: List<AdminRampTransactionDTO>,
    onApproveClick: ((ApproveResult) -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            TransactionItem(transaction = transaction, onApproveClick = onApproveClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingRampTransactionsScreen(
    onBackClick: (() -> Unit)? = null,
    adminRampTransactions: AdminRampTransactionsResponse?,
    onApproveClick: ((ApproveResult) -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    val filteredTransactions = adminRampTransactions?.data?.filter {
        it.txnID.contains(searchQuery, ignoreCase = true)
    } ?: emptyList()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    Scaffold(
        modifier = Modifier.background(Midnight),
        topBar = {
            CustomTopAppBar(
                title = "Transactions",
                onBackClick = onBackClick,
                backgroundColor = Midnight,
                titleColor = White
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by TXID", color = White) },
                    textStyle = TextStyle(color = White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = GoldenYellow,
                        unfocusedBorderColor = SteelBlueGrey,
                        cursorColor = GoldenYellow
                    )
                )

                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = {
                        isRefreshing = true
                        onRefresh?.invoke()
                        isRefreshing = false
                    }
                ) {
                    TransactionsList(
                        transactions = filteredTransactions,
                        onApproveClick = onApproveClick
                    )
                }
            }
        }
    )
}


// -------------------- Activity --------------------
class IncomingRampTransactionsActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()

        setContent {
                val adminDataFlow = UserPreferences.getAdminResult(applicationContext)
                val adminData by adminDataFlow.collectAsState(initial = AdminData())

                IncomingRampTransactionsScreen(
                    onBackClick = { finish() },
                    adminRampTransactions = adminData?.rampTransactions,
                    onApproveClick = { result ->
                        // Call backend API
                        lifecycleScope.launch {
                            try {
                                val token = userViewModel.token.asFlow().first { !it.isNullOrBlank() } ?: return@launch
                                val adminApi = ApiClient.getAuthenticatedAdminApi(token)
                                Log.d("IncomingRampTransactionsScreen", "result is $result")
                                val response = adminApi.approveTransaction(ApproveRampRequest(
                                    approved = result.approved,
                                    txId = result.txId,
                                    sequenceId = result.sequenceId
                                ))
//                                if (response.success) {
//                                    // Refresh admin data
//                                    val refreshedData = adminApi.fetchAllRampTransactions()
//                                    UserPreferences.saveAdminResult(applicationContext, AdminData(rampTransactions = refreshedData.result))
//                                }
                            } catch (e: Exception) {
                                Log.e("Approve", "Failed to approve transaction: ${result.txId}", e)
                            }
                        }
                    },
                    onRefresh = {
                        lifecycleScope.launch {
                            val token = userViewModel.token.asFlow().first { !it.isNullOrBlank() } ?: return@launch
                            val adminApi = ApiClient.getAuthenticatedAdminApi(token)
                            val refreshedData = adminApi.fetchAllRampTransactions()
                            UserPreferences.saveAdminResult(applicationContext, AdminData(rampTransactions = refreshedData.result))
                        }
                    }
                )
            }
    }
}
//
//// -------------------- Preview --------------------
//@Preview(showBackground = true, widthDp = 360, heightDp = 640)
//@Composable
//fun PreviewIncomingRampTransactionsScreen() {
//    val dummyTransactions = AdminRampTransactionsResponse(
//        data = listOf(
//            AdminRampTransactionDTO(
//                rampId = "ramp_001",
//                txnID = "txn_2039hd832k",
//                mainCryptoAmount = 1.5,
//                mainFiatAmount = 200.0,
//                transactionType = TransactionTypeEnum.CRYPTO_DEPOSIT,
//                userUID = 12345,
//                approved = true,
//                paymentStatus = PaymentStatusEnum.Complete,
//                sequenceId = "seq_001",
//                createdAt = "2025-08-31T12:00:00Z"
//            ),
//            AdminRampTransactionDTO(
//                rampId = "ramp_002",
//                txnID = "txn_482jd9f72",
//                mainCryptoAmount = 2.0,
//                mainFiatAmount = 300.0,
//                transactionType = TransactionTypeEnum.CRYPTO_DEPOSIT,
//                userUID = 67890,
//                approved = false,
//                paymentStatus = PaymentStatusEnum.Complete,
//                sequenceId = "seq_002",
//                createdAt = "2025-08-31T12:05:00Z"
//            )
//        ),
//        pageNumber = 1,
//        lastPage = 1,
//        total = 2
//    )
//
//    MaterialTheme {
//        IncomingRampTransactionsScreen(
//            onBackClick = {},
//            adminRampTransactions = dummyTransactions
//        )
//    }
//}

// -------------------- Approve Result --------------------
data class ApproveResult(
    val approved: Boolean,
    val txId: String,
    val sequenceId: String
)
