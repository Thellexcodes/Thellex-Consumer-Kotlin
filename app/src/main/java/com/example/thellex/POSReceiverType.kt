package com.example.thellex

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController


class POSReceiverTypeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_receiver_type)

        val receiverTypeButton = findViewById<LinearLayout>(R.id.posRequestAssetsLocalCurrencyLayout)
        receiverTypeButton.setOnClickListener {
            startActivity(Intent(this, POSChooseCryptoActivity::class.java))
        }
    }
}