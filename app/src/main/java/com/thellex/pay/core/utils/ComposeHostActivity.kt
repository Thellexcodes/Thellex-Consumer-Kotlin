package com.thellex.pay.core.utils

import DashboardRoute
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.data.enums.OnOffRampAction
import com.thellex.pay.features.auth.ui.SecuritySettingsScreen
import com.thellex.pay.features.fiat.RampTransactionDetailScreen
import com.thellex.pay.features.fiat.RampTransactionsScreen
import com.thellex.pay.features.fiat.ui.On_Off_RampScreenRoute
import com.thellex.pay.features.notifications.ui.NotificationsScreenRoute
import com.thellex.pay.features.wallet.ui.AssetDetailScreenRoute
import com.thellex.pay.features.wallet.ui.CryptoDepositScreenRoute
import com.thellex.pay.features.wallet.ui.CryptoDepositTokensSelectionRoute
import com.thellex.pay.features.wallet.ui.CryptoTransactionDetailRoute
import com.thellex.pay.features.wallet.ui.CryptoTransactionSummary
import com.thellex.pay.features.wallet.ui.CryptoWithdrawalReviewRoute
import com.thellex.pay.features.wallet.ui.CryptoWithdrawalScreenRoute
import com.thellex.pay.features.wallet.ui.WalletScreenRoute
import com.thellex.pay.screens.WebViewScreen
import com.thellex.pay.v2.features.payment_links.PaymentLinksScreen
import kotlinx.serialization.json.Json

class ComposeHostActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is the route we *want* to open (may contain arguments like long JSON)
        val intendedRoute = intent.getStringExtra(EXTRA_INTENDED_ROUTE)

        setContent {
            val navController = rememberNavController()

            // Always start on a safe route with NO arguments
            NavHost(
                navController = navController,
                startDestination = ComposeRoutes.PaymentLinks.route
            ) {
                composable(ComposeRoutes.Dashboard.route){
                    DashboardRoute(navController)
                }

                composable(ComposeRoutes.PaymentLinks.route) {
                    PaymentLinksScreen(navController)
                }

                composable(ComposeRoutes.Notifications.route) {
                    NotificationsScreenRoute(navController)
                }

                composable(ComposeRoutes.RampTransactions.route) {
                    RampTransactionsScreen(navController)
                }

                composable(ComposeRoutes.SecuritySettings.route) {
                    SecuritySettingsScreen(navController, onPinSuccess = {
                        setResult(Activity.RESULT_OK)
                        finish()
                    })
                }

                composable(ComposeRoutes.WalletHome.route) {
                    WalletScreenRoute(navController)
                }

                composable(ComposeRoutes.CryptoWithdrawal.route) {
                    CryptoWithdrawalScreenRoute(navController)
                }

                composable(ComposeRoutes.CryptoDepositTokensSelection.route) {
                    CryptoDepositTokensSelectionRoute(navController)
                }

                composable(
                    route = "${ComposeRoutes.CryptoDeposit.route}?ticker={ticker}",
                    arguments = listOf(
                        navArgument("ticker") {
                            type = NavType.StringType
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val ticker = backStackEntry.arguments?.getString("ticker")
                    CryptoDepositScreenRoute(navController = navController, ticker = "$ticker")
                }

                composable(
                    route = ComposeRoutes.CryptoWithdrawalReview.route + "?transaction={transaction}",
                    arguments = listOf(navArgument("transaction") { type = NavType.StringType })
                ) { backStackEntry ->
                    val json = backStackEntry.arguments?.getString("transaction")
                    Log.d("Here", "this is data $json")
                    val decodedJson = Uri.decode(json ?: "")
                    val transaction = if (decodedJson.isNotEmpty()) {
                        Json.decodeFromString<CryptoTransactionSummary>(decodedJson)
                    } else null

                    CryptoWithdrawalReviewRoute(navController = navController, transaction = transaction)
                }

                composable(
                    route = "${ComposeRoutes.RampTransactionDetail.route}/{rampId}",
                    arguments = listOf(
                        navArgument("rampId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val rampId = backStackEntry.arguments?.getString("rampId") ?: ""
                    RampTransactionDetailScreen(navController = navController, rampId = rampId)
                }

                composable(
                    route = "${ComposeRoutes.WebView.route}/{url}",
                    arguments = listOf(navArgument("url") { type = NavType.StringType })
                ) { backStackEntry ->
                    val url = backStackEntry.arguments?.getString("url")
                    if (url != null) WebViewScreen(url = url)
                }

                composable(
                    route = "${ComposeRoutes.AssetDetail.route}/{assetCode}",
                    arguments = listOf(navArgument("assetCode") { type = NavType.StringType })
                ) { backStackEntry ->
                    val assetCode = backStackEntry.arguments?.getString("assetCode")
                    if (assetCode != null) {
                        AssetDetailScreenRoute(navController = navController, assetCode = assetCode)
                    }
                }

                composable(
                    route = "${ComposeRoutes.OnOffRamp.route}/{action}",
                    arguments = listOf(
                        navArgument("action") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val actionParam = backStackEntry.arguments?.getString("action")
                    val action = OnOffRampAction.fromRoute(actionParam)

                    On_Off_RampScreenRoute(
                        navController = navController,
                        action = action
                    )
                }

                composable(
                    route = ComposeRoutes.CryptoTransactionDetail.route + "/{transaction}",
                    arguments = listOf(
                        navArgument("transaction") {
                            type = NavType.StringType
                            nullable = false
                        }
                    )
                ) { backStackEntry ->
                    val encodedJson = backStackEntry.arguments?.getString("transaction")

                    if (encodedJson == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("ERROR: Transaction data missing", color = Color.Red, fontSize = 20.sp)
                        }
                        return@composable
                    }

                    val transaction = try {
                        val decodedJson = Uri.decode(encodedJson)
                        Json.decodeFromString<CryptoTransactionSummary>(decodedJson)
                    } catch (e: Exception) {
                        null
                    }

                    CryptoTransactionDetailRoute(navController = navController, transaction = transaction)
                }
            }
            val hasNavigated = rememberSaveable { mutableStateOf(false) }

            intendedRoute?.let { route ->
                LaunchedEffect(route) {
                    if (!hasNavigated.value) {
                        hasNavigated.value = true

                        Log.d("EEE", "Navigating to intended route: $route")

                        navController.navigate(route) {
                            popUpTo(ComposeRoutes.PaymentLinks.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_INTENDED_ROUTE = "intendedRoute"

        /**
         * Use this for normal navigation (simple routes)
         */
        fun newIntent(
            context: Context,
            startDestination: String = ComposeRoutes.CryptoTransactionDetail.route
        ): Intent {
            return Intent(context, ComposeHostActivity::class.java).apply {
                putExtra(EXTRA_INTENDED_ROUTE, startDestination)
            }
        }

        /**
         * Convenience for Ramp detail (kept for backward compatibility)
         */
        fun newRampTransactionDetailIntent(context: Context, rampId: String): Intent {
            val routeWithParam = "${ComposeRoutes.RampTransactionDetail.route}/$rampId"
            return newIntent(context, routeWithParam)
        }

        /**
         * Recommended way to open CryptoTransactionDetail with full object
         */
        fun newCryptoTransactionDetailIntent(
            context: Context,
            encodedTransactionJson: String
        ): Intent {
            val route = "${ComposeRoutes.CryptoTransactionDetail.route}/$encodedTransactionJson"
            return newIntent(context, route)
        }
    }
}
