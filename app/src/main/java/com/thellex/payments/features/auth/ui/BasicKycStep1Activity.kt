package com.thellex.payments.features.auth.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.core.utils.Helpers.showSingleToast
import com.thellex.payments.databinding.FragmentKycStep1Binding
import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import java.util.Calendar

class BasicKycStep1Activity : AppCompatActivity() {

    private lateinit var binding: FragmentKycStep1Binding
    private lateinit var basicKycFormModel: BasicKycFormViewModel
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentKycStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]

        // Restore form data from intent if exists
        intent.getStringExtra("FORM_DATA_JSON")?.let { json ->
            val formData = gson.fromJson(json, BasicKycFormModelData::class.java)
            basicKycFormModel.formData.value = formData
            populateFields(formData)
        }

        binding.fragmentKycStep1EtDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->
                val formattedDate = String.format("%02d/%02d/%04d", d, m + 1, y)
                binding.fragmentKycStep1EtDob.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.fragmentKycStep1BtnContinue.setOnClickListener {
            val firstName = binding.fragmentKycStep1EtFirstName.text.toString().trim()
            val lastName = binding.fragmentKycStep1EtLastName.text.toString().trim()
            val phoneNumber = binding.fragmentKycStep1EtPhoneNumber.text.toString().trim()
            val dob = binding.fragmentKycStep1EtDob.text.toString().trim()
            val middleName = binding.fragmentKycStep1EtMiddleName.text.toString().trim()

            // Step 1 Validations
            if (firstName.isEmpty()) {
                showSingleToast("Please enter your First Name")
                return@setOnClickListener
            }
            if (lastName.isEmpty()) {
                showSingleToast("Please enter your Last Name")
                return@setOnClickListener
            }
            if (phoneNumber.isEmpty()) {
                showSingleToast("Please enter your Phone Number")
                return@setOnClickListener
            }
            if (dob.isEmpty()) {
                showSingleToast("Please enter your Date of Birth")
                return@setOnClickListener
            }

            val updated = BasicKycFormModelData(
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                dob = dob
            )

            basicKycFormModel.formData.value = updated

            val json = gson.toJson(updated)
            val intent = Intent(this, BasicKycStep2Activity::class.java)
            intent.putExtra("FORM_DATA_JSON", json)
            startActivity(intent)
        }
    }

    private fun populateFields(data: BasicKycFormModelData) {
        binding.fragmentKycStep1EtFirstName.setText(data.firstName)
        binding.fragmentKycStep1EtMiddleName.setText(data.middleName)
        binding.fragmentKycStep1EtLastName.setText(data.lastName)
        binding.fragmentKycStep1EtPhoneNumber.setText(data.phoneNumber)
        binding.fragmentKycStep1EtDob.setText(data.dob)
    }
}