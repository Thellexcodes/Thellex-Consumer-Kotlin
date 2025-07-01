package com.thellex.payments.features.wallet.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thellex.payments.databinding.ActivityWithdrawToCryptoWalletBinding

class WithdrawToCryptoWalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWithdrawToCryptoWalletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawToCryptoWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}