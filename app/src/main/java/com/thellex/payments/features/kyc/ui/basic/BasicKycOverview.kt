package com.thellex.payments.features.kyc.ui.basic

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers.highlightCurrency
import com.thellex.payments.databinding.ActivityKycOverviewBinding

class BasicKycOverview : AppCompatActivity() {

    private lateinit var binding: ActivityKycOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKycOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.activityKycOverviewLayoutMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val text = "Complete your KYC to start transacting up to $1,000,000.00 or even $50,000.00 daily."
        val descriptionTextView = findViewById<TextView>(R.id.activity_kyc_overview_tv_kyc_description)

        val goldColor = ContextCompat.getColor(this, R.color.goldenYellow)
        highlightCurrency(descriptionTextView, text, goldColor)

        binding.activityKycOverviewLayoutStartKycButton.setOnClickListener{
            val intent = Intent(this, BasicKycInfoActivity::class.java)
            startActivity(intent)
        }
    }
}