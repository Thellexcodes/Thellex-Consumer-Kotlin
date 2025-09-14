package com.thellex.payments.features.kyc.ui.basic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.ErrorHandler
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.enums.UserErrorEnum
import com.thellex.payments.data.model.BasicKycFormModelDto
import com.thellex.payments.data.model.IdTypeEnum
import com.thellex.payments.databinding.FragmentBasicKycReviewBinding
import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory
import com.thellex.payments.features.auth.viewModel.UserRepository
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class BasicKycAttestationActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: FragmentBasicKycReviewBinding
    private lateinit var basicKycFormModel: BasicKycFormViewModel
    private lateinit var userViewModel: UserViewModel
    private val userRepository by lazy { UserRepository.getInstance(applicationContext) }
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
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()
        ActivityTracker.add(this)

        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.basicKycReviewTopAppBar),
            title = "ATTESTATION"
        )
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
                        // Retrieve token with timeout
                        val token = withTimeoutOrNull(5000) {
                            userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                        }

                        if (token.isNullOrBlank()) {
                            Log.e(TAG, "Authentication failed: Token is null or blank")
                            CustomToast.show(
                                this@BasicKycAttestationActivity,
                                "Authentication Failed",
                                "Unable to authenticate. Please try again."
                            )
                            binding.submitBtn.setSubmitting(false)
                            return@launch
                        }
                        Log.d(TAG, "Retrieved token: $token")

                        // Build KYC request
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
                        Log.d(TAG, "KYC request: idType=${requestDto.idType}, bvn=${requestDto.bvn}, nin=${requestDto.nin}")

                        // Make API call
                        val api = ApiClient.getAuthenticatedKycApi(this@BasicKycAttestationActivity, token)
                        val response = api.verifyBasic(requestDto)

                        // Handle response
                        val kycResult = response.body()?.result
                        Log.d(TAG, "KYC result: isVerified=${kycResult?.isVerified}, nextTier=${kycResult?.nextTier}")

                        if (response.isSuccessful && kycResult?.isVerified == true) {
                            val currentUser = userViewModel.authResult.value
                            if (currentUser == null) {
                                Log.e(TAG, "Current user is null, cannot update KYC tier")
                                CustomToast.show(
                                    this@BasicKycAttestationActivity,
                                    "KYC Error",
                                    "User data not found. Please try again."
                                )
                                return@launch
                            }

                            val updatedUser = currentUser.copy(
                                currentTier = kycResult.nextTier,
                                nextTier = kycResult.nextTier,
                                banks = kycResult.banks
                            )
                            Log.d(TAG, "Updating user with new tier: ${kycResult.nextTier}")

                            // Save updated user
                            try {
                                userRepository.saveAuthResult(updatedUser)
                                Log.d(TAG, "Successfully saved updated user with tier: ${updatedUser.currentTier}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to save updated user: ${e.message}", e)
                                CustomToast.show(
                                    this@BasicKycAttestationActivity,
                                    "KYC Error",
                                    "Failed to save user data. Please try again."
                                )
                                return@launch
                            }

                            // Navigate to success activity
                            val intent = Intent(this@BasicKycAttestationActivity, KycSuccessActivity::class.java)
                            kycResult.nextTier.let { tier ->
                                intent.putExtra("CURRENT_TIER_JSON", gson.toJson(tier))
                            }
                            Log.d(TAG, "Navigating to KycSuccessActivity with tier: ${kycResult.nextTier}")
                            startActivity(intent)
                            finish()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val code = JSONObject(errorBody ?: "{}").optString("message")
                            val userError = UserErrorEnum.fromCode(code)
                            Log.e(TAG, "KYC API failed: code=$code, errorBody=$errorBody, userError=$userError")
                            ErrorHandler.handle(this@BasicKycAttestationActivity, "Verification Error", userError)
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Network error during KYC submission: ${e.message}", e)
                        ErrorHandler.handle(this@BasicKycAttestationActivity, "Network Error", UserErrorEnum.UNKNOWN_ERROR)
                    } catch (e: HttpException) {
                        Log.e(TAG, "HTTP error during KYC submission: code=${e.code()}, message=${e.message()}", e)
                        ErrorHandler.handle(this@BasicKycAttestationActivity, "Server Error", UserErrorEnum.fromCode(e.code().toString()))
                    } catch (e: Exception) {
                        Log.e(TAG, "Unexpected error during KYC submission: ${e.message}", e)
                        ErrorHandler.handle(this@BasicKycAttestationActivity, "Unexpected Error", UserErrorEnum.fromCode(e.message))
                    } finally {
                        Log.d(TAG, "KYC submission completed, resetting submit button")
                        binding.submitBtn.setSubmitting(false)
                    }
            }
        }
    }

    companion object {
        private const val TAG = "BasicKycAttestationActivity"
    }
}

