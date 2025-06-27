package com.thellex.payments.features.auth.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.databinding.FragmentKycStep1Binding
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import com.thellex.payments.features.kyc.ui.basic.BasicKycInfoActivity
import java.util.Calendar

class BasicKycStep1Fragment : Fragment() {

    private var _binding: FragmentKycStep1Binding? = null
    private val binding get() = _binding!!

    private lateinit var basicKycFormModel: BasicKycFormViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKycStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(requireContext().applicationContext)
        )[BasicKycFormViewModel::class.java]

        val dobEditText = binding.fragmentKycStep1EtDob

        dobEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Use requireContext() for Dialog in Fragment
            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                dobEditText.setText(formattedDate)
            }, year, month, day)

            // Optional: restrict date picker to past dates only
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.fragmentKycStep1BtnContinue.setOnClickListener {
            basicKycFormModel.formData.value = basicKycFormModel.formData.value?.copy(
                firstName = binding.fragmentKycStep1EtFirstName.text.toString().trim(),
                middleName = binding.fragmentKycStep1EtMiddleName.text.toString().trim(),
                lastName = binding.fragmentKycStep1EtLastName.text.toString().trim(),
                phoneNumber = binding.fragmentKycStep1EtPhoneNumber.text.toString().trim(),
                dob = binding.fragmentKycStep1EtDob.text.toString().trim()
            )
            (activity as? BasicKycInfoActivity)?.goToNextStep()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
