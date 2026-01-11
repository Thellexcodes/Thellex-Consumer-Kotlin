package com.thellex.pay.features.onboarding

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.databinding.ActivityNotificationPermissionBinding

class NotificationPermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationPermissionBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setNotificationPermissionFlag(isGranted)
        } else {
            setNotificationPermissionFlag(true)
        }
        goToNextScreen()
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

        binding.btnClose.setOnClickListener {
            setNotificationPermissionFlag(false)
            goToNextScreen()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                    setNotificationPermissionFlag(true)
                    goToNextScreen()
                }
                else -> {
                    requestPermissionLauncher.launch(permission)
                }
            }
        } else {
            setNotificationPermissionFlag(true)
            goToNextScreen()
        }
    }

    private fun setNotificationPermissionFlag(enabled: Boolean) {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("has_enabled_notifications", enabled).apply()
    }

    private fun goToNextScreen() {
//        startActivity(Intent(this, POSHomeActivity::class.java))
        finish()
    }
}