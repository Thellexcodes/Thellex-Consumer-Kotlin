package com.thellex.payments.features.kyc.ui.basic

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Validator
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

        intent.getStringExtra("FORM_DATA_JSON")?.let { json ->
            val formData = gson.fromJson(json, BasicKycFormModelData::class.java)
            basicKycFormModel.formData.value = formData
            populateFields(formData)
        }

        binding.continueBtn.setOnClickListener{ handleSubmit() }
    }

    private fun handleSubmit() {
        val nin = binding.fragmentKycStep2EtNin.text.toString().trim()
        val bvn = binding.fragmentKycStep2EtBvn.text.toString().trim()

        var isValid = true

        isValid = Validator.validateNonEmpty(binding.fragmentKycStep2EtNin, nin) && isValid
        isValid = Validator.validateDigitsOnly(binding.fragmentKycStep2EtNin, nin) && isValid

        isValid = Validator.validateNonEmpty(binding.fragmentKycStep2EtBvn, bvn) && isValid
        isValid = Validator.validateDigitsOnly(binding.fragmentKycStep2EtBvn, bvn) && isValid

        if (!isValid) return

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

    private fun populateFields(data: BasicKycFormModelData) {
        binding.fragmentKycStep2EtNin.setText(data.nin)
        binding.fragmentKycStep2EtBvn.setText(data.bvn)
    }
}
