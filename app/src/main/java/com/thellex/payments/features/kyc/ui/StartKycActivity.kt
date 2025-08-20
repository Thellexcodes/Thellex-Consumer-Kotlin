package com.thellex.payments.features.kyc.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.formatCurrencyWithNGN
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.databinding.ActivityStartKycBinding
import com.thellex.payments.features.kyc.ui.basic.KycSuccessActivity
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.kyc.ui.basic.KycTypeBottomSheetFragment

class StartKycActivity : AppCompatActivity() {
    // ViewBinding
    private lateinit var binding: ActivityStartKycBinding

    // ViewModel
    private lateinit var userViewModel: UserViewModel

    // Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout and set content view
        binding = ActivityStartKycBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ActivityTracker.add(this)
//        ActivityTracker.finishActivity(KycSuccessActivity::class.java)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()
        setupViewModel()
        setupListeners()
        observeUserLimits()
    }

    private fun setupListeners() {
        binding.activityStartBtnContinue.setOnClickListener {
            val modal = KycTypeBottomSheetFragment()
            modal.show(supportFragmentManager, modal.tag)
        }
    }

    // ViewModel Setup
    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    // Data Observers
    @SuppressLint("SetTextI18n")
    private fun observeUserLimits() {
        userViewModel.authResult.observe(this) { userDto ->
            userDto?.nextTier?.let {
                binding.activityStartKycTier1Value.text = it.name.toString()
                binding.activityStartKycTier1Limit.text = "${formatCurrencyWithNGN(it.transactionLimits.dailyDebitLimit)}/DAY"
            }
        }
    }
}

