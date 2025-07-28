package com.thellex.payments.features.pos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.data.enums.TierEnum
import com.thellex.payments.databinding.FragmentWithdrawalOptionsModalBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory

class WithdrawalOptionsModalFragment : BottomSheetDialogFragment() {

    private lateinit var userViewModel: UserViewModel
    private var listener: WithdrawalOptionsListener? = null

    private var isKycDone: Boolean = false

    private var _binding: FragmentWithdrawalOptionsModalBinding? = null
    private val binding get() = _binding!!

    interface WithdrawalOptionsListener {
        fun onCryptoToFiatOffRamp()
        fun onWithdrawToBank()
        fun onChainWithdraw()
        fun onStartKyc()
    }

    fun setListener(listener: WithdrawalOptionsListener) {
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
        _binding = FragmentWithdrawalOptionsModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.authResult.observe(viewLifecycleOwner) { userDto ->
            isKycDone = userDto?.currentTier?.name != TierEnum.NONE
            val kycFlags = userDto?.transactionSettings

            val isCryptoWithdrawalAllowed = kycFlags?.cryptoWithdrawalAllowed == true
            binding.onChainWithdrawal.isEnabled = isCryptoWithdrawalAllowed
            binding.onChainWithdrawal.alpha = if (isCryptoWithdrawalAllowed) 1f else 0.5f

            val isCryptoToFiatWithdrawalAllowed = kycFlags?.cryptoToFiatWithdrawalAllowed == true
            binding.cryptoToFiat.isEnabled = isCryptoToFiatWithdrawalAllowed
            binding.cryptoToFiat.alpha = if (isCryptoToFiatWithdrawalAllowed) 1f else 0.5f

            val isFiatToFiatWithdrawalAllowed = kycFlags?.fiatToFiatWithdrawalAllowed == true
            binding.cryptoToFiat.isEnabled =isFiatToFiatWithdrawalAllowed
            binding.cryptoToFiat.alpha = if (isFiatToFiatWithdrawalAllowed) 1f else 0.5f

            // Handle On-Chain Deposit KYC badge
            val isOnChainKycRequired = kycFlags?.cryptoWithdrawalRequiresKyc == true
            binding.badgeKycCrypto.visibility = if (!isKycDone && isOnChainKycRequired) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Handle Fiat-to-Crypto KYC badge
            val isFiatToCryptoKycRequired = kycFlags?.cryptoToFiatWithdrawalRequiresKyc == true
            binding.badgeKycFiat.visibility = if (!isKycDone && isFiatToCryptoKycRequired) {
                View.VISIBLE
            } else {
                View.GONE
            }

            val isFiatToFiatKycRequired = kycFlags?.fiatToFiatWithdrawalRequiresKyc == true
            binding.badgeKycBank.visibility = if (!isKycDone && isFiatToFiatKycRequired) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        binding.onChainWithdrawal.setOnClickListener {
            val isOnChainKycRequired = userViewModel.authResult.value?.transactionSettings?.cryptoWithdrawalRequiresKyc == true
            if (!isKycDone && isOnChainKycRequired) {
                listener?.onStartKyc()
            } else {
                listener?.onChainWithdraw()
            }
        }

        binding.cryptoToFiat.setOnClickListener {
            val isCryptoToFiatKycRequired = userViewModel.authResult.value?.transactionSettings?.cryptoToFiatWithdrawalRequiresKyc == true
            if (!isKycDone && isCryptoToFiatKycRequired) {
                listener?.onStartKyc()
            } else {
                listener?.onCryptoToFiatOffRamp()
            }
        }

        binding.fiatWithdraw.setOnClickListener {
            val isCryptoToFiatKycRequired = userViewModel.authResult.value?.transactionSettings?.fiatToFiatWithdrawalRequiresKyc == true
            if (!isKycDone && isCryptoToFiatKycRequired) {
                listener?.onStartKyc()
            } else {
                listener?.onWithdrawToBank()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): WithdrawalOptionsModalFragment {
            return WithdrawalOptionsModalFragment()
        }
    }
}
