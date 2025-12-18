package com.thellex.pay.features.auth.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.shared.PinEntryContent

@SuppressLint("RememberReturnType")
@Composable
fun SecuritySettingsScreen(
    navController: NavHostController? = null,
    onPinSuccess: () -> Unit = {}
) {
    val application = LocalContext.current.applicationContext as Application
    val factory = UserViewModelFactory(application)
    val userViewModel: UserViewModel = viewModel(factory = factory)

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AppGradientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
            ) {
                PinEntryContent(
                    userViewModel = userViewModel,
                    onPinSuccess = onPinSuccess,
                    onError = { errorMessage = it }
                )
            }
        }
    }
}

@Preview(name = "Default", showBackground = false)
@Composable
fun SecuritySettingsScreenPreview() {
    SecuritySettingsScreen()
}