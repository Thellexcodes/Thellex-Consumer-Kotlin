package com.example.thellex.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.example.thellex.QuickActions
import com.example.thellex.databinding.ActivityLoginBinding

import com.example.thellex.R

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