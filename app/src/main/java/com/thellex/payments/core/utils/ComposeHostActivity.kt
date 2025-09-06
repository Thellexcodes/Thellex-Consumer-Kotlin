package com.thellex.payments.core.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thellex.payments.core.routes.ComposeRoutes
import com.thellex.payments.features.fiat.RampTransactionDetailScreen
import com.thellex.payments.features.fiat.RampTransactionsScreen
import com.thellex.payments.v2.features.payment_links.PaymentLinksScreen


class ComposeHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination = intent.getStringExtra(EXTRA_START_DESTINATION) ?: ComposeRoutes.PaymentLinks.route

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable(ComposeRoutes.PaymentLinks.route) {
                            PaymentLinksScreen(navController)
                        }
                        composable(ComposeRoutes.RampTransactions.route) {
                            RampTransactionsScreen(navController)
                        }
                        composable(
                            "${ComposeRoutes.RampTransactionDetail.route}/{rampId}",
                            arguments = listOf(navArgument("rampId") { type = androidx.navigation.NavType.StringType })
                        ) { backStackEntry ->
                            RampTransactionDetailScreen(
                                navController = navController,
                                rampId = backStackEntry.arguments?.getString("rampId")
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_START_DESTINATION = "startDestination"

        fun newIntent(context: Context, startDestination: String = ComposeRoutes.PaymentLinks.route): Intent {
            return Intent(context, ComposeHostActivity::class.java).apply {
                putExtra(EXTRA_START_DESTINATION, startDestination)
            }
        }
    }
}