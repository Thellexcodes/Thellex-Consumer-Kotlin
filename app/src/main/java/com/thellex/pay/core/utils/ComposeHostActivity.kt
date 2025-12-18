package com.thellex.pay.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.features.auth.ui.SecuritySettingsScreen
import com.thellex.pay.features.fiat.RampTransactionDetailScreen
import com.thellex.pay.features.fiat.RampTransactionsScreen
import com.thellex.pay.screens.WebViewScreen
import com.thellex.pay.v2.features.payment_links.PaymentLinksScreen

class ComposeHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log the incoming Intent
        val startDestination = intent.getStringExtra(EXTRA_START_DESTINATION)
            ?: ComposeRoutes.PaymentLinks.route

        // Extract rampId if present in startDestination
        val initialRampId: String = startDestination
            .takeIf { it.startsWith(ComposeRoutes.RampTransactionDetail.route) }
            ?.substringAfter("${ComposeRoutes.RampTransactionDetail.route}/") ?: ""

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = startDestination) {

                    composable(ComposeRoutes.PaymentLinks.route) {
                        PaymentLinksScreen(navController)
                    }

                    composable(ComposeRoutes.RampTransactions.route) {
                        RampTransactionsScreen(navController)
                    }

                    composable(ComposeRoutes.SecuritySettings.route) {
                        SecuritySettingsScreen(navController,
                        onPinSuccess = {
                            setResult(Activity.RESULT_OK)
                            finish()
                        })
                    }

                    composable(
                        route = "${ComposeRoutes.RampTransactionDetail.route}/{rampId}",
                        arguments = listOf(navArgument("rampId") {
                            type = androidx.navigation.NavType.StringType
                            defaultValue = initialRampId
                        })
                    ) { backStackEntry ->
                        val rampId = backStackEntry.arguments?.getString("rampId")
                        RampTransactionDetailScreen(
                            navController = navController,
                            rampId = rampId
                        )
                    }

                    composable(
                        route = "${ComposeRoutes.WebView.route}/{url}",
                        arguments = listOf(navArgument("url") {
                            type = androidx.navigation.NavType.StringType
                        })
                    ) { backStackEntry ->
                        val url = backStackEntry.arguments?.getString("url")
                        if (url != null) {
                            WebViewScreen(url = url)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_START_DESTINATION = "startDestination"

        fun newIntent(
            context: Context,
            startDestination: String = ComposeRoutes.PaymentLinks.route
        ): Intent {
            return Intent(context, ComposeHostActivity::class.java).apply {
                putExtra(EXTRA_START_DESTINATION, startDestination)
            }
        }

        fun newRampTransactionDetailIntent(context: Context, rampId: String): Intent {
            val routeWithParam = "${ComposeRoutes.RampTransactionDetail.route}/$rampId"
            Log.d("ComposeHostActivity", "Creating RampTransactionDetail Intent with route: $routeWithParam")
            return newIntent(context, routeWithParam)
        }

        fun newWebViewIntent(context: Context, url: String): Intent {
            val routeWithParam = "${ComposeRoutes.WebView.route}/$url"
            Log.d("ComposeHostActivity", "Creating WebView Intent with route: $routeWithParam")
            return newIntent(context, routeWithParam)
        }
    }
}
