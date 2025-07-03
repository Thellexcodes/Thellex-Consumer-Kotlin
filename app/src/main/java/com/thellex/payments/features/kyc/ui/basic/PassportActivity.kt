package com.thellex.payments.features.kyc.ui.basic

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.payments.R
import com.thellex.payments.databinding.ActivityPassportBinding

class PassportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPassportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val kycType = intent.getStringExtra("KYC_TYPE")
        // Use binding and kycType as needed
    }
}
