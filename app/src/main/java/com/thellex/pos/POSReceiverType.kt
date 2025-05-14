package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity


class POSReceiverTypeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_receiver_type)

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