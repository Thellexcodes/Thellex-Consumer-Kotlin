package com.thellex.payments.features.kyc.ui.basic

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.IdTypeEnum
import com.thellex.payments.databinding.FragmentKycStep2Binding
import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory

class BasicKycStep2Activity : AppCompatActivity() {

    private lateinit var binding: FragmentKycStep2Binding
    private lateinit var basicKycFormModel: BasicKycFormViewModel
    private lateinit var topBar: Helpers.TopAppBarController
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentKycStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        // Setup top app bar
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.verifyIdentityTopAppBar),
            title = "VERIFY IDENTITY"
        )

        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]

        intent.getStringExtra("FORM_DATA_JSON")?.let { json ->
            val formData = gson.fromJson(json, BasicKycFormModelData::class.java)
            basicKycFormModel.formData.value = formData
            populateFields(formData)
        }

        setupEditTexts()
        binding.continueBtn.setOnClickListener { handleSubmit() }
    }

    private fun setupEditTexts() {
        // Disable autocomplete for all EditText fields
        listOf(
            binding.fragmentKycStep2EtNin,
            binding.fragmentKycStep2EtBvn,
            binding.fragmentKycStep2EtHouseNumber,
            binding.fragmentKycStep2EtStreetName,
            binding.fragmentKycStep2EtState,
            binding.fragmentKycStep2EtLga
        ).forEach { disableAutoComplete(it) }

        // Real-time validation for NIN
        binding.fragmentKycStep2EtNin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val nin = s.toString().trim()
                if (nin.isNotEmpty()) {
                    val (isValid, errorOrFormatted) = formatAndValidateIdNumber(nin, "NIN", 11)
                    if (!isValid) {
//                        Toast.makeText(this@BasicKycStep2Activity, errorOrFormatted, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.fragmentKycStep2EtNin.setBackgroundResource(R.drawable.bg_edittext_error)
                }
            }
        })

        // Real-time validation for BVN
        binding.fragmentKycStep2EtBvn.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val bvn = s.toString().trim()
                if (bvn.isNotEmpty()) {
                    val (isValid, errorOrFormatted) = formatAndValidateIdNumber(bvn, "BVN", 11)
                    if (!isValid) {
//                        Toast.makeText(this@BasicKycStep2Activity, errorOrFormatted, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.fragmentKycStep2EtBvn.setBackgroundResource(R.drawable.bg_edittext_error)
                }
            }
        })

        // Real-time validation for House Number (if visible)
        binding.fragmentKycStep2EtHouseNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (binding.fragmentKycStep2LlHouseNumberGroup.visibility == View.VISIBLE) {
                    val houseNumber = s.toString().trim()
                    if (houseNumber.isNotEmpty()) {
                        val (isValid, errorOrFormatted) = validateNonEmptyText(houseNumber, "House Number")
                        if (!isValid) {
//                            Toast.makeText(this@BasicKycStep2Activity, errorOrFormatted, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.fragmentKycStep2EtHouseNumber.setBackgroundResource(R.drawable.bg_edittext_error)
                    }
                }
            }
        })

        // Real-time validation for Street Name (if visible)
        binding.fragmentKycStep2EtStreetName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (binding.fragmentKycStep2LlStreetNameGroup.visibility == View.VISIBLE) {
                    val streetName = s.toString().trim()
                    if (streetName.isNotEmpty()) {
                        val (isValid, errorOrFormatted) = validateNonEmptyText(streetName, "Street Name")
                        if (!isValid) {
//                            Toast.makeText(this@BasicKycStep2Activity, errorOrFormatted, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.fragmentKycStep2EtStreetName.setBackgroundResource(R.drawable.bg_edittext_error)
                    }
                }
            }
        })

        // Real-time validation for State (if visible)
        binding.fragmentKycStep2EtState.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (binding.fragmentKycStep2LlStateGroup.visibility == View.VISIBLE) {
                    val state = s.toString().trim()
                    if (state.isNotEmpty()) {
                        val (isValid, errorOrFormatted) = validateNonEmptyText(state, "State")
                        if (!isValid) {
//                            Toast.makeText(this@BasicKycStep2Activity, errorOrFormatted, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.fragmentKycStep2EtState.setBackgroundResource(R.drawable.bg_edittext_error)
                    }
                }
            }
        })

        // Real-time validation for LGA (if visible)
        binding.fragmentKycStep2EtLga.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (binding.fragmentKycStep2LlLgaGroup.visibility == View.VISIBLE) {
                    val lga = s.toString().trim()
                    if (lga.isNotEmpty()) {
                        val (isValid, errorOrFormatted) = validateNonEmptyText(lga, "Local Government Area")
                        if (!isValid) {
//                            Toast.makeText(this@BasicKycStep2Activity, errorOrFormatted, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.fragmentKycStep2EtLga.setBackgroundResource(R.drawable.bg_edittext_error)
                    }
                }
            }
        })
    }

    private fun handleSubmit() {
        val nin = binding.fragmentKycStep2EtNin.text.toString().trim()
        val bvn = binding.fragmentKycStep2EtBvn.text.toString().trim()
        val houseNumber = binding.fragmentKycStep2EtHouseNumber.text.toString().trim()
        val streetName = binding.fragmentKycStep2EtStreetName.text.toString().trim()
        val state = binding.fragmentKycStep2EtState.text.toString().trim()
        val lga = binding.fragmentKycStep2EtLga.text.toString().trim()

        var isValid = true

        // Validate NIN
        val (ninValid, ninResult) = formatAndValidateIdNumber(nin, "NIN", 11)
        isValid = ninValid && isValid
        if (!ninValid) {
//            Toast.makeText(this, ninResult, Toast.LENGTH_SHORT).show()
        }

        // Validate BVN
        val (bvnValid, bvnResult) = formatAndValidateIdNumber(bvn, "BVN", 11)
        isValid = bvnValid && isValid
        if (!bvnValid) {
//            Toast.makeText(this, bvnResult, Toast.LENGTH_SHORT).show()
        }

        // Validate address fields if visible
        val houseNumberResult = if (binding.fragmentKycStep2LlHouseNumberGroup.visibility == View.VISIBLE) {
            val (valid, result) = validateNonEmptyText(houseNumber, "House Number")
            isValid = valid && isValid
//            if (!valid) Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            result
        } else ""
        val streetNameResult = if (binding.fragmentKycStep2LlStreetNameGroup.visibility == View.VISIBLE) {
            val (valid, result) = validateNonEmptyText(streetName, "Street Name")
            isValid = valid && isValid
//            if (!valid) Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            result
        } else ""
        val stateResult = if (binding.fragmentKycStep2LlStateGroup.visibility == View.VISIBLE) {
            val (valid, result) = validateNonEmptyText(state, "State")
            isValid = valid && isValid
//            if (!valid) Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            result
        } else ""
        val lgaResult = if (binding.fragmentKycStep2LlLgaGroup.visibility == View.VISIBLE) {
            val (valid, result) = validateNonEmptyText(lga, "Local Government Area")
            isValid = valid && isValid
//            if (!valid) Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            result
        } else ""

        if (!isValid) return

        val current = basicKycFormModel.formData.value ?: BasicKycFormModelData()
        val updated = current.copy(
            nin = ninResult,
            bvn = bvnResult,
            idType = IdTypeEnum.NIN,
            additionalIdType = IdTypeEnum.BVN,
            houseNumber = houseNumberResult,
            streetName = streetNameResult,
            state = stateResult,
            lga = lgaResult
        )

        basicKycFormModel.formData.value = updated

        val json = gson.toJson(updated)
        val intent = Intent(this, BasicKycAttestationActivity::class.java)
        intent.putExtra("FORM_DATA_JSON", json)
        startActivity(intent)
    }

    private fun formatAndValidateIdNumber(input: String, fieldName: String, expectedLength: Int): Pair<Boolean, String> {
        return try {
            // Clean the input: remove spaces
            val cleaned = input.trim().replace("\\s".toRegex(), "")

            // Check if the input is empty
            if (cleaned.isEmpty()) {
                runOnUiThread {
                    val editText = if (fieldName == "NIN") binding.fragmentKycStep2EtNin else binding.fragmentKycStep2EtBvn
                    editText.setBackgroundResource(R.drawable.bg_edittext_error)
                }
                return Pair(false, "$fieldName is required")
            }

            // Validate that the input contains only digits
            if (!cleaned.matches(Regex("\\d+"))) {
                runOnUiThread {
                    val editText = if (fieldName == "NIN") binding.fragmentKycStep2EtNin else binding.fragmentKycStep2EtBvn
                    editText.setBackgroundResource(R.drawable.bg_edittext_error)
                }
                return Pair(false, "$fieldName must contain only digits")
            }

            // Validate length
            if (cleaned.length != expectedLength) {
                runOnUiThread {
                    val editText = if (fieldName == "NIN") binding.fragmentKycStep2EtNin else binding.fragmentKycStep2EtBvn
                    editText.setBackgroundResource(R.drawable.bg_edittext_error)
                }
                return Pair(false, "$fieldName must be $expectedLength digits")
            }

            // Update UI on the main thread
            runOnUiThread {
                val editText = if (fieldName == "NIN") binding.fragmentKycStep2EtNin else binding.fragmentKycStep2EtBvn
                editText.setBackgroundResource(R.drawable.rounded_edittext)
            }

            Pair(true, cleaned)
        } catch (e: Exception) {
            Log.e("KYC", "$fieldName validation error: ${e.message}")
            runOnUiThread {
                val editText = if (fieldName == "NIN") binding.fragmentKycStep2EtNin else binding.fragmentKycStep2EtBvn
                editText.setBackgroundResource(R.drawable.bg_edittext_error)
            }
            Pair(false, "Validation error for $fieldName")
        }
    }

    private fun validateNonEmptyText(input: String, fieldName: String): Pair<Boolean, String> {
        val cleaned = input.trim()
        val isValid = cleaned.isNotEmpty()
        runOnUiThread {
            val editText = when (fieldName) {
                "House Number" -> binding.fragmentKycStep2EtHouseNumber
                "Street Name" -> binding.fragmentKycStep2EtStreetName
                "State" -> binding.fragmentKycStep2EtState
                "Local Government Area" -> binding.fragmentKycStep2EtLga
                else -> null
            }
            editText?.setBackgroundResource(
                if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
            )
        }
        return Pair(isValid, if (isValid) cleaned else "$fieldName is required")
    }

    private fun populateFields(data: BasicKycFormModelData) {
        binding.fragmentKycStep2EtNin.setText(data.nin)
        binding.fragmentKycStep2EtBvn.setText(data.bvn)
        binding.fragmentKycStep2EtHouseNumber.setText(data.houseNumber)
        binding.fragmentKycStep2EtStreetName.setText(data.streetName)
        binding.fragmentKycStep2EtState.setText(data.state)
        binding.fragmentKycStep2EtLga.setText(data.lga)
    }

    private fun disableAutoComplete(editText: EditText) {
        editText.apply {
            inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or inputType
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            isSaveEnabled = false
            imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityTracker.remove(this)
    }
}