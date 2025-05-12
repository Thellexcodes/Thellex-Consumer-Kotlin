package com.example.thellex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CashOutBottomSheet(): BottomSheetDialogFragment() {
    override fun getTheme(): Int = R.style.Theme_YourApp_BottomSheetDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.modal_cash_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.findViewById<Button>(R.id.cash_button).setOnClickListener {
//            Toast.makeText(requireContext(), "Cash selected", Toast.LENGTH_SHORT).show()
//            dismiss()
//        }
//
//        view.findViewById<Button>(R.id.card_button).setOnClickListener {
//            Toast.makeText(requireContext(), "Card selected", Toast.LENGTH_SHORT).show()
//            dismiss()
//        }
    }
}