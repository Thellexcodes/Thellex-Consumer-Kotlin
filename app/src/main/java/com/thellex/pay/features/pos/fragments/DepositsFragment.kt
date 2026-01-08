package com.thellex.pay.features.pos.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.thellex.pay.R
import com.thellex.pay.core.decorators.ItemSpacingDecoration
import com.thellex.pay.core.routes.ComposeRoutes
import com.thellex.pay.core.utils.ComposeHostActivity
import com.thellex.pay.data.datastore.getBaseSettingsCache
import com.thellex.pay.data.model.BaseSettingsCache
import com.thellex.pay.data.model.PaymentStatusEnum
import com.thellex.pay.data.model.PosTransaction
import com.thellex.pay.data.model.TransactionTypeEnum
import com.thellex.pay.databinding.FragmentDepositsBinding
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.pos.adapters.POSTransactionAdapter
import com.thellex.pay.features.wallet.ui.CryptoTransactionSummary
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class DepositsFragment : Fragment() {
    private var _binding: FragmentDepositsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: POSTransactionAdapter
    private val userViewModel: UserViewModel by activityViewModels()
    private var baseSettingsCache: BaseSettingsCache? = null

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

        lifecycleScope.launch {
            baseSettingsCache = requireContext().getBaseSettingsCache()
        }
    }

    private fun PosTransaction.toCryptoTransactionSummary(transaction: PosTransaction): CryptoTransactionSummary {
        return CryptoTransactionSummary(
            amount = transaction.amount,
            assetCode = TokenEnum.usdt,
            valueInUsd = transaction.valueInUsd,
            valueInLocal = transaction.valueInLocal,
            sourceAddress = transaction.sourceAddress,
            fundUid = transaction.fundUid,
            network = transaction.paymentNetwork,
            networkName = "Stellar",
            networkFee = 0.0,
            reason = transaction.reason
        )
    }

    private fun resolveTokenIcon(
        network: SupportedBlockchainEnum,
        token: TokenEnum
    ): String? {
        val chains = baseSettingsCache?.chains ?: return null

        return chains
            .firstOrNull { it.id == network }
            ?.supportedTokens
            ?.firstOrNull { it.symbol == token }
            ?.iconDisplay
    }

    private fun setupRecyclerView() {
        binding.recyclerDeposits.layoutManager = LinearLayoutManager(requireContext())

        transactionAdapter = POSTransactionAdapter { transaction ->
            when (transaction.transactionType) {

                TransactionTypeEnum.CRYPTO_TO_FIAT_DEPOSIT,
                TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT -> {
                    transaction.rampID?.let { rampId ->
                        val intent =
                            ComposeHostActivity.newRampTransactionDetailIntent(
                                requireContext(),
                                rampId
                            )
                        startActivity(intent)
                    }
                }
                TransactionTypeEnum.CRYPTO_DEPOSIT,
                TransactionTypeEnum.CRYPTO_WITHDRAWAL -> {
                    val cryptoSummary = transaction.toCryptoTransactionSummary(transaction)
                    val jsonString = Json.encodeToString(cryptoSummary)

                    val encoded = Uri.encode(jsonString, "")

                    val route =
                        "${ComposeRoutes.CryptoTransactionDetail.route}/$encoded"

                    val intent = ComposeHostActivity.newIntent(
                        context = requireContext(),
                        startDestination = route
                    )

                    startActivity(intent)
                }
                else -> {
                    Log.d(
                        "DepositsFragment",
                        "Unhandled transaction type: ${transaction.transactionType}"
                    )
                }
            }
        }

        binding.recyclerDeposits.adapter = transactionAdapter
        binding.recyclerDeposits.addItemDecoration(
            ItemSpacingDecoration(
                resources.getDimensionPixelSize(R.dimen.txn_margin)
            )
        )
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