package com.thellex.payments.features.dashboard.ui

import com.thellex.payments.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.thellex.payments.data.enums.ERRORS
import com.thellex.payments.databinding.ActivityMainBinding
import com.thellex.payments.features.onboarding.OnboardingActivity
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.network.services.SocketService
import com.thellex.payments.features.pos.ui.POSHomeActivity
import com.thellex.payments.features.auth.ui.LoginActivity
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userModel: UserViewModel

    // Guard to show error toast only once per failure
    private var hasShownErrorToast = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()
        checkAuthStatus()

        userModel.authResult.observe(this) { authResult ->
            val alertID = authResult?.alertID ?: "default-id"
            startSocketServiceWithAlertId(alertID)
        }
    }

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
                val api = ApiClient.getAuthenticatedApi(token)
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
                    // Prevent multiple toasts for repeated errors
                    if (hasShownErrorToast) return@launch
                    hasShownErrorToast = true

                    val errorBody = response.errorBody()?.string()
                    val errorCode = parseBackendErrorEnum(errorBody)
                    when (val errorEnum = ERRORS.fromCode(errorCode)) {
                        ERRORS.USER_NOT_FOUND -> {
                            navigateToLogin()
                        }
                        ERRORS.USER_SUSPENDED -> {
                            userModel.logout()
                            navigateToLogin()
                        }
                        ERRORS.UNAUTHORIZED -> {
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
                Log.e("TAG", "Error message", e)
                if (!hasShownErrorToast) {
                    hasShownErrorToast = true
                }
                userModel.logout()
                navigateToLogin()
            }
        }
    }

    private fun startSocketServiceWithAlertId(alertID: String) {
        lifecycleScope.launch {
            val serviceIntent = Intent(this@MainActivity, SocketService::class.java).apply {
                putExtra("alertID", alertID)
            }
            ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
        }
    }

    private fun parseBackendErrorEnum(errorBody: String?): String? {
        return try {
            val json = JSONObject(errorBody ?: "{}")
            json.optString("message", null.toString())
        } catch (e: JSONException) {
            null
        }
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    private suspend fun navigateToDashboard() = withContext(Dispatchers.Main) {
        startActivity(Intent(this@MainActivity, POSHomeActivity::class.java))
        finish()
    }

    private suspend fun navigateToLogin() = withContext(Dispatchers.Main) {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }
}
