package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


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

        // Find the button for navigating to receiver type and set the click listener
        val receiverTypeButton = findViewById<LinearLayout>(R.id.posLinearLayoutCryptocurrency)
        receiverTypeButton.setOnClickListener {
//            startActivity(Intent(this, POSReceiverTypeActivity::class.java))
            startActivity(Intent(this, POSChooseCryptoActivity::class.java))
        }
    }
}