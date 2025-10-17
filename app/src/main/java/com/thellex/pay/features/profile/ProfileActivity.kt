package com.thellex.pay.features.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thellex.pay.R
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.data.enums.RoleEnum
import com.thellex.pay.databinding.ActivityProfileBinding
import com.thellex.pay.features.admin.IncomingRampTransactionsActivity
import com.thellex.pay.features.admin.RevenueActivity
import com.thellex.pay.features.auth.ui.LoginActivity
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory

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
        setupClickListeners()
    }

    private fun initViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    private fun observeUser() {
        userViewModel.authResult.observe(this) { userDto ->
            binding.userRealName.text = userDto?.uid?.toString()?.uppercase() ?: "N/A"
            binding.userEmail.text = userDto?.email ?: ""

            if (userDto?.role == RoleEnum.SUPER_ADMIN) {
                binding.allTransactionsWrapper.visibility = View.VISIBLE
                binding.allRevenueWrapper.visibility = View.VISIBLE
            } else {
                binding.allTransactionsWrapper.visibility = View.GONE
                binding.allTransactionsWrapper.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        // Logout
        binding.logoutWrapper.setOnClickListener {
            onLogout()
        }

        // All Transactions (only visible for SUPER_ADMIN)
        binding.allTransactionsWrapper.setOnClickListener {
            startActivity(Intent(this, IncomingRampTransactionsActivity::class.java))
        }

        binding.allRevenueWrapper.setOnClickListener{
            startActivity(Intent(this, RevenueActivity::class.java))
        }
    }

    private fun onLogout() {
        ActivityTracker.remove(this)
        userViewModel.logout()
        //TODO: Prompt user to logout
        ActivityTracker.finishAll()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    companion object {
        private const val TAG = "ProfileActivity"
    }
}
