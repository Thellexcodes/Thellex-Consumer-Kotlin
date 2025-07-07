package com.thellex.payments.features.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.databinding.ActivityProfileBinding
import com.thellex.payments.features.auth.ui.LoginActivity
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleWindowInsets()
        initViewModel()
        setupUI()
        observeUser()
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    private fun observeUser() {
        userViewModel.authResult.observe(this) { userDto ->
            val upperUid = userDto?.uid?.toString()?.uppercase() ?: "N/A"
            val userEmail = userDto?.email
            binding.userRealName.text = upperUid
            binding.userEmail.text = userEmail
        }
    }

    private fun setupUI() {
        binding.logout.setOnClickListener {
            onLogout()
        }
    }

    private fun onLogout() {
        userViewModel.logout()
         finish()
        //TODO: Prompt user to logout
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
