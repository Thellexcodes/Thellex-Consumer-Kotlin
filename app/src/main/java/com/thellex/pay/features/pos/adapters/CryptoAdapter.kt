package com.thellex.pay.features.pos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pay.data.model.TokenListDto
import com.thellex.pay.databinding.ItemCryptoBinding
import java.util.Locale

class CryptoAdapter(
    private val cryptoList: MutableList<TokenListDto>,
    private val onItemClick: (TokenListDto) -> Unit
) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val binding = ItemCryptoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CryptoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val crypto = cryptoList[position]
        holder.bind(crypto)
    }

    override fun getItemCount(): Int = cryptoList.size

    fun updateData(newCryptoList: List<TokenListDto>) {
        cryptoList.clear()
        cryptoList.addAll(newCryptoList)
        notifyDataSetChanged()
    }

    inner class CryptoViewHolder(private val binding: ItemCryptoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(crypto: TokenListDto) {
            binding.cryptoName.text = crypto.assetCode.toString().uppercase(Locale.getDefault())
            binding.cryptoIcon.setImageResource(crypto.iconRes)

            binding.root.setOnClickListener {
                onItemClick(crypto)
            }
        }
    }

    companion object {
        private val TAG = "TAG"
    }
}