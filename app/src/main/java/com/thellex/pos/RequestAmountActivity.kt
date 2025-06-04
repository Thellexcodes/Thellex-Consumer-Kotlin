package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.thellex.pos.settings.PaymentType

class RequestAmountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_amount)

        val merchant_withdraw_request_button = findViewById<LinearLayout>(R.id.merchant_withdraw_request_button)
        val blacklistedAssetWarningLayout = findViewById<LinearLayout>(R.id.request_amount_blacklisted_asset_warning)


        val rootView = findViewById<View>(android.R.id.content)
        val statusBarHeight = resources.getIdentifier("status_bar_height", "dimen", "android").let { resId ->
            if (resId > 0) resources.getDimensionPixelSize(resId) else 0
        }
        val navBarHeight = resources.getIdentifier("navigation_bar_height", "dimen", "android").let { resId ->
            if (resId > 0) resources.getDimensionPixelSize(resId) else 0
        }

        // Apply bottom padding equal to navigation bar height
        rootView.setPadding(
            rootView.paddingLeft,
            statusBarHeight,
            rootView.paddingRight,
            navBarHeight
        )

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
            else -> {
                // Default fallback
            }
        }

        val backButton = findViewById<ImageButton>(R.id.merchant_withdraw_back_button)
        backButton.setOnClickListener {
            finish()
        }
    }
}