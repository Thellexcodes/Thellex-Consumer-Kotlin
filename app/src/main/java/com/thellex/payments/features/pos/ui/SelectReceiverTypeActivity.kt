package com.thellex.payments.features.pos.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.thellex.payments.R


class SelectReceiverTypeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_receiver_type)

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

        val receiverTypeButton = findViewById<LinearLayout>(R.id.posRequestAssetsLocalCurrencyLayout)
        receiverTypeButton.setOnClickListener {
            startActivity(Intent(this, POSChooseCryptoActivity::class.java))
        }

        val cryptocurrencyLayout = findViewById<LinearLayout>(R.id.posRequestAssetsCryptocurrencyLayout)
        cryptocurrencyLayout.setOnClickListener {
            startActivity(Intent(this, POSChooseCryptoActivity::class.java))
        }

    }
}