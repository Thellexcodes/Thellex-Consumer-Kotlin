package com.thellex.payments.features.fiat.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.ErrorHandler
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.data.enums.UserErrorEnum
import com.thellex.payments.data.model.CreateBankAccountDto
import com.thellex.payments.databinding.BottomSheetAddAccountBinding
import com.thellex.payments.databinding.TopAppBarBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.fiat.CryptoToFiatOffRampActivity
import com.thellex.payments.features.fiat.adapters.NGBankDto
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class AddAccountBottomSheetFragment : BottomSheetDialogFragment() {

    private var selectedBank: NGBankDto? = null
    private var _binding: BottomSheetAddAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel

    companion object {
        private const val BANK_SELECTED_KEY = "bankSelectedKey"
        private const val SELECTED_BANK_NAME = "selectedBankName"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.main.applyAdvancedSystemBarInsets()
        setupBottomSheetBehavior()
        setupTopAppBar()
        setupBankSelector()
        setupFragmentResultListener()
        setupAddAccountButton()

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(requireContext())
        )[UserViewModel::class.java]
    }

    private fun setupBottomSheetBehavior() {
        dialog?.setOnShowListener {
            val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            (dialog as? BottomSheetDialog)?.behavior?.apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }
    }

    private fun setupTopAppBar() {
        val includedBinding = TopAppBarBinding.bind(binding.root)
        Helpers.setupTopAppBar(
            activity = requireActivity(),
            rootView = includedBinding.root,
            title = "ADD A NEW ACCOUNT"
        )
        includedBinding.buttonBack.setOnClickListener { dismiss() }
    }

    private fun setupBankSelector() {
        binding.bankSelectorLayout.setOnClickListener {
            val dialog = BankSearchDialogFragment()
            dialog.show(parentFragmentManager, "BankSearchDialog")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(BANK_SELECTED_KEY, viewLifecycleOwner) { _, bundle ->
            val bank = bundle.getSerializable(SELECTED_BANK_NAME, NGBankDto::class.java)
            bank?.let {
                binding.selectBankLabel.text = it.name
                selectedBank = it
            }
        }
    }

    private fun setupAddAccountButton() {
        binding.addNewAccountButton.setOnClickListener {
            val numberText = binding.textAccountNumberInput.text.toString().trim()
            val accountNumber = numberText.toLongOrNull()

            if (selectedBank == null || accountNumber == null || !validateInputs(selectedBank!!.name, numberText)) {
                CustomToast.show(requireContext(), "Missing Info", "Please fill in all fields correctly.")
                return@setOnClickListener
            }

            binding.addNewAccountButton.setSubmitting(true)

            lifecycleScope.launch {
                try {
                    val authToken = withTimeoutOrNull(5000) {
                        userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                    }

                    if (authToken.isNullOrEmpty()) {
                        CustomToast.show(requireContext(), "Auth Error", "Authorization token is missing.")
                        return@launch
                    }

                    val payload = CreateBankAccountDto(
                        bankName = selectedBank!!.name,
                        accountNumber = accountNumber.toString(),
                        bankCode = selectedBank!!.code
                    )

                    val response = ApiClient.getAuthenticatedPaymentApi(requireContext(), authToken)
                        .addBankAccount(payload)

                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string()
                        Log.e("AddAccount", "API call failed: $errorBody")
                        CustomToast.show(requireContext(), "Failed", "Failed to link your bank account.")
                        return@launch
                    }

                    response.body()?.result?.let { newBankAccount ->
                        userViewModel.addBankAccountToUser(newBankAccount)

                        CustomToast.show(
                            requireContext(),
                            "Bank Account Linked",
                            "Successfully linked. Redirecting in 5 seconds..."
                        )

                        delay(5000)

                        val intent = Intent(requireContext(), CryptoToFiatOffRampActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                        dismiss()
                    }

                } catch (e: Exception) {
                    val userError = UserErrorEnum.fromCode(e.message)
                    ErrorHandler.handle(requireContext(), "Error", userError)
                } finally {
                    binding.addNewAccountButton.setSubmitting(false)
                }
            }
        }
    }

    private fun validateInputs(name: String, number: String): Boolean {
        return name.isNotEmpty() && number.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


