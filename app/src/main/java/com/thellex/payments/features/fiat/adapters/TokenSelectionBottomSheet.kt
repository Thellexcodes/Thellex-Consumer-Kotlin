package com.thellex.payments.features.fiat.adapters

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
import com.thellex.payments.databinding.FragmentTokenSelectionBinding
import com.thellex.payments.features.wallet.model.WalletBalanceDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.gson.reflect.TypeToken
import com.thellex.payments.features.wallet.model.WalletDto

class TokenSelectionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentTokenSelectionBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()
    private lateinit var adapter: TokenAdapter
    private var searchJob: Job? = null

    companion object {
        const val TAG = "TokenSelectionBottomSheet"
        const val RESULT_KEY = "token_selection_result"
        const val TOKEN_KEY = "selected_token"

        fun newInstance(walletBalance: WalletBalanceDto): TokenSelectionBottomSheet {
            val fragment = TokenSelectionBottomSheet()
            val json = Gson().toJson(walletBalance.wallets)
            fragment.arguments = Bundle().apply {
                putString("wallet_balance_json", json)
            }
            return fragment
        }
    }

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

        val walletBalanceJson = arguments?.getString("wallet_balance_json")
        val wallets: Map<String, WalletDto> = try {
            walletBalanceJson?.let {
                val type = object : TypeToken<Map<String, WalletDto>>() {}.type
                gson.fromJson(it, type)
            } ?: emptyMap()
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading wallet data: ${e.message}", Toast.LENGTH_SHORT).show()
            emptyMap()
        }

        if (wallets.isEmpty()) {
            Toast.makeText(context, "No tokens available", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        val tokens = wallets.values.toList()

        binding.recyclerviewTokens.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TokenSelectionBottomSheet.adapter
        }
        adapter.submitList(tokens)

        binding.edittextSearchToken.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = MainScope().launch {
                    delay(300)
                    val query = s.toString().trim()
                    val filteredTokens = if (query.isEmpty()) {
                        tokens
                    } else {
                        tokens.filter {
                            it.assetCode.name.contains(query, ignoreCase = true)
                        }
                    }
                    adapter.submitList(filteredTokens)
                    binding.recyclerviewTokens.visibility =
                        if (filteredTokens.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//
//class TokensEnumDeserializer : JsonDeserializer<TokensEnum> {
//    override fun deserialize(
//        json: JsonElement?,
//        typeOfT: Type?,
//        context: JsonDeserializationContext?
//    ): TokensEnum {
//        val value = json?.asString?.uppercase() ?: throw IllegalArgumentException("Invalid assetCode")
//        return try {
//            TokensEnum.valueOf(value)
//        } catch (e: IllegalArgumentException) {
//            throw IllegalArgumentException("Unknown assetCode: $value")
//        }
//    }
//}
//
