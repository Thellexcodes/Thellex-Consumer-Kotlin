package com.thellex.payments.features.pos.fragments

import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.databinding.FragmentNetworkTokenSelectionTopSheetBinding
import com.thellex.payments.databinding.ItemTokenNetworkBinding
import com.thellex.payments.features.wallet.model.WalletDto
import com.thellex.payments.settings.SupportedBlockchainEnum
import java.util.Locale

class NetworkTokenSelectionTopSheet(
    private val wallets: Map<String, WalletDto>,
    private val network: SupportedBlockchainEnum,
    private val onTokenSelected: (WalletDto) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentNetworkTokenSelectionTopSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkTokenSelectionTopSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.tokenRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ✅ Filter using enum directly
        val filteredTokens = wallets.values.filter { it.network == network }

        recyclerView.adapter = TokenAdapter(filteredTokens) { token ->
            onTokenSelected(token)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                val displayMetrics = Resources.getSystem().displayMetrics
                val halfScreenHeight = displayMetrics.heightPixels / 2
                it.layoutParams.height = halfScreenHeight
                behavior.peekHeight = halfScreenHeight
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TokenAdapter(
        private val tokens: List<WalletDto>,
        private val onClick: (WalletDto) -> Unit
    ) : RecyclerView.Adapter<TokenAdapter.ViewHolder>() {

        inner class ViewHolder(private val binding: ItemTokenNetworkBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(token: WalletDto) {
                binding.tokenSymbol.text = token.assetCode.name.uppercase(Locale.getDefault())
                binding.tokenNetwork.text = getDisplayNameForNetwork(token.network)

                val iconRes = Helpers.getIconResIdForToken(token.assetCode.name.lowercase(Locale.getDefault()))
                binding.tokenIcon.setImageResource(iconRes)

                binding.root.setOnClickListener {
                    onClick(token)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTokenNetworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(tokens[position])
        }

        override fun getItemCount() = tokens.size
    }

    companion object {
        private const val TAG = "NetworkTokenSelectionBottomSheet"

        fun show(
            fragmentManager: FragmentManager,
            wallets: Map<String, WalletDto>,
            network: SupportedBlockchainEnum,
            onTokenSelected: (WalletDto) -> Unit
        ) {
            val sheet = NetworkTokenSelectionTopSheet(wallets, network, onTokenSelected)
            sheet.show(fragmentManager, TAG)
        }

        // Optional: Display name formatting based on enum
        fun getDisplayNameForNetwork(network: SupportedBlockchainEnum): String {
            return when (network) {
                SupportedBlockchainEnum.bep20 -> "Binance Smart Chain"
                SupportedBlockchainEnum.matic -> "Polygon"
                SupportedBlockchainEnum.stellar -> "Stellar"
                SupportedBlockchainEnum.base -> "Base"
                SupportedBlockchainEnum.lisk -> "Lisk"
            }
        }
    }
}




