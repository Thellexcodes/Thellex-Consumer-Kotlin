package com.thellex.payments.features.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.thellex.payments.core.utils.Helpers.showSingleToast
import com.thellex.payments.data.model.BasicKycFormModelDto
import com.thellex.payments.data.model.IdTypeEnum
import com.thellex.payments.databinding.FragmentBasicKycReviewBinding
import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class BasicKycAttestationActivity : AppCompatActivity() {

    private lateinit var binding: FragmentBasicKycReviewBinding
    private lateinit var basicKycFormModel: BasicKycFormViewModel
    private lateinit var userViewModel: UserViewModel
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

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]
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
            // Ensure attestation checkbox is checked
            if (!binding.attestationCheckbox.isChecked) {
                showSingleToast("Please attest that the information provided is correct.")
                return@setOnClickListener
            }

            // Get the form data or abort if null
            val formData = basicKycFormModel.formData.value
            if (formData == null) {
                showSingleToast("Form data is missing. Please complete previous steps.")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val token = withTimeoutOrNull(5000) {
                    userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                }

                if (token.isNullOrBlank()) {
                    return@launch
                }

                val requestDto = BasicKycFormModelDto(
                    idType = IdTypeEnum.NIN.name,
                    additionalIdType = IdTypeEnum.BVN.name,
                    firstName = formData.firstName ?: "",
                    middleName = formData.middleName,
                    lastName = formData.lastName ?: "",
                    phoneNumber = formData.phoneNumber ?: "",
                    dob = formData.dob ?: "",
                    bvn = formData.bvn ?: "",
                    nin = formData.nin ?: "",
                    houseNumber = formData.houseNumber,
                    streetName = formData.streetName,
                    state = formData.state,
                    lga = formData.lga
                )

                try {
                    val api = ApiClient.getAuthenticatedKycApi(token)
                    val response = api.verifyBasic(requestDto)

                    if (response.isSuccessful) {
                        val kycResult = response.body()?.result

                        if (kycResult?.isVerified == true) {
                            val currentUser = userViewModel.authResult.value
                            val updatedUser = currentUser?.copy(
                                currentTier = kycResult.currentTier,
                                nextTier = kycResult.nextTier
                            )
                            userViewModel.saveAuthResult(updatedUser)
                            startActivity(Intent(this@BasicKycAttestationActivity, KycSuccessActivity::class.java))
                            finish()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.d("TAG", "KYC request failed with code: ${response.code()}, errorBody: $errorBody")
                    }
                } catch (e: Exception) {
                    Log.e("TAG", "Exception during KYC verification", e)
                }
            }
        }
    }
}


