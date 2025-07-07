package com.thellex.payments.features.kyc.ui.basic

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.databinding.FragmentKycTypeBottomSheetBinding

class KycTypeBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentKycTypeBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKycTypeBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ninOption.setOnClickListener {
            dismiss()
            val intent = Intent(requireContext(), BasicKycRequirements::class.java)
            intent.putExtra("KYC_TYPE", "NIN")
            requireActivity().startActivity(intent)
        }

        binding.passportOption.setOnClickListener {
            dismiss()
            val intent = Intent(requireContext(), PassportActivity::class.java)
            intent.putExtra("KYC_TYPE", "PASSPORT")
            requireActivity().startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}