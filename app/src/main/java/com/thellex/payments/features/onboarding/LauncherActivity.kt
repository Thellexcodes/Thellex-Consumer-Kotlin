package com.thellex.payments.features.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.features.dashboard.ui.MainActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityTracker.add(this)

        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstLaunch = sharedPrefs.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
