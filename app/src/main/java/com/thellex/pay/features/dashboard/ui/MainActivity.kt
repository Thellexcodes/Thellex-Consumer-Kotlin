package com.thellex.pay.features.dashboard.ui

import android.app.Activity
import android.app.Dialog
import com.thellex.pay.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.thellex.pay.R
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.AppUpdateHelper
import com.thellex.pay.core.utils.ComposeHostActivity
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
import com.thellex.pay.network.services.ApiClient
import com.thellex.pay.features.auth.ui.LoginActivity
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userModel: UserViewModel
    private lateinit var repository: AppVersionRepository
    private var hasShownErrorToast = false
    private var isForceUpdatePending = false

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

        userModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        checkAppVersionOnStart()

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        CrashLogger.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            CrashLogger.sendStoredCrashes(this@MainActivity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
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
                        Log.d(TAG, "Optional update available: $state")
                        AppUpdateHelper.handleAppUpdate(this@MainActivity, repository)
                        checkAuthStatus()
                    }
                    is AppVersionState.Error -> {
                        Log.e(TAG, "Version check failed: ${state.message}")
                        checkAuthStatus()
                    }
                    else -> {
                        Log.d(TAG, "App is up to date")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check app version", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAuthStatus() {
        if (isForceUpdatePending) return

        lifecycleScope.launch {
            val token = withTimeoutOrNull(5000) {
                userModel.token.asFlow().first { !it.isNullOrBlank() }
            }

            if (token.isNullOrBlank()) {
                navigateToLogin()
                return@launch
            }

            try {
                val api = ApiClient.getAuthenticatedApi(this@MainActivity, token)
                val response = api.checkAuthStatus()

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    val authResult = authResponse?.result
                    if (authResult != null) {
                        userModel.saveAuthResult(authResult)
                        navigateToDashboard()
                    } else {
                        userModel.logout()
                        navigateToLogin()
                    }
                } else {
                    if (response.code() == 404) {
                        showServerUnavailableDialog()
                        return@launch
                    }

                    if (hasShownErrorToast) return@launch
                    hasShownErrorToast = true

                    val errorBody = response.errorBody()?.string()
                    val errorCode = Helpers.parseBackendErrorEnum(errorBody)
                    val errorEnum = UserErrorEnum.fromCode(errorCode)

                    when (errorEnum) {
                        UserErrorEnum.USER_NOT_FOUND,
                        UserErrorEnum.USER_SUSPENDED,
                        UserErrorEnum.UNAUTHORIZED -> {
                            userModel.logout()
                            navigateToLogin()
                        }
                        else -> {
                            userModel.logout()
                            navigateToLogin()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Error message", e)
                if (!hasShownErrorToast) {
                    hasShownErrorToast = true
                    val errorMessage = Helpers.getErrorMessageFromException(e)
                    val userError = UserErrorEnum.fromCode(errorMessage)
                    ErrorHandler.handle(this@MainActivity, "Error", userError)
                }
                userModel.logout()
                navigateToLogin()
            }
        }
    }

    private val pinScreenLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = ComposeHostActivity.newIntent(
                this@MainActivity,
                ComposeRoutes.Dashboard.route
            )
            startActivity(intent)
            finish()
        }
    }

    private suspend fun navigateToDashboard() = withContext(Dispatchers.Main) {
        val authResult = userModel.authResult.value

        if (authResult == null) {
            navigateToLogin()
            return@withContext
        }

        val intent = ComposeHostActivity.newIntent(
            this@MainActivity,
            ComposeRoutes.SecuritySettings.route
        )

        pinScreenLauncher.launch(intent)
    }

    private suspend fun navigateToLogin() = withContext(Dispatchers.Main) {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showServerUnavailableDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_server_error)
        dialog.setCancelable(false)

        val btnRetry = dialog.findViewById<Button>(R.id.btn_retry)
        btnRetry.setOnClickListener {
            dialog.dismiss()
            checkAuthStatus()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            AppUpdateHelper.handleAppUpdate(this@MainActivity, repository)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}