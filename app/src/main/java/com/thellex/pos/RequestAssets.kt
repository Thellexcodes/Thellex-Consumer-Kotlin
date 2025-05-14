package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity


class RequestAssetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_assets) // Inflate the layout for the activity

        // Find the button for navigating to receiver type and set the click listener
        val receiverTypeButton = findViewById<LinearLayout>(R.id.posLinearLayoutCryptocurrency)
        receiverTypeButton.setOnClickListener {
//            startActivity(Intent(this, POSReceiverTypeActivity::class.java))
            startActivity(Intent(this, POSChooseCryptoActivity::class.java))
        }
    }
}