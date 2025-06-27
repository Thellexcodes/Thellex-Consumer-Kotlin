package com.thellex.payments.features.kyc.ui.basic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.thellex.payments.databinding.ActivityBasicInfoBinding
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import com.thellex.payments.features.auth.viewModel.BasicKycPagerAdapter

class BasicKycInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBasicInfoBinding
    private lateinit var kycFormModel: BasicKycFormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBasicInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.activityKycMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        kycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]

        val adapter = BasicKycPagerAdapter(this)
        binding.basicKycViewPager.adapter = adapter
        binding.basicKycViewPager.isUserInputEnabled = false

        binding.basicKycViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateStepIndicator(position)
            }
        })
    }

    fun goToNextStep() {
        val current = binding.basicKycViewPager.currentItem
        if (current < 2) binding.basicKycViewPager.currentItem = current + 1
    }

    fun updateStepIndicator(step: Int) {
        // Update your circle progress bar or dots here
    }
}
