package com.thellex.pos

import UserViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.thellex.pos.enums.ERRORS
import com.thellex.pos.utils.Helpers.showLongToast
import com.thellex.pos.databinding.ActivityMainBinding
import com.thellex.pos.services.ApiClient
import com.thellex.pos.ui.login.LoginActivity
import com.thellex.pos.ui.login.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        lifecycleScope.launch {
            navigateToLogin()
            val token = withTimeoutOrNull(5000) {
                viewModel.token.first { !it.isNullOrBlank() }
            }

            Log.e("AuthToken", token ?: "Token is null or timeout reached")

            if (token == null) {
                navigateToLogin()
                return@launch
            }

            try {
                val api = ApiClient.getAuthenticatedApi(token)
                val response = api.checkAuthStatus()

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    val address = authResponse?.result?.qWallet?.wallets?.getOrNull(0)?.address

                    val rawJson = Gson().toJson(authResponse)
                    Log.d("RAW_JSON_BODY", rawJson)
                    Log.d("WALLET_1", address.toString())
                    Log.d("USERDATA", "user: ${authResponse?.result?.email}")

                    val authResult = authResponse?.result
                    if (authResult != null) {
                        viewModel.saveToken(authResult.token)
                        viewModel.saveAuthResult(authResult)

                        if (authResult.isAuthenticated) {
                            // User is authenticated, navigate to dashboard
                            navigateToDashboard()
                        } else {
                            viewModel.logout()
                            navigateToLogin()
                        }
                    } else {
                        viewModel.logout()
                        navigateToLogin()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorCode = parseBackendErrorEnum(errorBody)
                    val errorEnum = ERRORS.fromCode(errorCode)

                    when (errorEnum) {
                        ERRORS.USER_NOT_FOUND -> {
//                            navigateToLogin()
                        }
                        ERRORS.USER_SUSPENDED -> {
                            // You may show a toast message if needed:
                             showLongToast("Your account has been suspended.")
//                            viewModel.logout()
//                            navigateToLogin()
                        }
                        ERRORS.UNAUTHORIZED -> {
                            // Optionally show a message:
                             showLongToast("Unauthorized access. Please log in again.")
//                            viewModel.logout()
//                            navigateToLogin()
                        }
                        else -> {
                            // Optionally handle unknown errors:
                            showLongToast("An unexpected error occurred: $errorEnum")
//                            navigateToLogin()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "Error message", e)
                showLongToast(ERRORS.UNKNOWN_ERROR.message)
//                viewModel.saveToken(null)
//                viewModel.logout()
//                navigateToLogin()
            }
        }
    }

    private fun parseBackendErrorEnum(errorBody: String?): String? {
        return try {
            val json = JSONObject(errorBody ?: "{}")
            json.optString("message", null) // assumes error enum is returned in the `message` field
        } catch (e: JSONException) {
            null
        }
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    private suspend fun navigateToDashboard() = withContext(Dispatchers.Main){
        startActivity(Intent(this@MainActivity, POSActivity::class.java))
        finish()
    }

    private suspend fun navigateToLogin() = withContext(Dispatchers.Main) {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }
}