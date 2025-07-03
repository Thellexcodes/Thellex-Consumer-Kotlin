package com.thellex.payments.features.kyc.ui.basic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers.highlightCurrency
import com.thellex.payments.data.enums.BasicKycRequirementsEnum
import com.thellex.payments.databinding.ActivityKycOverviewBinding
import com.thellex.payments.features.auth.ui.BasicKycStep1Activity
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.kyc.adapters.KycRequirementsAdapter

class BasicKycRequirements : AppCompatActivity() {

    private lateinit var binding: ActivityKycOverviewBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKycOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupViewModel()
        observeUserLimits()
        setupStartKycButton()
        val kycType = intent.getStringExtra("KYC_TYPE")
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.activityKycOverviewLayoutMain) { v, insets ->
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
    private fun observeUserLimits() {
        userViewModel.authResult.observe(this) { userDto ->
            val nextTier = userDto?.nextTier
            if (nextTier != null) {
                // Format and display daily debit limit with NGN currency
                val dailyDebitLimit = nextTier.transactionLimits.dailyDebitLimit ?: 0
                val formattedLimit = "%,.2f".format(dailyDebitLimit.toDouble())
                val displayText = "Complete your KYC to start transacting up to ₦$formattedLimit"
                val goldColor = ContextCompat.getColor(this, R.color.goldenYellow)
                highlightCurrency(binding.activityKycOverviewTvKycDescription, displayText, goldColor)

                // Filter requirements excluding those in excludedRequirements set
                val allRequirements = nextTier.requirements ?: emptyList()
                val filteredRequirements = allRequirements.filter { it !in excludedRequirements }
                setupRequirementsRecyclerView(filteredRequirements)
            }
        }
    }

    private fun setupRequirementsRecyclerView(requirements: List<String>) {
        binding.rvRequirements.apply {
            layoutManager = LinearLayoutManager(this@BasicKycRequirements)
            adapter = KycRequirementsAdapter(requirements)
        }
    }

    private fun setupStartKycButton() {
        binding.activityKycOverviewLayoutStartKycButton.setOnClickListener {
            startActivity(Intent(this, BasicKycStep1Activity::class.java))
        }
    }

    companion object {
        private val excludedRequirements = setOf(
            BasicKycRequirementsEnum.ID_TYPE.displayName,
            BasicKycRequirementsEnum.ADDITIONAL_ID_TYPE.displayName,
            BasicKycRequirementsEnum.HOUSE_NUMBER.displayName,
            BasicKycRequirementsEnum.STREET_NAME.displayName,
            BasicKycRequirementsEnum.STATE.displayName,
            BasicKycRequirementsEnum.LOCAL_GOVERNMENT_AREA.displayName
        )
    }
}
