package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.thellex.pos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val colorHex = Color.parseColor("#0C0D48")
//        window.statusBarColor = colorHex
//        window.navigationBarColor = colorHex

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToOnboarding()
        }, 500)
    }

    private fun navigateToOnboarding() {
//         Navigate to the OnboardingActivity
        val intent = Intent(this, Onboarding::class.java)
        startActivity(intent)
        finish()
    }
}