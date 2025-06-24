package com.thellex.payments.features.wallet.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.R
import com.thellex.payments.features.wallet.ui.SendToWalletActivity

class SendBottomSheetFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.wallet_send_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sendToStellarOption = view.findViewById<View>(R.id.layout_wallet_address)
//        val sendToBankOption = view.findViewById<View>(R.id.sendToBankOption)

        sendToStellarOption.setOnClickListener {
            startActivity(Intent(requireContext(), SendToWalletActivity::class.java))
            dismiss()
        }

//        sendToBankOption.setOnClickListener {
//            startActivity(Intent(requireContext(), SendToBankActivity::class.java))
//            dismiss()
//        }
    }
}

