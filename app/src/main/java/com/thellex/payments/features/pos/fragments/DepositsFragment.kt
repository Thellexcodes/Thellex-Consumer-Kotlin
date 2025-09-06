package com.thellex.payments.features.pos.ui

import android.content.Intent
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
import com.thellex.payments.core.routes.ComposeRoutes
import com.thellex.payments.core.utils.ComposeHostActivity
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.databinding.FragmentDepositsBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.pos.adapters.POSTransactionAdapter

class DepositsFragment : Fragment() {
    private var _binding: FragmentDepositsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: POSTransactionAdapter
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDepositsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeUserTransactions()
    }

    private fun setupRecyclerView() {
        binding.recyclerDeposits.layoutManager = LinearLayoutManager(requireContext())
        transactionAdapter = POSTransactionAdapter { transaction ->
            when (transaction.transactionType) {
                TransactionTypeEnum.CRYPTO_TO_FIAT_DEPOSIT,
                TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT -> {
                    transaction.rampID?.let { rampId ->
                        val intent = ComposeHostActivity.newIntent(requireContext(), ComposeRoutes.RampTransactionDetail.route)
                        intent.putExtra("ramp_id", rampId)
                        startActivity(intent)
                    } ?: Log.d("DepositsFragment", "No rampID for ${transaction.transactionType}")
                }
                TransactionTypeEnum.CRYPTO_DEPOSIT -> {
                    Log.d("DepositsFragment", "Crypto deposit clicked: ${transaction.transactionType}")
                    // Handle CRYPTO_DEPOSIT (e.g., show a different activity or dialog)
                }
                else -> {
                    Log.d("DepositsFragment", "Unexpected transaction type: ${transaction.transactionType}")
                }
            }
        }
        binding.recyclerDeposits.adapter = transactionAdapter
        val itemSpacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        binding.recyclerDeposits.addItemDecoration(ItemSpacingDecoration(itemSpacing))
    }

    private fun observeUserTransactions() {
        userViewModel.depositTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.updateList(transactions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}