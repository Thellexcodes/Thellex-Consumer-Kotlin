package com.thellex.payments.features.pos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.R

class RequestOptionsModalFragment : BottomSheetDialogFragment() {

    interface ReceiveOptionsListener {
        fun onFiatClick()
        fun onCryptoClick()
        fun onBankClick()
        fun onStartKyc()
    }

    private var listener: ReceiveOptionsListener? = null

    fun setListener(listener: ReceiveOptionsListener) {
        this.listener = listener
    }

    private var isKycDone: Boolean = false
    private var tier: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isKycDone = it.getBoolean("isKycDone", false)
            tier = it.getString("tier")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_request_options_modal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnReceiveFromFiat = view.findViewById<ConstraintLayout>(R.id.btnReceiveFromFiat)
        val btnReceiveCrypto = view.findViewById<ConstraintLayout>(R.id.btnReceiveCrypto)
        val btnReceiveFromBank = view.findViewById<ConstraintLayout>(R.id.btnReceiveFromBank)

        val btnContinueFiat = view.findViewById<AppCompatButton>(R.id.btnContinueFiat)
        val btnContinueCrypto = view.findViewById<AppCompatButton>(R.id.btnContinueCrypto)
        val btnContinueBank = view.findViewById<AppCompatButton>(R.id.btnContinueBank)

        val badgeKycFiat = view.findViewById<TextView>(R.id.badgeKycFiat)
        val badgeKycBank = view.findViewById<TextView>(R.id.badgeKycBank)

        if (isKycDone) {
            badgeKycFiat.visibility = View.GONE
            badgeKycBank.visibility = View.GONE
        } else {
            badgeKycFiat.visibility = View.VISIBLE
            badgeKycBank.visibility = View.VISIBLE
        }

        btnReceiveFromFiat.setOnClickListener {
            if (isKycDone) {
                listener?.onFiatClick()
            } else {
                listener?.onStartKyc()
            }
        }

        btnReceiveCrypto.setOnClickListener {
            listener?.onCryptoClick()
        }

        btnReceiveFromBank.setOnClickListener {
            if (isKycDone) {
                listener?.onBankClick()
            } else {
                listener?.onStartKyc()
            }
        }

        // Click listeners for the Continue buttons (same behavior)
        btnContinueFiat.setOnClickListener {
            if (isKycDone) {
                listener?.onFiatClick()
            } else {
                listener?.onStartKyc()
            }
        }

        btnContinueCrypto.setOnClickListener {
            listener?.onCryptoClick()
        }

        btnContinueBank.setOnClickListener {
            if (isKycDone) {
                listener?.onBankClick()
            } else {
                listener?.onStartKyc()
            }
        }
    }

    companion object {
        fun newInstance(isKycDone: Boolean, tier: String?): RequestOptionsModalFragment {
            val fragment = RequestOptionsModalFragment()
            val args = Bundle().apply {
                putBoolean("isKycDone", isKycDone)
                putString("tier", tier)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
