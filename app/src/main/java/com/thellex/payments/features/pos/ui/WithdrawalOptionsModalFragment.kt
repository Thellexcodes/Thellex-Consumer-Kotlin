package com.thellex.payments.features.pos.ui

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
        fun onWithdrawToFiat()
        fun onWithdrawToBank()
        fun onWithdrawToCryptoWallet()
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

        userViewModel.authResult.observe(viewLifecycleOwner) { user ->
            isKycDone = user?.currentTier?.name != TierEnum.NONE

            // Toggle badge visibility
            if (isKycDone) {
                binding.badgeKycFiat.visibility = View.GONE
                binding.badgeKycBank.visibility = View.GONE
            } else {
                binding.badgeKycFiat.visibility = View.VISIBLE
                binding.badgeKycBank.visibility = View.VISIBLE
            }
        }

        binding.fragmentWithdrawalOptionsBankTransfer.setOnClickListener {
            if (isKycDone) listener?.onWithdrawToBank() else listener?.onStartKyc()
        }

        binding.fragmentWithdrawalOptionsCrypto.setOnClickListener{
            listener?.onWithdrawToCryptoWallet()
        }

        binding.fragmentWithdrawalOptionsFiat.setOnClickListener {
            if (isKycDone) listener?.onWithdrawToFiat() else listener?.onStartKyc()
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
