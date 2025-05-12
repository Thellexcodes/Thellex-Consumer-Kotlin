package com.example.thellex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController

class POSReceiverType : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_p_o_s_receiver_type, container, false)

        // Find the button for navigating to receiver type and set the click listener
        val receiverTypeButton = view.findViewById<LinearLayout>(R.id.posRequestAssetsLocalCurrencyLayout)
        receiverTypeButton.setOnClickListener {
            println("clicked!!!!")
            findNavController().popBackStack() // Clears the back stack
            findNavController().navigate(R.id.posChooseCryptoFragment)
        }

        return view
    }
}