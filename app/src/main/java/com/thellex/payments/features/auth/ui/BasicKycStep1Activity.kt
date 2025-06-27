package com.thellex.payments.features.auth.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.databinding.FragmentKycStep1Binding
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import java.util.Calendar

class BasicKycStep1Activity : AppCompatActivity() {

    private lateinit var binding: FragmentKycStep1Binding
    private lateinit var basicKycFormModel: BasicKycFormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentKycStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]

        val dobEditText = binding.fragmentKycStep1EtDob

        dobEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                dobEditText.setText(formattedDate)
            }, year, month, day)

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
            // Call a method to go to next step — update accordingly
            goToNextStep()
        }
    }

    private fun goToNextStep() {
         startActivity(Intent(this, BasicKycStep2Activity::class.java))
    }
}
