package com.thellex.pos.features.wallet.ui

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.pos.R
import com.thellex.pos.features.pos.ui.POSChooseCryptoActivity
import com.thellex.pos.features.pos.ui.RequestAmountActivity
import com.thellex.pos.settings.PaymentType


class RequestAssetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_assets) // Inflate the layout for the activity

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top,
                view.paddingRight,
                systemBarsInsets.bottom
            )

            insets
        }

        // Navigate to Crypto Receiver Type
        val cryptoReceiverButton = findViewById<LinearLayout>(R.id.posLinearLayoutCryptocurrency)
        cryptoReceiverButton.setOnClickListener {
            startActivity(Intent(this, POSChooseCryptoActivity::class.java))
        }

        // Navigate to Local Currency Receiver Type
        val localCurrencyReceiverButton = findViewById<LinearLayout>(R.id.posLinearLayoutLocalCurrency)
        localCurrencyReceiverButton.setOnClickListener {
            val intent = Intent(this, RequestAmountActivity::class.java)
            intent.putExtra("type", PaymentType.REQUEST_FIAT)
            startActivity(intent)
        }
    }
}