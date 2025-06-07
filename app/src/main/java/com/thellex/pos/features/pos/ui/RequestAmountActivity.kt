package com.thellex.pos.features.pos.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.thellex.pos.features.fiat.DynamicFiatAccountActivity
import com.thellex.pos.R
import com.thellex.pos.settings.PaymentType

class RequestAmountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_amount)

        val merchant_withdraw_request_button = findViewById<LinearLayout>(R.id.merchant_withdraw_request_button)
        val blacklistedAssetWarningLayout = findViewById<LinearLayout>(R.id.request_amount_blacklisted_asset_warning)

        val type = intent.getSerializableExtra("type") as? PaymentType

        when (type) {
            PaymentType.WITHDRAW_FIAT -> {
                merchant_withdraw_request_button.setOnClickListener{
                    startActivity(Intent(this, SelectPaymentMethodActivity::class.java))
                }
            }
            PaymentType.REQUEST_FIAT-> {
                blacklistedAssetWarningLayout.visibility = View.GONE
                merchant_withdraw_request_button.setOnClickListener{
                    startActivity(Intent(this, DynamicFiatAccountActivity::class.java))
                }
            }
            else -> { }
        }

        val backButton = findViewById<ImageButton>(R.id.merchant_withdraw_back_button)
        backButton.setOnClickListener {
            finish()
        }
    }
}