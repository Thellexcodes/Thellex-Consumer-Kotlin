package com.thellex.payments.features.kyc.ui.basic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.ErrorHandler
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.data.enums.UserErrorEnum
import com.thellex.payments.data.model.BasicKycFormModelDto
import com.thellex.payments.data.model.IdTypeEnum
import com.thellex.payments.databinding.FragmentBasicKycReviewBinding
import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

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
        ActivityTracker.add(this)
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
                firstnameText.text = data.firstName.orEmpty()
                middleNameText.text = data.middleName.orEmpty()
                lastnameText.text = data.lastName.orEmpty()
                dobText.text = data.dob.orEmpty()
                phoneNumberText.text = data.phoneNumber.orEmpty()
                ninText.text = data.nin.orEmpty()
                bvnText.text = data.bvn.orEmpty()
                houseNumberText.text = data.houseNumber.orEmpty()
                streetNameText.text = data.streetName.orEmpty()
                stateText.text = data.state.orEmpty()
                lgaText.text = data.lga.orEmpty()
            }
        }
    }

    private fun setupSubmitButton() {
        binding.submitBtn.setOnClickListener {
            if (!binding.attestationCheckbox.isChecked) {
                CustomToast.show(
                    this,
                    "Attestation Required",
                    "Please attest that the information provided is correct."
                )
                return@setOnClickListener
            }

            val formData = basicKycFormModel.formData.value
            if (formData == null) {
                CustomToast.show(
                    this,
                    "Incomplete Form",
                    "Form data is missing. Please complete previous steps."
                )
                return@setOnClickListener
            }

            binding.submitBtn.setSubmitting(true)

            lifecycleScope.launch {
                try {
                    val token = withTimeoutOrNull(5000) {
                        userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                    }

                    if (token.isNullOrBlank()) {
                        CustomToast.show(
                            this@BasicKycAttestationActivity,
                            "Authentication Failed",
                            "Unable to authenticate. Please try again."
                        )
                        return@launch
                    }

                    val requestDto = BasicKycFormModelDto(
                        idType = IdTypeEnum.NIN.name,
                        additionalIdType = IdTypeEnum.BVN.name,
                        firstName = formData.firstName,
                        middleName = formData.middleName,
                        lastName = formData.lastName,
                        phoneNumber = formData.phoneNumber,
                        dob = formData.dob,
                        bvn = formData.bvn,
                        nin = formData.nin,
                        houseNumber = formData.houseNumber,
                        streetName = formData.streetName,
                        state = formData.state,
                        lga = formData.lga
                    )

                    val api = ApiClient.getAuthenticatedKycApi(token)
                    val response = api.verifyBasic(requestDto)

                    val kycResult = response.body()?.result
                    if (response.isSuccessful && kycResult?.isVerified == true) {
                        val currentUser = userViewModel.authResult.value
                        val updatedUser = currentUser?.copy(
                            currentTier = kycResult.currentTier,
                            nextTier = kycResult.nextTier
                        )
                        userViewModel.saveAuthResult(updatedUser)
                        startActivity(Intent(this@BasicKycAttestationActivity, KycSuccessActivity::class.java))
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val code = JSONObject(errorBody ?: "").optString("message")
                        val userError = UserErrorEnum.fromCode(code)
                        ErrorHandler.handle(this@BasicKycAttestationActivity, "Verification Error", userError)
                    }
                } catch (e: Exception) {
                    val userError = UserErrorEnum.fromCode(e.message)
                    ErrorHandler.handle(this@BasicKycAttestationActivity, "Unexpected Error", userError)
                } finally {
                    binding.submitBtn.setSubmitting(false)
                }
            }
        }
    }

    companion object {
        private const val TAG = "TAG"
    }
}


