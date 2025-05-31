package com.thellex.pos

import UserViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.thellex.pos.databinding.ActivityMainBinding
import com.thellex.pos.services.ApiClient
import com.thellex.pos.ui.login.LoginActivity
import com.thellex.pos.ui.login.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

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
            val token = withTimeoutOrNull(5000) {  // wait max 5 seconds
                viewModel.token.first { !it.isNullOrBlank() }
            }

            if (token == null) {
                navigateToLogin()
                return@launch
            }

            try {
                val api = ApiClient.getAuthenticatedApi(token)
                val response = api.checkAuthStatus()

                if (response.isSuccessful && response.body() != null) {
                    val authResult = response.body()!!.result
                    Log.d("AUTH_DEBUG", "Email: ${authResult.email}, Token: ${authResult.token}")
                    val newToken = response.body()!!.result.token
                    viewModel.saveToken(newToken)

                    if (authResult.isAuthenticated) {
                        navigateToDashboard()
                    } else {
                        navigateToLogin()
                    }
                } else {
                    // Handle error
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Something went wrong when authenticating users",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, Onboarding::class.java)
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