package com.thellex.payments.features.kyc.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.R
import com.thellex.payments.core.adapters.CountryPickerDialogFragment
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.data.model.PhoneNumberDto
import com.thellex.payments.data.model.SubmitBvnDto
import com.thellex.payments.data.model.getNonSanctionedCountryList
import com.thellex.payments.databinding.FragmentRequestBvnModalBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

class RequestBvnModalFragment : BottomSheetDialogFragment() {

    private lateinit var userViewModel: UserViewModel
    private var _binding: FragmentRequestBvnModalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestBvnModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(requireContext())
        )[UserViewModel::class.java]

        // Setup country picker
        setupCountryPicker()

        // Setup submit button
        binding.btnSubmit.setOnClickListener {
            submitBvnAndPhone()
        }

        // Real-time phone number validation
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phoneInput = s.toString().trim()
                if (phoneInput.isNotEmpty()) {
                    val countryCode = binding.tvCountryCode.text.toString()
                    val (isValid, _) = formatAndValidatePhoneNumber(phoneInput, countryCode)
                    binding.phoneContainer.setBackgroundResource(
                        if (isValid) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
                    )
                } else {
                    binding.phoneContainer.setBackgroundResource(R.drawable.rounded_edittext)
                }
            }
        })

        // Real-time BVN validation (basic: ensure it's numeric and 11 digits)
        binding.etBvn.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val bvn = s.toString().trim()
                binding.etBvn.setBackgroundResource(
                    if (bvn.isNotEmpty() && bvn.matches(Regex("\\d{11}")))
                        R.drawable.rounded_edittext
                    else
                        R.drawable.bg_edittext_error
                )
            }
        })
    }

    private fun setupCountryPicker() {
        binding.countryPickerContainer.setOnClickListener {
            Log.d(TAG, "Country picker clicked")
            val dialog = CountryPickerDialogFragment()
            dialog.setOnCountrySelectedListener { country ->
                Log.d(TAG, "Country selected: ${country.name}, code: ${country.code}")
                binding.tvCountryCode.text = country.code
                Glide.with(this)
                    .load(country.flagUrl)
                    .into(binding.ivCountryFlag)
                val phoneInput = binding.etPhoneNumber.text.toString().trim()
                if (phoneInput.isNotEmpty()) {
                    val (isValid, errorOrFormatted) = formatAndValidatePhoneNumber(phoneInput, country.code)
                    if (!isValid) {
                        Toast.makeText(requireContext(), errorOrFormatted, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.show(childFragmentManager, "CountryPickerDialog")
        }
    }

    private fun formatAndValidatePhoneNumber(rawInput: String, countryCode: String): Pair<Boolean, String> {
        return try {
            // Find the country from the country list based on the countryCode
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
            if (cleaned.length !in 7..15) {
                return Pair(false, "Phone number length invalid for ${country.name}")
            }

            // Format the phone number with the country code
            val formattedNumber = "$countryCode$cleaned"

            // Final regex validation for the formatted number
            val isValid = cleaned.length >= 7 && formattedNumber.matches(Regex("^\\+\\d{7,15}$"))

            // Update UI
            lifecycleScope.launch(Dispatchers.Main) {
                binding.phoneContainer.setBackgroundResource(
                    if (isValid || cleaned.isEmpty()) R.drawable.rounded_edittext else R.drawable.bg_edittext_error
                )
            }

            Pair(isValid, if (isValid) formattedNumber else "Invalid phone number")
        } catch (e: Exception) {
            Log.e(TAG, "Error validating phone number: ${e.message}", e)
            lifecycleScope.launch(Dispatchers.Main) {
                binding.phoneContainer.setBackgroundResource(R.drawable.bg_edittext_error)
            }
            Pair(false, "Error validating phone number")
        }
    }

    private fun submitBvnAndPhone() {
        val bvn = binding.etBvn.text.toString().trim()
        val phone = binding.etPhoneNumber.text.toString().trim()
        val countryCode = binding.tvCountryCode.text.toString()

        Log.d(TAG, "Submit clicked: BVN=$bvn, Phone=$phone, CountryCode=$countryCode")

        // Validate BVN
        if (!bvn.matches(Regex("^\\d{11}$"))) {
            binding.etBvn.setBackgroundResource(R.drawable.bg_edittext_error)
            CustomToast.show(requireContext(), "Error", "Please enter a valid 11-digit BVN")
            return
        }


        // Validate phone number
        val (isValidPhone, errorOrFormatted) = formatAndValidatePhoneNumber(phone, countryCode)
        if (!isValidPhone) {
            binding.phoneContainer.setBackgroundResource(R.drawable.bg_edittext_error)
            CustomToast.show(requireContext(), "Error", errorOrFormatted)
            return
        }

        // Prepare DTO
        val dto = SubmitBvnDto(
            bvn = bvn,
            phoneNumber = PhoneNumberDto(
                phoneCountryCode = countryCode,
                phoneNumber = errorOrFormatted.removePrefix(countryCode)
            )
        )

        lifecycleScope.launch {
            try {
                // Show loading state
                binding.btnSubmit.apply {
                    isEnabled = false
                    setSubmitting(true)
                }

                // Retrieve token with timeout
                val token = withTimeoutOrNull(5000) {
                    userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                } ?: run {
                    Log.e(TAG, "Token retrieval timed out or is empty")
                    withContext(Dispatchers.Main) {
                        CustomToast.show(requireContext(), "Error", "Authentication timed out. Please log in again.")
                        binding.btnSubmit.apply {
                            isEnabled = true
                            setSubmitting(false)
                        }
                    }
                    return@launch
                }

                Log.d(TAG, "Submitting DTO: $dto")
                val api = ApiClient.getAuthenticatedKycApi(requireContext(), token)
                val response = api.submitBvnAndPhone(dto)

                withContext(Dispatchers.Main) {
                    binding.btnSubmit.apply {
                        isEnabled = true
                        setSubmitting(false)
                    }

                    if (response.isSuccessful) {
                        val result = response.body()?.result
                        if (result?.isValid == true) {
                            Log.d(TAG, "BVN submission successful. Result: $result")
                            userViewModel.updateUserWithBvnResult(result)
                            CustomToast.show(requireContext(), "Success", "BVN and Phone verified successfully")
                            dismiss()
                        } else {
                            Log.e(TAG, "Submission successful but result is null")
                            CustomToast.show(requireContext(), "Error", "Verification failed: Empty response from server")
                        }
                    } else {
                        val errorBodyString = response.errorBody()?.string()
                        val errorMessages = try {
                            val jsonArray = JSONObject(errorBodyString ?: "{}").optJSONArray("message")
                            if (jsonArray != null && jsonArray.length() > 0) {
                                (0 until jsonArray.length()).map { jsonArray.getString(it) }.joinToString("; ")
                            } else {
                                JSONObject(errorBodyString ?: "{}").optString("message", "Unknown server error")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse error body: ${e.message}", e)
                            "Failed to parse server error"
                        }
                        Log.e(TAG, "Submission failed: ${response.code()} - $errorMessages")
                        CustomToast.show(requireContext(), "Error", errorMessages)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Submission error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.btnSubmit.apply {
                        isEnabled = true
                        setSubmitting(false)
                    }
                    CustomToast.show(
                        requireContext(),
                        "Error",
                        when (e) {
                            is java.net.SocketTimeoutException -> "Request timed out. Please try again."
                            is java.io.IOException -> "Network error. Please check your connection."
                            else -> "Submission failed: ${e.message ?: "Unknown error"}"
                        }
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "RequestBvnModal"
        @JvmStatic
        fun newInstance() = RequestBvnModalFragment()
    }
}