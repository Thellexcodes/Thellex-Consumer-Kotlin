package com.thellex.payments.features.fiat.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.R
import com.thellex.payments.databinding.FragmentBankSearchDialogBinding
import com.thellex.payments.features.fiat.adapters.Bank
import com.thellex.payments.features.fiat.adapters.BankSearchAdapter

class BankSearchDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBankSearchDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var bankSearchAdapter: BankSearchAdapter

    class BankSearchDialogFragment : BottomSheetDialogFragment() {

        private var _binding: FragmentBankSearchDialogBinding? = null
        private val binding get() = _binding!!

        private lateinit var bankSearchAdapter: BankSearchAdapter

        companion object {
            const val BANK_SELECTED_KEY = "bankSelectedKey"
            const val SELECTED_BANK_NAME = "selectedBankName"
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            _binding = FragmentBankSearchDialogBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            bankSearchAdapter = BankSearchAdapter(banks) { selectedBank ->
                val bundle = Bundle().apply {
                    putString(SELECTED_BANK_NAME, selectedBank.name)
                }
                parentFragmentManager.setFragmentResult(BANK_SELECTED_KEY, bundle)
                dismiss()
            }

            binding.bankRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = bankSearchAdapter
            }

            binding.searchBar.addTextChangedListener { text ->
                bankSearchAdapter.filter(text.toString())
            }
        }

        override fun onStart() {
            super.onStart()
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isDraggable = true
                }
            }
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        override fun getTheme() = R.style.FullScreenBottomSheetDialogTheme

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }

    companion object {
        const val BANK_SELECTED_KEY = "bankSelectedKey"
        const val SELECTED_BANK_NAME = "selectedBankName"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBankSearchDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bankSearchAdapter = BankSearchAdapter(banks) { selectedBank ->
            Log.d("BD", "$selectedBank")
            val bundle = Bundle().apply {
                putSerializable(SELECTED_BANK_NAME, selectedBank)
            }
            parentFragmentManager.setFragmentResult(BANK_SELECTED_KEY, bundle)
            dismiss()
        }

        binding.bankRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bankSearchAdapter
        }

        binding.searchBar.addTextChangedListener { text ->
            bankSearchAdapter.filter(text.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            BottomSheetBehavior.from(it).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isDraggable = true
            }
        }
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun getTheme() = R.style.FullScreenBottomSheetDialogTheme

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



val banks = listOf(
    Bank("9 Payment Service Bank", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "825", ussd = ""),
    Bank("9 Payment Service Bank (9PSB)", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "242", ussd = ""),
    Bank("Aaa Finance", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "268", ussd = ""),
    Bank("Abbey Mortgage Bank", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "293", ussd = ""),
    Bank("AB Microfinance Bank", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "423", ussd = ""),
    Bank("Above Only Microfinance Bank", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "413", ussd = ""),
    Bank("Abucoop Microfinance Bank", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "544", ussd = ""),
    Bank("Abulesoro Microfinance Bank Ltd", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "659", ussd = ""),
    Bank("ABU Microfinance Bank", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "402", ussd = ""),
    Bank("Accelerex Network", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "405", ussd = ""),
    Bank("Access Bank", logo = "https://nigerianbanks.xyz/logo/access-bank.png", slug = "access-bank", code = "044", ussd = "*901#"),
    Bank("Access Bank (Diamond)", logo = "https://nigerianbanks.xyz/logo/access-bank-diamond.png", slug = "access-bank-diamond", code = "063", ussd = "*426#"),
    Bank("AccessMobile", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "769", ussd = ""),
    Bank("Accion Microfinance Bank", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "342", ussd = ""),
    Bank("Ada Microfinance Bank", logo = "https://nigerianbanks.xyz/logo/default-image.png", slug = "", code = "599", ussd = "")
)

