package com.thellex.payments.features.dashboard.ui

import android.app.Dialog
import com.thellex.payments.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CrashLogger
import com.thellex.payments.core.utils.ErrorHandler
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.enums.UserErrorEnum
import com.thellex.payments.data.repository.AppVersionRepository
import com.thellex.payments.databinding.ActivityMainBinding
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.features.pos.ui.POSHomeActivity
import com.thellex.payments.features.auth.ui.LoginActivity
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userModel: UserViewModel
    private var hasShownErrorToast = false
    private lateinit var repository: AppVersionRepository

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
        observeVersionState()

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        CrashLogger.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            CrashLogger.sendStoredCrashes(this@MainActivity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        checkAuthStatus()
    }

    private fun checkAppVersionOnStart() {
        lifecycleScope.launch {
            repository.checkAppVersion()
        }
    }

    private fun observeVersionState() {
//        lifecycleScope.launch {
//            repository.versionState.collect { state ->
//                when (state) {
//                    is AppVersionState.ForceUpdate -> {
//                        showForceUpdateDialog(
//                            context = this@MainActivity,
//                            latestVersion = state.latestVersion,
//                            downloadUrl = state.downloadUrl,
//                            updateType = state.updateType
//                        )
//                    }
//                    is AppVersionState.OptionalUpdate -> {
//                        showOptionalUpdateDialog(
//                            context = this@MainActivity,
//                            latestVersion = state.latestVersion,
//                            downloadUrl = state.downloadUrl,
//                            updateType = state.updateType
//                        )
//                    }
//                    is AppVersionState.Error -> {
//                        // Show toast for errors
//                        android.widget.Toast.makeText(
//                            this@MainActivity,
//                            state.message,
//                            android.widget.Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                    AppVersionState.Idle, AppVersionState.UpToDate -> {
//                        // No action needed
//                    }
//                }
//            }
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAuthStatus() {
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

    private suspend fun navigateToDashboard() = withContext(Dispatchers.Main) {
        startActivity(Intent(this@MainActivity, POSHomeActivity::class.java))
        finish()
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

    companion object {
        private val TAG = "TAGY"
    }
}