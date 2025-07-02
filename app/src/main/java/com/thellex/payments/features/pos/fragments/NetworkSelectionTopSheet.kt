package com.thellex.payments.features.pos.fragments

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.getDisplayNameForNetwork
import com.thellex.payments.databinding.FragmentNetworkTokenSelectionTopSheetBinding
import com.thellex.payments.databinding.ItemNetworkBinding
import com.thellex.payments.features.wallet.model.WalletDto
import com.thellex.payments.settings.SupportedBlockchainEnum

class NetworkSelectionTopSheet(
    private val wallets: Map<String, WalletDto>,
    private val onNetworkSelected: (WalletDto) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentNetworkTokenSelectionTopSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkTokenSelectionTopSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Filter unique networks using the enum directly
        val uniqueNetworks = wallets.values.distinctBy { it.network }

        binding.tokenRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tokenRecyclerView.adapter = NetworkAdapter(uniqueNetworks) { selected ->
            onNetworkSelected(selected)
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

    inner class NetworkAdapter(
        private val networks: List<WalletDto>,
        private val onClick: (WalletDto) -> Unit
    ) : RecyclerView.Adapter<NetworkAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemNetworkBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(wallet: WalletDto) {
                val networkEnum = wallet.network

                // ✅ Display name and icon using enum
                binding.networkName.text = getDisplayNameForNetwork(networkEnum)
                binding.networkIcon.setImageResource(Helpers.getIconResIdForBlockchain(networkEnum.name.lowercase()))

                binding.root.setOnClickListener { onClick(wallet) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemNetworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int = networks.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(networks[position])
        }
    }

    companion object {
        private const val TAG = "NetworkSelectionTopSheet"

        fun show(
            fragmentManager: FragmentManager,
            wallets: Map<String, WalletDto>,
            onNetworkSelected: (WalletDto) -> Unit
        ) {
            val sheet = NetworkSelectionTopSheet(wallets, onNetworkSelected)
            sheet.show(fragmentManager, TAG)
        }

        // Optional: You can customize display names here
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


