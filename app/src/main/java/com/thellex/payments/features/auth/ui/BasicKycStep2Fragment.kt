package com.thellex.payments.features.auth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.data.model.IdTypeEnum
import com.thellex.payments.databinding.FragmentKycStep2Binding
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import com.thellex.payments.features.kyc.ui.basic.BasicKycInfoActivity

class BasicKycStep2Fragment : Fragment() {

    private var _binding: FragmentKycStep2Binding? = null
    private val binding get() = _binding!!

    private lateinit var basicKycFormModel: BasicKycFormViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKycStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        basicKycFormModel = ViewModelProvider(
            requireActivity(),  // Shared across fragments
            BasicKycFormViewModelFactory(requireContext().applicationContext)
        )[BasicKycFormViewModel::class.java]

        binding.fragmentKycStep2BtnContinue.setOnClickListener {
            basicKycFormModel.formData.value = basicKycFormModel.formData.value?.copy(
                nin = binding.fragmentKycStep2EtNin.text.toString().trim(),
                bvn = binding.fragmentKycStep2EtBvn.text.toString().trim(),
                houseNumber = binding.fragmentKycStep2EtHouseNumber.text.toString().trim(),
                streetName = binding.fragmentKycStep2EtStreetName.text.toString().trim(),
                state = binding.fragmentKycStep2EtState.text.toString().trim(),
                lga = binding.fragmentKycStep2EtLga.text.toString().trim(),
                idType = IdTypeEnum.NIN,
                additionalIdType = IdTypeEnum.BVN
            )

            (activity as? BasicKycInfoActivity)?.goToNextStep()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
