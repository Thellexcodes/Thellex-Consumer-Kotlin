package com.thellex.payments.features.pos.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.thellex.payments.R
import com.thellex.payments.core.decorators.ItemSpacingDecoration
import com.thellex.payments.core.utils.ComposeHostActivity
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.databinding.FragmentWithdrawalsBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.pos.adapters.POSTransactionAdapter

class WithdrawalsFragment : Fragment() {
    private var _binding: FragmentWithdrawalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: POSTransactionAdapter
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWithdrawalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeUserTransactions()
    }

    private fun setupRecyclerView() {
        binding.recyclerWithdrawals.layoutManager = LinearLayoutManager(requireContext())
        transactionAdapter = POSTransactionAdapter { transaction ->
            when (transaction.transactionType) {
                TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL,
                TransactionTypeEnum.FIAT_TO_CRYPTO_WITHDRAWAL -> {
                    transaction.rampID?.let { rampId ->
                        val intent = ComposeHostActivity.newRampTransactionDetailIntent(requireContext(), rampId)
                        startActivity(intent)
                    } ?: Log.d("WithdrawalsFragment", "No rampID for ${transaction.transactionType}")
                }
                TransactionTypeEnum.CRYPTO_WITHDRAWAL -> {
                    // Handle CRYPTO_WITHDRAWAL (e.g., show a different activity or dialog)
                }
                else -> {
                    Log.d("WithdrawalsFragment", "Unexpected transaction type: ${transaction.transactionType}")
                }
            }
        }
        binding.recyclerWithdrawals.adapter = transactionAdapter
        val itemSpacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        binding.recyclerWithdrawals.addItemDecoration(ItemSpacingDecoration(itemSpacing))
    }

    private fun observeUserTransactions() {
        userViewModel.withdrawalTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.updateList(transactions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}