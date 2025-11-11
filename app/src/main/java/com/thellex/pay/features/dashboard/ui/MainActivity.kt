package com.thellex.pay.features.dashboard.ui

import android.app.Dialog
import com.thellex.pay.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.thellex.pay.R
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.AppUpdateHelper
import com.thellex.pay.core.utils.CrashLogger
import com.thellex.pay.core.utils.ErrorHandler
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.data.enums.UserErrorEnum
import com.thellex.pay.data.model.AppVersionState
import com.thellex.pay.data.repository.AppVersionRepository
import com.thellex.pay.databinding.ActivityMainBinding
import com.thellex.pay.features.auth.repository.BiometricRepository
import com.thellex.pay.features.auth.repository.SecurityRepository
import com.thellex.pay.network.services.ApiClient
import com.thellex.pay.features.pos.ui.POSHomeActivity
import com.thellex.pay.features.auth.ui.LoginActivity
import com.thellex.pay.features.auth.ui.SecurityModal
import com.thellex.pay.features.auth.viewModel.RegisterPasskeyViewModel
import com.thellex.pay.features.auth.viewModel.RegisterPasskeyViewModelFactory
import com.thellex.pay.features.auth.viewModel.SecurityViewModel
import com.thellex.pay.features.auth.viewModel.SecurityViewModelFactory
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userModel: UserViewModel
    private lateinit var securityViewModel: SecurityViewModel
    private lateinit var registerPasskeyViewModel: RegisterPasskeyViewModel
    private lateinit var repository: AppVersionRepository

    private var hasShownErrorToast = false
    private var isForceUpdatePending = false
    private var isPinVerified = false
    private var isRedirecting = false // Prevent double navigation

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Thellex)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        repository = AppVersionRepository(this)

        // ViewModels
        userModel = ViewModelProvider(this, UserViewModelFactory(applicationContext))[UserViewModel::class.java]
        val securityRepository = SecurityRepository( applicationContext)
        securityViewModel = ViewModelProvider(this, SecurityViewModelFactory(securityRepository))[SecurityViewModel::class.java]
        val bioRepository = BiometricRepository(this)
        registerPasskeyViewModel = ViewModelProvider(this, RegisterPasskeyViewModelFactory(this, bioRepository))[RegisterPasskeyViewModel::class.java]

        checkAppVersionOnStart()

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        CrashLogger.init(this)
        CoroutineScope(Dispatchers.IO).launch { CrashLogger.sendStoredCrashes(this@MainActivity) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        // Reset PIN state on start
        isPinVerified = false
        isRedirecting = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAppVersionOnStart() {
        lifecycleScope.launch {
            try {
                val state = repository.checkAppVersion()
                when (state) {
                    is AppVersionState.ForceUpdate -> {
                        isForceUpdatePending = true
                        AppUpdateHelper.handleAppUpdate(this@MainActivity, repository)
                    }
                    is AppVersionState.OptionalUpdate -> {
                        AppUpdateHelper.handleAppUpdate(this@MainActivity, repository)
                        checkAuthAndShowPin()
                    }
                    is AppVersionState.Error -> {
                        Log.e(TAG, "Version check failed: ${state.message}")
                        checkAuthAndShowPin()
                    }
                    else -> checkAuthAndShowPin()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check app version", e)
                checkAuthAndShowPin()
            }
        }
    }

    // NEW: Combined auth + PIN check
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAuthAndShowPin() {
        if (isForceUpdatePending || isRedirecting) return

        lifecycleScope.launch {
            val token = withTimeoutOrNull(5000) {
                userModel.token.asFlow().firstOrNull { !it.isNullOrBlank() }
            }

            if (token.isNullOrBlank()) {
                navigateToLogin()
                return@launch
            }

            try {
                val api = ApiClient.getAuthenticatedApi(this@MainActivity, token)
                val response = api.checkAuthStatus()

                if (response.isSuccessful && response.body()?.result != null) {
                    userModel.saveAuthResult(response.body()!!.result)
                    // DO NOT NAVIGATE YET — wait for PIN
                    showPinModalIfNeeded()
                } else {
                    handleAuthFailure()
                }
            } catch (e: Exception) {
                handleAuthFailure()
            }
        }
    }

    private fun handleAuthFailure() {
        if (hasShownErrorToast) return
        hasShownErrorToast = true
        userModel.logout()
        navigateToLogin()
    }

    // NEW: Show PIN modal if user has PIN set
    private fun showPinModalIfNeeded() {
        lifecycleScope.launch {
            val auth = userModel.authResult.asFlow().filterNotNull().firstOrNull()
            if (auth?.security?.hasPin == true) {
                showSecurityModal()
            } else {
                navigateToDashboard()
            }
        }
    }

    // PIN Modal (Compose)
    private fun showSecurityModal() {
        val composeView = ComposeView(this).apply {
            setContent {
                SecurityModal(
                    securityViewModel = securityViewModel,
                    registerPasskeyViewModel = registerPasskeyViewModel,
                    userViewModel = userModel,
                    title = "Enter Your PIN",
                    subtitleSetup = "Set a PIN for secure access.",
                    subtitleConfirm = "Confirm your PIN.",
                    subtitleError = "PINs don’t match. Try again.",
                    subtitleLoading = "Verifying PIN...",
                    onSetupCompleted = {
                        isPinVerified = true
                        (this@apply.parent as? ViewGroup)?.removeView(this@apply)
                        lifecycleScope.launch { navigateToDashboard() }
                    },
                    onDismiss = {
                        (this@apply.parent as? ViewGroup)?.removeView(this@apply)
                        userModel.logout()
                        navigateToLogin()
                    }
                )
            }
        }

        findViewById<ViewGroup>(android.R.id.content).addView(
            composeView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    // NAVIGATION
    private suspend fun navigateToDashboard() = withContext(Dispatchers.Main) {
        if (isRedirecting) return@withContext
        isRedirecting = true
        startActivity(Intent(this@MainActivity, POSHomeActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    // RESUME: Always show PIN if hasPin = true
    override fun onResume() {
        super.onResume()

        // Reset PIN state
        isPinVerified = false

        lifecycleScope.launch {
            AppUpdateHelper.handleAppUpdate(this@MainActivity, repository)
            val auth = userModel.authResult.asFlow().filterNotNull().firstOrNull()
            if (auth?.security?.hasPin == true && !isRedirecting) {
                showSecurityModal()
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}