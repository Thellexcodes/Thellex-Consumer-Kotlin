package com.thellex.payments.features.admin

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.thellex.payments.core.decorators.BrightSkyBlue
import com.thellex.payments.core.decorators.DarkBlue
import com.thellex.payments.core.decorators.GoldenYellow
import com.thellex.payments.core.decorators.Green
import com.thellex.payments.core.decorators.KumbhSansFontFamily
import com.thellex.payments.core.decorators.Midnight
import com.thellex.payments.core.decorators.SteelBlueGrey
import com.thellex.payments.core.decorators.White
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.shared.CustomTopAppBar
import kotlinx.coroutines.launch

// -------------------- Data Model --------------------
data class AllRampIncomingTransactions(
    val rampId: String,
    val txnID: String,
    val mainCryptoAmount: Double,
    val mainFiatAmount: Double,
    val transactionType: TransactionTypeEnum,
    val userUID: Int,
    val providerTransactionID: String? = null,
    val approved: Boolean? = null
)

// -------------------- Transaction Item --------------------
@Composable
fun TransactionItem(
    transaction: AllRampIncomingTransactions,
    onClick: (() -> Unit)? = null,
    onApproveClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBlue, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Status & Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Green, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.5.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (transaction.approved == true) "Approved" else "Pending",
                    color = White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = KumbhSansFontFamily
                )
            }
            Text(
                text = "Aug 23, 2025, 3:15 PM",
                color = SteelBlueGrey,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TXID
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "TXID:",
                color = White,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp
            )
            Text(
                transaction.txnID,
                color = White,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Amount", color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal)
            Text("${transaction.mainCryptoAmount} USDT", color = GoldenYellow, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // User
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("User", color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal)
            Text("johndoe@gmail.com", color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Light, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Type
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Type", color = SteelBlueGrey, fontFamily = KumbhSansFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal)
            Box(
                modifier = Modifier
                    .background(SteelBlueGrey, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.5.dp, vertical = 3.dp)
            ) {
                Text("On Ramp", color = White, fontSize = 10.sp, fontFamily = KumbhSansFontFamily, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Approve Button
        Button(
            onClick = { onApproveClick?.invoke() },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldenYellow
            )
        ) {
            Text(
                "APPROVE",
                fontFamily = KumbhSansFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -------------------- Transactions List --------------------
@Composable
fun TransactionsList(transactions: List<AllRampIncomingTransactions>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            TransactionItem(transaction = transaction, onApproveClick = {
                // Handle approve click
            })
        }
    }
}

// -------------------- Screen Composable --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingRampTransactionsScreen(
    onBackClick: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }

    val sampleData = listOf(
        AllRampIncomingTransactions(
            rampId = "ramp_001",
            txnID = "txn_2039hd832k",
            mainCryptoAmount = 1.5,
            mainFiatAmount = 200.0,
            transactionType = TransactionTypeEnum.CRYPTO_DEPOSIT,
            userUID = 12345,
            providerTransactionID = "prov_txn_789",
            approved = true
        ),
        AllRampIncomingTransactions(
            rampId = "ramp_002",
            txnID = "txn_482jd9f72",
            mainCryptoAmount = 2.0,
            mainFiatAmount = 300.0,
            transactionType = TransactionTypeEnum.CRYPTO_DEPOSIT,
            userUID = 67890,
            providerTransactionID = "prov_txn_456",
            approved = false
        )
    )

    val filteredTransactions = sampleData.filter {
        it.txnID.contains(searchQuery, ignoreCase = true)
    }

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
                // Search Bar
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

                // Transactions list
                TransactionsList(transactions = filteredTransactions)
            }
        }
    )
}

// -------------------- Activity --------------------
class IncomingRampTransactionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()

        lifecycleScope.launch {

        }

        setContent {
            MaterialTheme {
                IncomingRampTransactionsScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

// -------------------- Preview --------------------
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewIncomingRampTransactionsScreen() {
    MaterialTheme {
        IncomingRampTransactionsScreen()
    }
}
