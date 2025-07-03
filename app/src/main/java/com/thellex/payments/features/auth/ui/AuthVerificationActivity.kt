package com.thellex.payments.features.auth.ui

import com.thellex.payments.features.auth.viewModel.UserViewModel
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.otpview.OTPListener
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.network.services.ApiClient
import com.thellex.payments.data.model.VerifyUserDto
import com.thellex.payments.databinding.ActivityAuthVerificationBinding
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.pos.ui.POSHomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


class AuthVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthVerificationBinding
    private lateinit var userModel: UserViewModel
    private var token: String? = null
    private var countDownTimer: CountDownTimer? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        ActivityTracker.add(this)

        token = intent.getStringExtra("token")

        setupInsets()
        setupOtpListener()

        startExpirationTimer()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top,
                view.paddingRight,
                systemBarsInsets.bottom
            )
            insets
        }
    }

    private fun setupOtpListener() {
        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
            }
            override fun onOTPComplete(otp: String) {
                val otpInt = otp.toIntOrNull()
                if (otpInt != null) {
                    verifyOtp(otpInt)
                } else {
                    CustomToast.show(this@AuthVerificationActivity,"Warning", "Invalid OTP entered")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startExpirationTimer() {
        val expiresAtMillis = getExpirationMillis()
        if (expiresAtMillis == null) {
            binding.authVerificationTimer.text = ""
            return
        }

        val nowMillis = System.currentTimeMillis()
        val millisUntilExpired = expiresAtMillis - nowMillis

        if (millisUntilExpired <= 0) {
            binding.authVerificationTimer.text = "00:00"
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
            }
        }.start()
    }

    @OptIn(ExperimentalTime::class)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getExpirationMillis(): Long? {
        return try {
            val expiresAtStr = userModel.getExpiresAt() ?: return null

            // Parse ISO string like "2025-07-02T22:31:08.175+00"
            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val offsetDateTime = OffsetDateTime.parse(expiresAtStr.toString(), formatter)

            offsetDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
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
                    return@launch
                }

                val api = ApiClient.getAuthenticatedApi(token!!)
                val response = api.verifyCode(verifyUserRequestData)

                response.body()?.result?.let { result ->
                    withContext(Dispatchers.Main) {
                        userModel.saveAuthResult(result)
                        navigateToQuickActions()
                    }
                } ?: run {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e("TAG", "Error message: $errorBody")
                }

            } catch (e: Exception) {
                Log.e("TAG", "Error message: $e")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AuthVerificationActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToLoginPin() {
        startActivity(Intent(this, LoginPinActivity::class.java))
        finish()
    }

    private fun navigateToQuickActions() {
        startActivity(Intent(this, POSHomeActivity::class.java))
        finish()
    }
}