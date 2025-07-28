package com.thellex.payments.features.kyc.ui.basic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.TierInfo
import com.thellex.payments.databinding.ActivityKycSuccessBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.kyc.ui.FaceVerificationActivity
import com.thellex.payments.features.kyc.ui.StartKycActivity
import com.thellex.payments.features.pos.ui.POSHomeActivity

class KycSuccessActivity : AppCompatActivity() {

    // --- UI & ViewModel ---
    private lateinit var binding: ActivityKycSuccessBinding
    private lateinit var userViewModel: UserViewModel
    private val gson = Gson()

    // --- Lifecycle ---
    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityTracker.add(this)
        super.onCreate(savedInstanceState)
        setupViewBinding()
        setupViewModel()
        updateUIFromIntent()
        observeUser()
        setupListeners()
        startCheckmarkAnimation()
        closeAllOtherActivities()
    }

    // --- Setup Methods ---
    private fun setupViewBinding() {
        binding = ActivityKycSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.kycSuccessRoot.applyAdvancedSystemBarInsets()
    }

    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIFromIntent() {
        intent.getStringExtra("CURRENT_TIER_JSON")?.let { json ->
            val currentTier = gson.fromJson(json, TierInfo::class.java)
            currentTier?.let { tier -> updateUI(tier) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeUser() {
        userViewModel.authResult.observe(this) { user ->
            user?.currentTier?.let { currentTier ->
                updateUI(currentTier)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(currentTier: TierInfo) {
        val limits = currentTier.transactionLimits
        val withdrawalFee = currentTier.txnFee.feePercentage.toString()
        binding.dailyCreditLimitText.text = "${limits.dailyCreditLimit} NGN"
        binding.dailyDebitLimitText.text = "${limits.dailyDebitLimit} NGN"
        binding.singleDebitLimitText.text = "${limits.singleDebitLimit} NGN"
        binding.feePercentageText.text = withdrawalFee
    }

    private fun setupListeners() {
        binding.proceedToDashboardButton.setOnClickListener {
            startActivity(Intent(this, POSHomeActivity::class.java))
        }

        binding.upgradeLimitsButton.setOnClickListener {
            startActivity(Intent(this, StartKycActivity::class.java))
        }
    }

    private fun startCheckmarkAnimation() {
        Log.d("KycSuccessActivity", "Starting checkmark animation")
        binding.checkmarkView.startAnimation()
    }

    private fun closeAllOtherActivities() {
        ActivityTracker.finishActivity(StartKycActivity::class.java)
        ActivityTracker.finishActivity(BasicKycStep1Activity::class.java)
        ActivityTracker.finishActivity(BasicKycStep2Activity::class.java)
        ActivityTracker.finishActivity(PassportActivity::class.java)
        ActivityTracker.finishActivity(FaceVerificationActivity::class.java)
        ActivityTracker.finishActivity(StartKycActivity::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityTracker.remove(this)
    }
}