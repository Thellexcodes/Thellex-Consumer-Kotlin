package com.thellex.payments.features.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers.showSingleToast
import com.thellex.payments.data.model.IdTypeEnum
import com.thellex.payments.databinding.FragmentKycStep2Binding
import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory

class BasicKycStep2Activity : AppCompatActivity() {

    private lateinit var binding: FragmentKycStep2Binding
    private lateinit var basicKycFormModel: BasicKycFormViewModel
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentKycStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)

        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]

        // Restore form data JSON passed from Step 1
        intent.getStringExtra("FORM_DATA_JSON")?.let { json ->
            val formData = gson.fromJson(json, BasicKycFormModelData::class.java)
            basicKycFormModel.formData.value = formData
            populateFields(formData)
        }

        binding.fragmentKycStep2BtnContinue.setOnClickListener {
            val nin = binding.fragmentKycStep2EtNin.text.toString().trim()
            val bvn = binding.fragmentKycStep2EtBvn.text.toString().trim()

            var isValid = true

            if (!validateNonEmpty(binding.fragmentKycStep2EtNin, nin, "Please enter your NIN")) {
                isValid = false
            } else if (!validateDigitsOnly(binding.fragmentKycStep2EtNin, nin, "NIN must contain only numbers")) {
                isValid = false
            }

            if (!validateNonEmpty(binding.fragmentKycStep2EtBvn, bvn, "Please enter your BVN")) {
                isValid = false
            } else if (!validateDigitsOnly(binding.fragmentKycStep2EtBvn, bvn, "BVN must contain only numbers")) {
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            val current = basicKycFormModel.formData.value ?: BasicKycFormModelData()

            val updated = current.copy(
                nin = nin,
                bvn = bvn,
                idType = IdTypeEnum.NIN,
                additionalIdType = IdTypeEnum.BVN
            )

            basicKycFormModel.formData.value = updated

            val json = gson.toJson(updated)
            val intent = Intent(this, BasicKycAttestationActivity::class.java)
            intent.putExtra("FORM_DATA_JSON", json)
            startActivity(intent)
        }
    }

    private fun populateFields(data: BasicKycFormModelData) {
        binding.fragmentKycStep2EtNin.setText(data.nin)
        binding.fragmentKycStep2EtBvn.setText(data.bvn)
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

    private fun validateDigitsOnly(editText: EditText, text: String, errorMsg: String): Boolean {
        return if (!text.all { it.isDigit() }) {
            editText.setBackgroundResource(R.drawable.bg_edittext_error)
            false
        } else {
            editText.setBackgroundResource(R.drawable.rounded_edittext)
            true
        }
    }

}
