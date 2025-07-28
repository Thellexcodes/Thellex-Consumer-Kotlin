package com.thellex.payments.features.fiat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.core.utils.Helpers.getIconResIdForToken
import com.thellex.payments.databinding.ItemTokenBinding
import com.thellex.payments.features.wallet.model.WalletDto
import java.util.Locale

class TokenAdapter(
    private val onTokenSelected: (WalletDto) -> Unit
) : ListAdapter<WalletDto, TokenAdapter.TokenViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
        val binding = ItemTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val params = binding.root.layoutParams as ViewGroup.MarginLayoutParams
        val spacing = (parent.context.resources.displayMetrics.density * 12).toInt() // 12dp to px
        params.bottomMargin = spacing
        binding.root.layoutParams = params

        return TokenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TokenViewHolder(private val binding: ItemTokenBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(token: WalletDto) {
            binding.tokenName.text = token.assetCode.toString().uppercase(Locale.getDefault())
            binding.tokenIcon.setImageResource(getIconResIdForToken(token.assetCode.toString()))

            binding.root.setOnClickListener {
                onTokenSelected(token)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WalletDto>() {
        override fun areItemsTheSame(oldItem: WalletDto, newItem: WalletDto): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: WalletDto, newItem: WalletDto): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private val TAG = "TAGY"
    }
}
