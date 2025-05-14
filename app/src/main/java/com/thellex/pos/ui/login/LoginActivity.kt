package com.thellex.pos.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thellex.pos.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextButton?.setOnClickListener {
            navigateToQuickActions()
        }
    }

    private fun navigateToQuickActions() {
        val intent = Intent(this, LoginPinActivity::class.java)
        startActivity(intent)
        finish()
    }
}