package com.thellex.payments.features.pos.fragments

import android.os.Bundle
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
        fun onCryptoToFiatOnRampClick()
        fun onChainDepositClick()
        fun onFiatDepositClick()
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
            val kycFlags = userDto?.transactionSettings

            val isCryptoDepositAllowed = kycFlags?.cryptoDepositAllowed == true
            binding.fragmentRequestOptionsOnChainDeposit.isEnabled = isCryptoDepositAllowed
            binding.fragmentRequestOptionsOnChainDeposit.alpha = if (isCryptoDepositAllowed) 1f else 0.5f

            val isFiatToCryptoDepositAllowed = kycFlags?.fiatToCryptoDepositAllowed == true
            binding.fiatToCrypto.isEnabled = isFiatToCryptoDepositAllowed
            binding.fiatToCrypto.alpha = if (isFiatToCryptoDepositAllowed) 1f else 0.5f

            val isFiatToFiatDepositAllowed = kycFlags?.fiatToFiatDepositAllowed == true
            binding.fiatDeposit.isEnabled = isFiatToFiatDepositAllowed
            binding.fiatDeposit.alpha = if (isFiatToFiatDepositAllowed) 1f else 0.5f

            // Handle On-Chain Deposit KYC badge
            val isOnChainKycRequired = kycFlags?.cryptoDepositRequiresKyc == true
            binding.badgeKycOnChain.visibility = if (!isKycDone && isOnChainKycRequired) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Handle Fiat-to-Crypto KYC badge
            val isFiatToCryptoKycRequired = kycFlags?.fiatToCryptoDepositRequiresKyc == true
            binding.badgeKycConvertFiat.visibility = if (!isKycDone && isFiatToCryptoKycRequired) {
                View.VISIBLE
            } else {
                View.GONE
            }

            val isFiatToFiatKycRequired = kycFlags?.fiatToFiatDepositRequiresKyc == true
            binding.badgeKycFiat.visibility = if (!isKycDone && isFiatToFiatKycRequired) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        binding.fragmentRequestOptionsOnChainDeposit.setOnClickListener {
            val isOnChainKycRequired = userViewModel.authResult.value?.transactionSettings?.cryptoDepositRequiresKyc == true
            if (!isKycDone && isOnChainKycRequired) {
                listener?.onStartKyc()
            } else {
                listener?.onChainDepositClick()
            }
        }

        binding.fiatToCrypto.setOnClickListener {
            val isFiatToCryptoKycRequired = userViewModel.authResult.value?.transactionSettings?.fiatToCryptoDepositRequiresKyc == true
            if (!isKycDone && isFiatToCryptoKycRequired) {
                listener?.onStartKyc()
            } else {
                listener?.onCryptoToFiatOnRampClick()
            }
        }

        binding.fiatDeposit.setOnClickListener {
            val isFiatToCryptoKycRequired = userViewModel.authResult.value?.transactionSettings?.fiatToFiatDepositRequiresKyc == true
            if (!isKycDone && isFiatToCryptoKycRequired) {
                listener?.onStartKyc()
            } else {
                listener?.onFiatDepositClick()
            }
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
