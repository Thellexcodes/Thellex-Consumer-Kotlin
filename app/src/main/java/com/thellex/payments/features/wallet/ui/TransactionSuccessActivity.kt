package com.thellex.payments.features.wallet.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers.truncateMiddle
import com.thellex.payments.databinding.ActivityTransactionSuccessBinding
import com.thellex.payments.features.pos.ui.POSHomeActivity

class TransactionSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.finishActivity(WithdrawToCryptoWalletActivity::class.java)

        initView()
        handleIntent()
        setupListeners()
    }

    private fun initView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top,
                view.paddingRight,
                systemBarsInsets.bottom
            )
            insets
        }
    }

    private fun handleIntent() {
        val address = intent.getStringExtra(EXTRA_DESTINATION_ADDRESS)
        val amount = intent.getStringExtra(EXTRA_RECIPIENT_AMOUNT)

        if (address == null || amount == null) {
            Log.w("TAG", "Missing transaction details in intent extras")
        }

        binding.tvRecipientAddress.text = address?.truncateMiddle(8, 8) ?: "N/A"
        binding.tvAmountValue.text = amount ?: "0.00"
    }


    private fun setupListeners() {
        binding.buttonDone.setOnClickListener {
            startActivity(Intent(this, POSHomeActivity::class.java))
            ActivityTracker.finishActivity(WithdrawToCryptoWalletActivity::class.java)
            finish()
        }
    }

    companion object {
        private const val EXTRA_DESTINATION_ADDRESS = "destinationAddress"
        private const val EXTRA_RECIPIENT_AMOUNT = "recipientAmount"
    }
}
