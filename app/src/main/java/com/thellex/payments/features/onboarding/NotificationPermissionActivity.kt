package com.thellex.payments.features.onboarding

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.databinding.ActivityNotificationPermissionBinding
import com.thellex.payments.features.auth.ui.LoginActivity
import com.thellex.payments.features.dashboard.ui.MainActivity

class NotificationPermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationPermissionBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            goToNextScreen()
        } else {
            goToNextScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets(fixedHorizontalPaddingDp = 12)

        binding.btnEnableNotifications.setOnClickListener {
            requestNotificationPermission()
        }
        binding.btnClose.setOnClickListener { finish() }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                    goToNextScreen()
                }

                else -> {
                    requestPermissionLauncher.launch(permission)
                }
            }
        } else {
            goToNextScreen()
        }
    }

    private fun goToNextScreen() {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("is_first_launch", false).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}