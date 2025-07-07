package com.thellex.payments.features.kyc.ui.basic

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
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
        ActivityTracker.add(this)

        setupViewModel()
        setupEditTexts()
        restoreFormData()
        setupDatePicker()
        setupContinueButton()
    }

    private fun setupViewModel() {
        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]
    }

    private fun setupEditTexts() {
        listOf(
            binding.fragmentKycStep1EtFirstName,
            binding.fragmentKycStep1EtMiddleName,
            binding.fragmentKycStep1EtLastName,
            binding.fragmentKycStep1EtPhoneNumber,
            binding.fragmentKycStep1EtDob
        ).forEach { disableAutoComplete(it) }
    }

    private fun restoreFormData() {
        intent.getStringExtra("FORM_DATA_JSON")?.let { json ->
            val formData = gson.fromJson(json, BasicKycFormModelData::class.java)
            basicKycFormModel.formData.value = formData
            populateFields(formData)
        }
    }

    private fun setupDatePicker() {
        binding.fragmentKycStep1EtDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    val formattedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                    binding.fragmentKycStep1EtDob.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
                show()
            }
        }
    }

    private fun setupContinueButton() {
        binding.fragmentKycStep1BtnContinue.setOnClickListener {
            val firstName = binding.fragmentKycStep1EtFirstName.text.toString().trim()
            val middleName = binding.fragmentKycStep1EtMiddleName.text.toString().trim()
            val lastName = binding.fragmentKycStep1EtLastName.text.toString().trim()
            val phoneNumber = binding.fragmentKycStep1EtPhoneNumber.text.toString().trim()
            val dob = binding.fragmentKycStep1EtDob.text.toString().trim()

            val isValid = validateForm(firstName, lastName, phoneNumber, dob)

            if (isValid) {
                val updated = BasicKycFormModelData(
                    firstName = firstName,
                    middleName = middleName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    dob = dob
                )

                basicKycFormModel.formData.value = updated
                val json = gson.toJson(updated)

                startActivity(Intent(this, BasicKycStep2Activity::class.java).apply {
                    putExtra("FORM_DATA_JSON", json)
                })
            }
        }
    }

    private fun validateForm(
        firstName: String,
        lastName: String,
        phone: String?,
        dob: String
    ): Boolean {
        var isValid = true

        isValid = validateNonEmpty(binding.fragmentKycStep1EtFirstName, firstName, "First Name is required") && isValid
        isValid = validateNonEmpty(binding.fragmentKycStep1EtLastName, lastName, "Last Name is required") && isValid
//        isValid = validatePhoneNumber(binding.fragmentKycStep1EtPhoneNumber, phone) && isValid
        isValid = validateDob(binding.fragmentKycStep1EtDob, dob) && isValid

        return isValid
    }

    private fun disableAutoComplete(editText: EditText) {
        editText.apply {
            inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or inputType
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            isSaveEnabled = false
            imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        }
    }

    private fun validateNonEmpty(editText: EditText, text: String, errorMsg: String): Boolean {
        val isValid = text.isNotEmpty()
        editText.setBackgroundResource(
            if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
        )
        return isValid
    }

    private fun validatePhoneNumber(editText: EditText, phone: String): Boolean {
        val pattern = Regex("^\\+?[0-9]{7,15}\$")
        val isValid = pattern.matches(phone)
        editText.setBackgroundResource(
            if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
        )
        return isValid
    }

    private fun validateDob(editText: EditText, dob: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }

        val isValid = try {
            val date = dateFormat.parse(dob)
            date != null && !date.after(Date())
        } catch (e: Exception) {
            false
        }

        editText.setBackgroundResource(
            if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
        )
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
