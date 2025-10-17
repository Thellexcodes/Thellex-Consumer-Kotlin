package com.thellex.pay.features.kyc.ui.basic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.thellex.pay.R
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.highlightCurrency
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.data.enums.BasicKycRequirementsEnum
import com.thellex.pay.databinding.ActivityKycOverviewBinding
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.kyc.adapters.KycRequirementsAdapter

class BasicKycRequirementsActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: ActivityKycOverviewBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKycOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.activityKycOverviewLayoutMain.applyAdvancedSystemBarInsets()

        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.inKycOverviewTopAppBar),
            title = ""
        )

        setupViewModel()
        observeUserLimits()
        setupStartKycButton()
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
            layoutManager = LinearLayoutManager(this@BasicKycRequirementsActivity)
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
