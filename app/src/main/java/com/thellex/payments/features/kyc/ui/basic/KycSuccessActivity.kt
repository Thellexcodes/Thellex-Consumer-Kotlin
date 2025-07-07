package com.thellex.payments.features.kyc.ui.basic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.core.utils.ActivityTracker
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

    // --- Lifecycle ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewBinding()
        setupViewModel()
        observeUser()
        setupListeners()
        ActivityTracker.add(this)
        closeAllOtherActivities()
    }

    // --- Setup Methods ---
    private fun setupViewBinding() {
        binding = ActivityKycSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
    }

    @SuppressLint("SetTextI18n")
    private fun observeUser() {
        userViewModel.authResult.observe(this) { user ->
            user?.currentTier?.let { currentTier ->
                val limits = currentTier.transactionLimits
                val withdrawalFee = currentTier.txnFee.feePercentage.toString()

                binding.dailyCreditLimitText.text = "${limits.dailyCreditLimit} NGN"
                binding.dailyDebitLimitText.text = "${limits.dailyDebitLimit} NGN"
                binding.singleDebitLimitText.text = "${limits.singleDebitLimit} NGN"
                binding.feePercentageText.text = withdrawalFee
            }
        }
    }

    private fun setupListeners() {
        binding.proceedToDashboardButton.setOnClickListener {
            startActivity(Intent(this, POSHomeActivity::class.java))
        }

        binding.upgradeLimitsButton.setOnClickListener{
            startActivity(Intent(this, StartKycActivity::class.java))
        }
    }

    private fun closeAllOtherActivities() {
        ActivityTracker.finishActivity(StartKycActivity::class.java)
        ActivityTracker.finishActivity(BasicKycStep1Activity::class.java)
        ActivityTracker.finishActivity(BasicKycStep2Activity::class.java)
        ActivityTracker.finishActivity(PassportActivity::class.java)
        ActivityTracker.finishActivity(FaceVerificationActivity::class.java)
        ActivityTracker.finishActivity(StartKycActivity::class.java)
    }
}
