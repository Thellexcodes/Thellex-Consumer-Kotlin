package com.thellex.payments.features.pos.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.data.enums.TierEnum
import com.thellex.payments.databinding.FragmentRequestOptionsModalBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory

class RequestOptionsModalFragment : BottomSheetDialogFragment() {

    private lateinit var userViewModel: UserViewModel
    private var listener: ReceiveOptionsListener? = null

    private var isKycDone: Boolean = false

    private var _binding: FragmentRequestOptionsModalBinding? = null
    private val binding get() = _binding!!

    interface ReceiveOptionsListener {
        fun onFiatClick()
        fun onCryptoClick()
        fun onBankClick()
        fun onStartKyc()
    }

    fun setListener(listener: ReceiveOptionsListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = UserViewModelFactory(requireContext())
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestOptionsModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.authResult.observe(viewLifecycleOwner) { userDto ->
            isKycDone = userDto?.currentTier?.name != TierEnum.NONE

            if (isKycDone) {
                binding.badgeKycNaira.visibility = View.GONE
                binding.badgeKycFiat3.visibility = View.GONE
            } else {
                binding.badgeKycNaira.visibility = View.VISIBLE
                binding.badgeKycFiat3.visibility = View.VISIBLE
            }
        }

        binding.fragmentRequestOptionsReceiveFromFiat.setOnClickListener {
            if (isKycDone) listener?.onBankClick() else listener?.onStartKyc()
        }

        binding.fragmentRequestOptionsOnChainDeposit.setOnClickListener {
            listener?.onCryptoClick()
        }

        binding.fragmentRequestOptionsFiatToCrypto.setOnClickListener {
            if (isKycDone) listener?.onFiatClick() else listener?.onStartKyc()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): RequestOptionsModalFragment {
            return RequestOptionsModalFragment()
        }
    }
}
