package com.thellex.payments.features.auth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.databinding.FragmentBasicKycReviewBinding
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory

class BasicKycReviewFragment : Fragment() {

    private var _binding: FragmentBasicKycReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var basicKycFormModel: BasicKycFormViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasicKycReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(requireContext().applicationContext)
        )[BasicKycFormViewModel::class.java]

//        viewModel.formData.observe(viewLifecycleOwner) { data ->
//            binding.summaryTextView.text = buildString {
//                appendLine("First Name: ${data.firstName}")
//                appendLine("Middle Name: ${data.middleName}")
//                appendLine("Last Name: ${data.lastName}")
//                appendLine("Phone Number: ${data.phoneNumber}")
//                appendLine("Date of Birth: ${data.dob}")
//                appendLine("NIN: ${data.nin}")
//                appendLine("BVN: ${data.bvn}")
//                appendLine("House Number: ${data.houseNumber}")
//                appendLine("Street Name: ${data.streetName}")
//                appendLine("State: ${data.state}")
//                appendLine("LGA: ${data.lga}")
//                appendLine("ID Type: ${data.idType}")
//                appendLine("Additional ID Type: ${data.additionalIdType}")
//            }
//        }
//
//        binding.btnSubmit.setOnClickListener {
//            // Submit form or send to backend
//            Toast.makeText(requireContext(), "Form submitted!", Toast.LENGTH_SHORT).show()
//            // (activity as? BasicInfoActivity)?.finish() // or go to confirmation screen
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
