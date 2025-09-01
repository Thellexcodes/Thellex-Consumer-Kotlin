package com.thellex.payments.v2.features.payment_links

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentLinksScreen(
    navController: NavController,
    onCreateClick: () -> Unit
) {
    // Hardcoded dummy links
    val dummyLinks = remember {
        listOf(
            Triple("Invoice #1001", 49.99, "Pending"),
            Triple("Invoice #1002", 79.50, "Paid"),
            Triple("Invoice #1003", 120.0, "Pending"),
            Triple("Invoice #1004", 200.0, "Paid")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Payment Links") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Text("+")
            }
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(dummyLinks) { link ->
                    PaymentLinkItem(title = link.first, amount = link.second, status = link.third) {
                        // Handle click on this payment link
                    }
                }
            }
        }
    )
}

@Composable
fun PaymentLinkItem(title: String, amount: Double, status: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text("Amount: \$${amount}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                status,
                style = MaterialTheme.typography.bodyMedium,
                color = if (status == "Paid") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary
            )
        }
    }
}