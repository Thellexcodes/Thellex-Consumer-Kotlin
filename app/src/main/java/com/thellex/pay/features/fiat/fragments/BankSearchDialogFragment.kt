package com.thellex.pay.features.fiat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.pay.R
import com.thellex.pay.databinding.FragmentBankSearchDialogBinding
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.features.fiat.adapters.BankSearchAdapter

class BankSearchDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBankSearchDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var bankSearchAdapter: BankSearchAdapter
    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory(requireContext()) }

    companion object {
        const val BANK_SELECTED_KEY = "bankSelectedKey"
        const val SELECTED_BANK_NAME = "selectedBankName"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBankSearchDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe ngBanks from UserViewModel
        userViewModel.authResult
        userViewModel.authResult.observe(viewLifecycleOwner) { user ->
            val banks = user?.banks ?: emptyList()
            bankSearchAdapter = BankSearchAdapter(banks) { selectedBank ->
                val bundle = Bundle().apply {
                    putSerializable(SELECTED_BANK_NAME, selectedBank) // Serialize BankDto
                }
                parentFragmentManager.setFragmentResult(BANK_SELECTED_KEY, bundle)
                dismiss()
            }

            binding.bankRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = bankSearchAdapter
            }
        }

        // Set up search bar
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