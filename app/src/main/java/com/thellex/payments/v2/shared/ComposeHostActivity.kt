package com.thellex.payments.v2.shared

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thellex.payments.v2.features.payment_links.PaymentLinksScreen

class ComposeHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination = intent.getStringExtra(EXTRA_START_DESTINATION) ?: "screen1"

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = startDestination) {
                composable("screen1") { PaymentLinksScreen(
                    navController,
                    onCreateClick = {

                    }
                ) }
            }
        }
    }

    companion object {
        private const val EXTRA_START_DESTINATION = "startDestination"

        fun newIntent(context: Context, startDestination: String = "screen1"): Intent {
            return Intent(context, ComposeHostActivity::class.java).apply {
                putExtra(EXTRA_START_DESTINATION, startDestination)
            }
        }
    }
}

