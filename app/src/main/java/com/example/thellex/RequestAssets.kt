package com.example.thellex

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController



class RequestAssetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_assets)

        // Find the button for navigating to receiver type and set the click listener
        val receiverTypeButton = findViewById<LinearLayout>(R.id.posLinearLayoutCryptocurrency)
        receiverTypeButton.setOnClickListener {
            startActivity(Intent(this, POSReceiverTypeActivity::class.java))
        }
    }
}