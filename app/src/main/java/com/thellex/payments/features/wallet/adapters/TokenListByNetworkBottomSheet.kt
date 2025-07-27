package com.thellex.payments.features.wallet.adapters

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thellex.payments.databinding.FragmentTokenSelectionBinding
import com.thellex.payments.features.fiat.adapters.TokenAdapter
import com.thellex.payments.features.wallet.model.WalletBalanceDto
import com.thellex.payments.features.wallet.model.WalletDto
import com.thellex.payments.settings.SupportedBlockchainEnum
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TokenListByNetworkBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentTokenSelectionBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()
    private lateinit var adapter: TokenAdapter
    private var searchJob: Job? = null

    companion object {
        const val TAG = "TokenListByNetworkBottomSheet"
        const val RESULT_KEY = "token_selection_result"
        const val TOKEN_KEY = "selected_token"
        private const val ARG_WALLETS_JSON = "wallet_balance_json"
        private const val ARG_SELECTED_NETWORK = "selected_network"

        fun newInstance(walletBalance: WalletBalanceDto, selectedNetwork: SupportedBlockchainEnum): TokenListByNetworkBottomSheet {
            val fragment = TokenListByNetworkBottomSheet()
            val json = Gson().toJson(walletBalance.wallets)
            fragment.arguments = Bundle().apply {
                putString(ARG_WALLETS_JSON, json)
                putString(ARG_SELECTED_NETWORK, selectedNetwork.name) // pass as String
            }
            return fragment
        }
    }

    private lateinit var filteredTokens: List<WalletDto>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTokenSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TokenAdapter { token ->
            val result = Bundle().apply {
                putString(TOKEN_KEY, gson.toJson(token))
            }
            parentFragmentManager.setFragmentResult(RESULT_KEY, result)
            dismiss()
        }

        val walletBalanceJson = arguments?.getString(ARG_WALLETS_JSON)
        val selectedNetworkName = arguments?.getString(ARG_SELECTED_NETWORK)
        val selectedNetwork = selectedNetworkName?.let { SupportedBlockchainEnum.valueOf(it) }

        val wallets: Map<String, WalletDto> = try {
            walletBalanceJson?.let {
                val type = object : TypeToken<Map<String, WalletDto>>() {}.type
                gson.fromJson(it, type)
            } ?: emptyMap()
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading wallet data: ${e.message}", Toast.LENGTH_SHORT).show()
            emptyMap()
        }

        if (wallets.isEmpty() || selectedNetwork == null) {
            Toast.makeText(context, "No tokens available for the selected network", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        // Filter tokens by selected network
        filteredTokens = wallets.values.filter { it.network == selectedNetwork }

        if (filteredTokens.isEmpty()) {
            Toast.makeText(context, "No tokens available on $selectedNetwork", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        binding.recyclerviewTokens.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TokenListByNetworkBottomSheet.adapter
        }
        adapter.submitList(filteredTokens)

        binding.edittextSearchToken.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = MainScope().launch {
                    delay(300)
                    val query = s.toString().trim()
                    val filtered = if (query.isEmpty()) {
                        filteredTokens
                    } else {
                        filteredTokens.filter {
                            it.assetCode.name.contains(query, ignoreCase = true)
                        }
                    }
                    adapter.submitList(filtered)

                    if (filtered.isEmpty()) {
                        binding.recyclerviewTokens.visibility = View.GONE
                        binding.textviewNoTokensFound.visibility = View.VISIBLE
                    } else {
                        binding.recyclerviewTokens.visibility = View.VISIBLE
                        binding.textviewNoTokensFound.visibility = View.GONE
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
