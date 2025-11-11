package com.thellex.pay.features.auth.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.thellex.pay.R
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.CustomToast
import com.thellex.pay.core.utils.ErrorHandler
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.applyEmailCharacterFilter
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.data.enums.UserErrorEnum
import com.thellex.pay.databinding.ActivityLoginBinding
import com.thellex.pay.network.services.ApiClient
import com.thellex.pay.data.model.LoginRequestDto
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.profile.ProfileActivity
import com.thellex.pay.network.services.BackendErrorRequestDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userModel: UserViewModel
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        ActivityTracker.finishActivity(ProfileActivity::class.java)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        userModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        binding.tvTermsOfUse.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1rq0UvYgq328aJrvycyWqIK1YcGEMLQftY4dizruQu0I/edit?usp=drivesdk"))
            startActivity(browserIntent)
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1Dm3us_YAKl2LW93Rc464EnuO2ychZDZRdwxFeS8jSDo/edit?usp=drivesdk"))
            startActivity(browserIntent)
        }

        applyEmailCharacterFilter(binding.emailInput)
        setupTabSwitching()
        setupInputValidation()
        setupSubmitAction()
    }

    private fun setupTabSwitching() {
//        binding.tabEmail.setOnClickListener {
//            binding.tabEmail.setBackgroundResource(R.drawable.bg_orange_tab_selected)
//            binding.tabPhone.setBackgroundResource(R.drawable.bg_tab_unselected)
//        }
//
//        binding.tabPhone.setOnClickListener {
//            binding.tabPhone.setBackgroundResource(R.drawable.bg_orange_tab_selected)
//            binding.tabEmail.setBackgroundResource(R.drawable.bg_tab_unselected)
//        }
    }

    private fun setupInputValidation() {
        binding.emailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                val isValid = Helpers.isValidEmail(email)
                val bg = if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
                binding.emailInput.setBackgroundResource(bg)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun setupSubmitAction() {
        binding.nextButton.setOnClickListener {
            if (isSubmitting) return@setOnClickListener

            val email = binding.emailInput.text.toString().trim()

            if (!Helpers.isValidEmail(email)) {
                binding.emailInput.setBackgroundResource(R.drawable.bg_edittext_error)
                return@setOnClickListener
            }

            // Check if the verification code is still valid
            if (userModel.isVerificationCodeValid()) {
                val savedToken = userModel.getToken()
                if (!savedToken.isNullOrEmpty()) {
                    navigateToVerification(savedToken)
                } else {
                    // No token, proceed to login request
                    makeLoginRequest(email)
                }
            } else {
                // expired or no cached verification, proceed normally
                makeLoginRequest(email)
            }
        }
    }

    private fun setSubmitting(submitting: Boolean) {
        isSubmitting = submitting

        binding.nextButton.isEnabled = !submitting

        if (submitting) {
            binding.nextButton.text = "Requesting..."
            binding.nextButton.setBackgroundResource(R.drawable.rounded_border_button_darkblue)
            binding.nextButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            binding.nextButton.text = "Next"
            binding.nextButton.setBackgroundResource(R.drawable.button_ripple_golden_yellow)
            binding.nextButton.setTextColor(ContextCompat.getColor(this, R.color.darkBlue))
        }
    }

    private fun makeLoginRequest(email: String) {
        setSubmitting(true)
        val userRequestData = LoginRequestDto(email = email)

        lifecycleScope.launch {
            try {
                val response = ApiClient.getPublicApi(this@LoginActivity).loginUser(userRequestData)
                if (response.isSuccessful) {
                    val result = response.body()?.result
                    if (result != null) {
                        userModel.saveToken(result.accessToken)
                        userModel.saveExpiresAt(result.expiresAt)
                        navigateToVerification(result.accessToken)
                    } else {
                        throw Exception("Empty result from server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val code = JSONObject(errorBody ?: "").optString("message")
                    val userError = UserErrorEnum.fromCode(code)

                    if (userError == UserErrorEnum.CODE_ALREADY_SENT) {
                        val accessToken = withTimeoutOrNull(5000) {
                            userModel.token.asFlow().first { !it.isNullOrBlank() }
                        }
                        if (!accessToken.isNullOrBlank()) {
                            navigateToVerification(accessToken)
                        } else {
                            ErrorHandler.handle(this@LoginActivity, "Error", userError)
                        }
                    } else {
                        ErrorHandler.handle(this@LoginActivity, "Error", userError)
                    }
                }
            } catch (e: Exception) {
                val userError = UserErrorEnum.fromCode(e.message)
                ErrorHandler.handle(this@LoginActivity, "Error", userError)
                Log.d(TAG, "Error in LoginActivity: ${e.message}, code: ${userError.name}")

                ApiClient.getPublicErrorReportApi(this@LoginActivity).reportError(
                        BackendErrorRequestDto(
                            screen = TAG,
                            errorType = "EXCEPTION",
                            message = e.message ?: "Unknown error",
                            code = userError.name
                        )
                )
            } finally {
                setSubmitting(false)
            }
        }
    }

    private fun navigateToVerification(token: String?) {
        if (!token.isNullOrEmpty()) {
            startActivity(
                Intent(this, AuthVerificationActivity::class.java).apply {
                    putExtra("token", token)
                }
            )
        } else {
            CustomToast.show(
                this,
                title = "Error",
                message = "Authentication token is missing. Please login again."
            )
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}

