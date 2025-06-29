package com.thellex.payments.features.auth.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.databinding.FragmentBasicKycReviewBinding
import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory

class BasicKycAttestationActivity : AppCompatActivity() {

    private lateinit var binding: FragmentBasicKycReviewBinding
    private lateinit var basicKycFormModel: BasicKycFormViewModel
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupViewModel()
        restoreFormDataFromIntent()
        observeFormData()
        setupSubmitButton()
    }

    private fun setupBinding() {
        binding = FragmentBasicKycReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupViewModel() {
        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]
    }

    private fun restoreFormDataFromIntent() {
        intent.getStringExtra("FORM_DATA_JSON")?.let { json ->
            val formData = gson.fromJson(json, BasicKycFormModelData::class.java)
            basicKycFormModel.formData.value = formData
        }
    }

    private fun observeFormData() {
        basicKycFormModel.formData.observe(this) { data ->
            with(binding) {
                // Personal information
                firstnameText.text = data.firstName.orEmpty()
                middleNameText.text = data.middleName.orEmpty()
                lastnameText.text = data.lastName.orEmpty()
                dobText.text = data.dob.orEmpty()

                // Contact & Identification
                phoneNumberText.text = data.phoneNumber.orEmpty()
                ninText.text = data.nin.orEmpty()
                bvnText.text = data.bvn.orEmpty()

                // Address
                houseNumberText.text = data.houseNumber.orEmpty()
                streetNameText.text = data.streetName.orEmpty()
                stateText.text = data.state.orEmpty()
                lgaText.text = data.lga.orEmpty()
            }
        }
    }

    private fun setupSubmitButton() {
        binding.submitBtn.setOnClickListener {
            startActivity(Intent(this, KycSuccessActivity::class.java))
            finish()
        }
    }
}


