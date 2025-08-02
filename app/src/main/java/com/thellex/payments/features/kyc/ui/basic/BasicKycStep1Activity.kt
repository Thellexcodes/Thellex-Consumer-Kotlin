package com.thellex.payments.features.kyc.ui.basic

import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.thellex.payments.R
import com.thellex.payments.core.adapters.CountryPickerDialogFragment
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.getNonSanctionedCountryList
import com.thellex.payments.databinding.FragmentKycStep1Binding
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BasicKycStep1Activity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: FragmentKycStep1Binding
    private lateinit var basicKycFormModel: BasicKycFormViewModel
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentKycStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.basicKycStep1TopAppBar),
            title = "BASIC INFO"
        )

        setupViewModel()
        setupEditTexts()
        setupDefaultCountry()
        restoreFormData()
        setupCountryPicker()
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
            binding.fragmentKycStep1EtPhoneNumber
        ).forEach { disableAutoComplete(it) }

        // Real-time validation for first name
        binding.fragmentKycStep1EtFirstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateNonEmpty(
                    binding.fragmentKycStep1EtFirstName,
                    s.toString().trim(),
                    "First Name is required"
                )
            }
        })

        // Real-time validation for last name
        binding.fragmentKycStep1EtLastName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateNonEmpty(
                    binding.fragmentKycStep1EtLastName,
                    s.toString().trim(),
                    "Last Name is required"
                )
            }
        })

        // Real-time validation for phone number
        setupPhoneNumberInput()

        // Note: DOB validation is handled via DatePickerDialog, so no TextWatcher needed
    }

    private fun setupDefaultCountry() {
        binding.fragmentKycStep1TvCountryCode.text = "+234"
        Glide.with(this)
            .load("https://flagcdn.com/16x12/ng.png")
            .into(binding.fragmentKycStep1IvCountryFlag)
    }

    private fun restoreFormData() {
        intent.getStringExtra("FORM_DATA_JSON")?.let { json ->
            val formData = gson.fromJson(json, BasicKycFormModelData::class.java)
            basicKycFormModel.formData.value = formData
            populateFields(formData)
        }
    }

    private fun setupCountryPicker() {
        binding.fragmentKycStep1CountryPickerContainer.setOnClickListener {
            val dialog = CountryPickerDialogFragment()
            dialog.setOnCountrySelectedListener { country ->
                binding.fragmentKycStep1TvCountryCode.text = country.code
                Glide.with(this)
                    .load(country.flagUrl)
                    .into(binding.fragmentKycStep1IvCountryFlag)
                val phoneInput = binding.fragmentKycStep1EtPhoneNumber.text.toString().trim()
                if (phoneInput.isNotEmpty()) {
                    val (isValid, errorOrFormatted) = formatAndValidatePhoneNumber(phoneInput, country.code)
                    if (!isValid) {
                        Toast.makeText(this, errorOrFormatted, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.show(supportFragmentManager, "CountryPickerDialog")
        }
    }

    private fun setupDatePicker() {
        binding.kycStep1DobInputContainer.setOnClickListener {
            Log.d("KYC", "Date picker clicked")
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog.newInstance(
                { _, year, monthOfYear, dayOfMonth ->
                    val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
                    binding.kycStep1DobText.setText(formattedDate)
                    validateDob(binding.kycStep1DobText, formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                maxDate = Calendar.getInstance()
                accentColor = ContextCompat.getColor(this@BasicKycStep1Activity, R.color.steelBlueGrey)
                setOkText("Confirm")
                setCancelText("Cancel")
                showYearPickerFirst(true)
                setThemeDark(false)
                setTitle("Select Date of Birth")
            }
            datePicker.show(supportFragmentManager, "DatePickerDialog")
        }
    }

    private fun setupContinueButton() {
        binding.fragmentKycStep1BtnContinue.setOnClickListener {
            val firstName = binding.fragmentKycStep1EtFirstName.text.toString().trim()
            val middleName = binding.fragmentKycStep1EtMiddleName.text.toString().trim()
            val lastName = binding.fragmentKycStep1EtLastName.text.toString().trim()
            val phoneInput = binding.fragmentKycStep1EtPhoneNumber.text.toString().trim()
            val countryCode = binding.fragmentKycStep1TvCountryCode.text.toString().trim()
            val dob = binding.kycStep1DobText.text.toString().trim()

            // Validate all fields
            var isValid = true
            isValid = validateNonEmpty(binding.fragmentKycStep1EtFirstName, firstName, "First Name is required") && isValid
            isValid = validateNonEmpty(binding.fragmentKycStep1EtLastName, lastName, "Last Name is required") && isValid
            isValid = validatePhoneNonEmpty(phoneInput) && isValid
            val (phoneValid, formattedPhoneOrError) = formatAndValidatePhoneNumber(phoneInput, countryCode)
            isValid = phoneValid && isValid
            isValid = validateDob(binding.kycStep1DobText, dob) && isValid

            if (!phoneValid && phoneInput.isNotEmpty()) {
                Toast.makeText(this, formattedPhoneOrError, Toast.LENGTH_SHORT).show()
            }

            if (isValid) {
                val updated = BasicKycFormModelData(
                    firstName = firstName,
                    middleName = middleName,
                    lastName = lastName,
                    phoneNumber = formattedPhoneOrError,
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

    private fun setupPhoneNumberInput() {
        binding.fragmentKycStep1EtPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phoneInput = s.toString().trim()
                val countryCode = binding.fragmentKycStep1TvCountryCode.text.toString().trim()
                if (phoneInput.isNotEmpty()) {
                    val (isValid, errorOrFormatted) = formatAndValidatePhoneNumber(phoneInput, countryCode)
                    binding.fragmentKycStep1PhoneContainer.setBackgroundResource(
                        if (isValid || phoneInput.isNotEmpty()) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
                    )
                    if (!isValid && phoneInput.length >= 7) { // Show error only after sufficient input
                        Toast.makeText(this@BasicKycStep1Activity, errorOrFormatted, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.fragmentKycStep1PhoneContainer.setBackgroundResource(R.drawable.bg_edittext_error)
                }
            }
        })
    }

    private fun validatePhoneNonEmpty(phoneInput: String): Boolean {
        val isValid = phoneInput.isNotEmpty()
        binding.fragmentKycStep1PhoneContainer.setBackgroundResource(
            if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
        )
        return isValid
    }

    private fun formatAndValidatePhoneNumber(rawInput: String, countryCode: String): Pair<Boolean, String> {
        return try {
            // Find the country from the countryList based on the countryCode
            val country = getNonSanctionedCountryList().find { it.code == countryCode }
                ?: return Pair(false, "Invalid country code")

            // Clean the raw input: remove spaces and leading zeros
            var cleaned = rawInput.trim().replace("\\s".toRegex(), "")
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1)
            }

            // Validate that the cleaned input contains only digits
            if (!cleaned.matches(Regex("\\d+"))) {
                return Pair(false, "Phone number must contain only digits")
            }

            // Validate phone number length based on country-specific rules
            // Note: phoneLength is assumed to be defined in Country data class
            if (cleaned.length !in 7..15) { // Relaxed range for partial input
                return Pair(false, "Phone number length invalid for ${country.name}")
            }

            // Format the phone number with the country code
            val formattedNumber = "$countryCode$cleaned"

            // Final regex validation for the formatted number
            val isValid = cleaned.length >= 7 && formattedNumber.matches(Regex("^\\+\\d{7,15}$"))

            // Update UI
            runOnUiThread {
                binding.fragmentKycStep1PhoneContainer.setBackgroundResource(
                    if (isValid || cleaned.isNotEmpty()) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
                )
            }

            Pair(isValid, if (isValid) formattedNumber else "Invalid phone number")
        } catch (e: Exception) {
            runOnUiThread {
                binding.fragmentKycStep1PhoneContainer.setBackgroundResource(R.drawable.bg_edittext_error)
            }
            Pair(false, "Error validating phone number")
        }
    }

    private fun validateNonEmpty(editText: EditText, text: String, errorMsg: String): Boolean {
        val isValid = text.isNotEmpty()
        editText.setBackgroundResource(
            if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
        )
        if (!isValid) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
        return isValid
    }

    private fun validateDob(view: TextView, dob: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
            isLenient = false
        }

        val isValid = try {
            val date = dateFormat.parse(dob)
            date != null && !date.after(Date())
        } catch (e: Exception) {
            false
        }

        binding.kycStep1DobInputContainer.setBackgroundResource(
            if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
        )
        if (!isValid && dob.isNotEmpty()) {
            Toast.makeText(this, "Invalid date of birth", Toast.LENGTH_SHORT).show()
        }
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

    private fun populateFields(data: BasicKycFormModelData) {
        binding.fragmentKycStep1EtFirstName.setText(data.firstName)
        binding.fragmentKycStep1EtMiddleName.setText(data.middleName)
        binding.fragmentKycStep1EtLastName.setText(data.lastName)
        data.phoneNumber.let { phone ->
            val countryCode = phone.takeWhile { it != ' ' && it != '-' }.take(4)
            val phoneNumber = phone.substringAfter(countryCode, "")
            binding.fragmentKycStep1TvCountryCode.text = countryCode
            val country = getNonSanctionedCountryList().find { it.code == countryCode }
            Glide.with(this)
                .load(country?.flagUrl)
                .into(binding.fragmentKycStep1IvCountryFlag)
            binding.fragmentKycStep1EtPhoneNumber.setText(phoneNumber)
        }
        binding.kycStep1DobText.text = data.dob.ifEmpty { "" }
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.with(this).clear(binding.fragmentKycStep1IvCountryFlag)
    }
}