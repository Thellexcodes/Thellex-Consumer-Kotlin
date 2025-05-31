package com.thellex.pos

import UserViewModel
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.mukeshsolanki.OtpView
import com.thellex.pos.services.ApiClient
import com.thellex.pos.services.VerifyUserDto
import com.thellex.pos.ui.login.LoginPinActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthVerificationActivity : AppCompatActivity() {
    private lateinit var viewModel: UserViewModel

    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_verification)

        viewModel = UserViewModel(applicationContext)

        token = intent.getStringExtra("token")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top,
                view.paddingRight,
                systemBarsInsets.bottom
            )
            insets
        }

        val otpView = findViewById<OtpView>(R.id.otp_view)

        otpView.setOtpCompletionListener { otp ->
            val otpInt = otp.toIntOrNull()
            if (otpInt != null) {
                verifyOtp(otpInt)
            } else {
                Toast.makeText(this, "Invalid OTP entered", Toast.LENGTH_SHORT).show()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun verifyOtp(otp: Int) {
        val verifyUserRequestData = VerifyUserDto(code = otp)

        lifecycleScope.launch {
            try {
                if (token.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AuthVerificationActivity,
                            "Authentication token missing. Please login again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch // Stop further execution
                }

                val api = ApiClient.getAuthenticatedApi(token!!)
                val response = api.verifyCode(verifyUserRequestData)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    println("AuthVerificationActivity Response: $responseBody")
                    withContext(Dispatchers.Main) {
                        if (responseBody != null) {
                            viewModel.saveToken(token!!)
//                            navigateToLoginPin()
                            navigateToQuickActions()
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AuthVerificationActivity, "Error: $errorBody", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AuthVerificationActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToLoginPin() {
        val intent = Intent(this, LoginPinActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToQuickActions(){
        val intent = Intent(this, POSActivity::class.java)
        startActivity(intent)
        finish()
    }
}