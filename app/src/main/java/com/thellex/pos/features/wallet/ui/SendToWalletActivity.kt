package com.thellex.pos.features.wallet.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.pos.R
import com.thellex.pos.databinding.ActivityWalletSendtoBinding

class SendToWalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletSendtoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletSendtoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.activityWalletSendtoNextButton.setOnClickListener {
            val intent = Intent(this, EnterAmountActivity::class.java)
            startActivity(intent)
        }
    }
}