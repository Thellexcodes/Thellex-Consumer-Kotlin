package com.thellex.pay.v2.features.payment_links

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.thellex.pay.core.decorators.PinkRed

@Composable
fun PaymentLinksScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize().background(color = PinkRed),
        contentAlignment = Alignment.Center
    ) {
        Text("Go to Ramp Transactions")
    }
}