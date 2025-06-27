package com.thellex.payments.features.auth.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.databinding.ActivityKycSuccessBinding


class KycSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKycSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKycSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.proceedToDashboardButton.setOnClickListener{
            closeAllOtherActivities()
            startActivity(Intent())
        }
    }

    private fun closeAllOtherActivities(){
        //TODO: Close all previous activities
        ActivityTracker.finishActivity(BasicKycStep2Activity::class.java)
    }
}
