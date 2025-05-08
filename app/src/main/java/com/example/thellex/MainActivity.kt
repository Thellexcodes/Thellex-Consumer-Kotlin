package com.example.thellex

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.thellex.databinding.ActivityMainBinding

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