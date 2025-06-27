package com.thellex.payments.features.kyc.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.payments.databinding.ActivityStartKycBinding
import com.thellex.payments.features.kyc.ui.basic.BasicKycOverview

class StartKycActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartKycBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartKycBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.activityStartBtnContinue.setOnClickListener {
            startActivity(Intent(this, BasicKycOverview::class.java))
        }
    }
}
