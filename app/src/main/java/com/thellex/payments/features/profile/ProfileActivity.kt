package com.thellex.payments.features.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.databinding.ActivityProfileBinding
import com.thellex.payments.features.auth.ui.LoginActivity
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory

class ProfileActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: ActivityProfileBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.profileMain.applyAdvancedSystemBarInsets()
        // Initialize data from intent
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.profile_include_top_app_bar),
            title = "Profile"
        )

        initViewModel()
        observeUser()
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

    private fun onLogout() {
        ActivityTracker.remove(this)
        userViewModel.logout()
        //TODO: Prompt user to logout
        ActivityTracker.finishAll()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
