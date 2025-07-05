package com.thellex.payments.features.auth.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers.showSingleToast
import com.thellex.payments.databinding.FragmentKycStep1Binding
import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

        // Disable autocomplete, autocorrect on all EditTexts using binding
        disableAutoComplete(binding.fragmentKycStep1EtFirstName)
        disableAutoComplete(binding.fragmentKycStep1EtMiddleName)
        disableAutoComplete(binding.fragmentKycStep1EtLastName)
        disableAutoComplete(binding.fragmentKycStep1EtPhoneNumber)
        disableAutoComplete(binding.fragmentKycStep1EtDob)

        // Restore form data if exists
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
                val formattedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                binding.fragmentKycStep1EtDob.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.fragmentKycStep1BtnContinue.setOnClickListener {
            val firstName = binding.fragmentKycStep1EtFirstName.text.toString().trim()
            val middleName = binding.fragmentKycStep1EtMiddleName.text.toString().trim()
            val lastName = binding.fragmentKycStep1EtLastName.text.toString().trim()
            val phoneNumber = binding.fragmentKycStep1EtPhoneNumber.text.toString().trim()
            val dob = binding.fragmentKycStep1EtDob.text.toString().trim()

            // Validate inputs and apply backgrounds
            var isValid = true

            isValid = validateNonEmpty(
                binding.fragmentKycStep1EtFirstName,
                firstName,
                "First Name is required"
            ) && isValid

            // Middle name optional - no validation

            isValid = validateNonEmpty(
                binding.fragmentKycStep1EtLastName,
                lastName,
                "Last Name is required"
            ) && isValid

            isValid = validatePhoneNumber(
                binding.fragmentKycStep1EtPhoneNumber,
                phoneNumber
            ) && isValid

            isValid = validateDob(
                binding.fragmentKycStep1EtDob,
                dob
            ) && isValid

            if (!isValid) return@setOnClickListener

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

    private fun disableAutoComplete(editText: EditText) {
        editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or editText.inputType
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        editText.isSaveEnabled = false
        editText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
    }

    private fun validateNonEmpty(editText: EditText, text: String, errorMsg: String): Boolean {
        return if (text.isEmpty()) {
            editText.setBackgroundResource(R.drawable.bg_edittext_error)
            false
        } else {
            editText.setBackgroundResource(R.drawable.rounded_edittext)
            true
        }
    }

    private fun validatePhoneNumber(editText: EditText, phone: String): Boolean {
        val phonePattern = Regex("^\\+?[0-9]{7,15}\$")
        val isValid = phonePattern.matches(phone)

        if (!isValid) {
            editText.setBackgroundResource(R.drawable.bg_edittext_error)
        } else {
            editText.setBackgroundResource(R.drawable.rounded_edittext)
        }
        return isValid
    }

    private fun validateDob(editText: EditText, dob: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.isLenient = false

        val isValid = try {
            val date = dateFormat.parse(dob) ?: return false
            !date.after(Date())
        } catch (e: Exception) {
            false
        }

        if (!isValid) {
            editText.setBackgroundResource(R.drawable.bg_edittext_error)
        } else {
            editText.setBackgroundResource(R.drawable.rounded_edittext)
        }
        return isValid
    }

    private fun validateNumberField(editText: EditText, number: String, fieldName: String): Boolean {
        val isValid = number.all { it.isDigit() }

        if (!isValid) {
            editText.setBackgroundResource(R.drawable.bg_edittext_error)
        } else {
            editText.setBackgroundResource(R.drawable.rounded_edittext)
        }
        return isValid
    }

    private fun populateFields(data: BasicKycFormModelData) {
        binding.fragmentKycStep1EtFirstName.setText(data.firstName)
        binding.fragmentKycStep1EtMiddleName.setText(data.middleName)
        binding.fragmentKycStep1EtLastName.setText(data.lastName)
        binding.fragmentKycStep1EtPhoneNumber.setText(data.phoneNumber)
        binding.fragmentKycStep1EtDob.setText(data.dob)
    }
}
