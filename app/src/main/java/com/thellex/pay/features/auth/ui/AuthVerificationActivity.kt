package com.thellex.pay.features.auth.ui

import com.thellex.pay.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.otpview.OTPListener
import com.thellex.pay.R
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.ComposeHostActivity
import com.thellex.pay.core.utils.CustomToast
import com.thellex.pay.core.utils.ErrorHandler
import com.thellex.pay.core.utils.FcmHelper
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.data.enums.UserErrorEnum
import com.thellex.pay.network.services.ApiClient
import com.thellex.pay.data.model.VerifyUserDto
import com.thellex.pay.databinding.ActivityAuthVerificationBinding
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.onboarding.NotificationPermissionActivity
import com.thellex.pay.features.pos.ui.POSHomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

class AuthVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthVerificationBinding
    private lateinit var userModel: UserViewModel
    private var token: String? = null
    private var countDownTimer: CountDownTimer? = null
    private var isSubmitting = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.authVerificationMain.applyAdvancedSystemBarInsets()

        userModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        ActivityTracker.add(this)
        token = intent.getStringExtra("token")

        setupOtpListener()
        setupVerifyButton()
        startExpirationTimer()
    }

    private fun setupOtpListener() {
        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {}
            override fun onOTPComplete(otp: String) {
                // Do nothing here – handled via button click now
            }
        }
    }

    private fun setupVerifyButton() {
        binding.submitButton.setOnClickListener {
            if (isSubmitting) return@setOnClickListener

            val otp = binding.otpView.otp.toString().trim()
            val otpInt = otp.toIntOrNull()

            if (otpInt == null) {
                CustomToast.show(this@AuthVerificationActivity, "Warning", "Invalid OTP entered")
                return@setOnClickListener
            }

            verifyOtp(otpInt)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startExpirationTimer() {
        val expiresAtMillis = getExpirationMillis()
        if (expiresAtMillis == null) {
            binding.authVerificationTimer.text = ""
            return
        }

        // Use System.currentTimeMillis() instead of Instant.now()
        val nowMillis = System.currentTimeMillis()
        val millisUntilExpired = expiresAtMillis - nowMillis

        if (millisUntilExpired <= 0) {
            binding.authVerificationTimer.text = "00:00"
            redirectToLogin()
            return
        }

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millisUntilExpired, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                binding.authVerificationTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.authVerificationTimer.text = "00:00"
                redirectToLogin()
            }
        }.start()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    @OptIn(ExperimentalTime::class)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getExpirationMillis(): Long? {
        return try {
            val expiresAtStr = userModel.getExpiresAt() ?: return null
            val instant = Instant.parse(expiresAtStr.toString())
            instant.toEpochMilli()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    private fun setSubmitting(submitting: Boolean) {
        isSubmitting = submitting
        binding.submitButton.isEnabled = !submitting

        if (submitting) {
            binding.submitButton.text = "Verifying..."
            binding.submitButton.setBackgroundResource(R.drawable.button_riple_darkblue)
            binding.submitButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            binding.submitButton.text = "Verify"
            binding.submitButton.setBackgroundResource(R.drawable.button_ripple_golden_yellow)
            binding.submitButton.setTextColor(ContextCompat.getColor(this, R.color.dark_purple))
        }
    }

    private fun verifyOtp(otp: Int) {
        val verifyUserRequestData = VerifyUserDto(code = otp)

        lifecycleScope.launch {
            setSubmitting(true)
            try {
                if (token.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        ErrorHandler.handle(
                            this@AuthVerificationActivity,
                            "Error",
                            UserErrorEnum.TOKEN_MISSING
                        )
                    }
                    return@launch
                }

                val fcmToken = FirebaseMessaging.getInstance().token.await()
                FcmHelper.sendFcmTokenToBackend(this@AuthVerificationActivity, userAuthToken = token!!, fcmToken = fcmToken)
                val api = ApiClient.getAuthenticatedApi(this@AuthVerificationActivity, token!!)
                val response = api.verifyCode(verifyUserRequestData)

                response.body()?.result?.let { result ->
                    withContext(Dispatchers.Main) {
                        userModel.saveAuthResult(result)
                        Log.d(TAG, "this is security settings ${result.security}")
                        if (result?.security?.hasPin == true) {
                            navigateToQuickActions()
                        } else {
                            navigateToPinSettingsScreen()
                        }
                    }
                } ?: run {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    val userError = UserErrorEnum.fromCode(errorBody)
                    withContext(Dispatchers.Main) {
                        ErrorHandler.handle(this@AuthVerificationActivity, "Error", userError)
                    }
                }
            }catch (e: Exception) {
                val userError = UserErrorEnum.fromCode(e.message)
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error occurred during verification", e)
                    ErrorHandler.handle(this@AuthVerificationActivity, "Error", userError)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    setSubmitting(false)
                }
            }
        }
    }

    private fun navigateToPinSettingsScreen() {
        val intent = ComposeHostActivity.newIntent(this, ComposeRoutes.SecuritySettings.route)
        startActivity(intent)
    }

    private fun navigateToQuickActions() {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val hasEnabledNotifications = sharedPrefs.getBoolean("has_enabled_notifications", false)
        if (!hasEnabledNotifications) {
            startActivity(Intent(this, NotificationPermissionActivity::class.java))
        } else {
            startActivity(Intent(this, POSHomeActivity::class.java))
        }
        finish()
    }

    companion object {
        private const val TAG = "AuthVerificationActivity"
    }
}
