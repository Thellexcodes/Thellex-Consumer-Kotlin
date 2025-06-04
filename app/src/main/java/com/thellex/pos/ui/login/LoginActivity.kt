package com.thellex.pos.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.thellex.pos.AuthVerificationActivity
import com.thellex.pos.R
import com.thellex.pos.utils.Helpers
import com.thellex.pos.databinding.ActivityLoginBinding
import com.thellex.pos.services.ApiClient
import com.thellex.pos.data.model.LoginRequestDto
import com.thellex.pos.data.model.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        client = OkHttpClient.Builder()
            .hostnameVerifier { _, _ -> true }
            .sslSocketFactory(Helpers.createUnsafeSslSocketFactory(), Helpers.createUnsafeTrustManager())
            .build()

        val tabEmail: AppCompatButton = findViewById(R.id.tab_email)
        val tabPhone: AppCompatButton = findViewById(R.id.tab_phone)

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

        tabEmail.setOnClickListener {
            tabEmail.setBackgroundResource(R.drawable.bg_orange_tab_selected)
            tabPhone.setBackgroundResource(R.drawable.bg_tab_unselected)
        }

        tabPhone.setOnClickListener {
            tabPhone.setBackgroundResource(R.drawable.bg_orange_tab_selected)
            tabEmail.setBackgroundResource(R.drawable.bg_tab_unselected)
        }

        binding.nextButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                makeLoginRequest(email)
            } else {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun makeLoginRequest(email: String) {
        val userRequestData = LoginRequestDto(email = email)

        lifecycleScope.launch {
            try {
                val response = ApiClient.getPublicApi().loginUser(userRequestData)
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    // Update UI on main thread
                    withContext(Dispatchers.Main) {
                        if (responseBody != null) {
                            navigateToVerification(responseBody)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Error: $errorBody", Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "$errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToVerification(res: LoginResponse) {
        val token = res.result
        if (token.isNotEmpty()) {
//            Toast.makeText(this, "Authentication token: $token", Toast.LENGTH_LONG).show()
            val intent = Intent(this, AuthVerificationActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Authentication token is missing. Cannot proceed.", Toast.LENGTH_LONG).show()
        }
    }

}