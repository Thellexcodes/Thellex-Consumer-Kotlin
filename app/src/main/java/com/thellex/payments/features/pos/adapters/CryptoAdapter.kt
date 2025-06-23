package com.thellex.payments.features.pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R
import com.thellex.payments.data.model.Crypto
import com.thellex.payments.data.model.TokenListDto
import java.util.Locale

class CryptoAdapter(
    private val cryptoList: MutableList<TokenListDto>,
    private val onItemClick: (TokenListDto) -> Unit
) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_crypto, parent, false)
        return CryptoViewHolder(view)
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

    inner class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cryptoName: TextView = itemView.findViewById(R.id.cryptoName)
        private val cryptoIcon: ImageView = itemView.findViewById(R.id.cryptoIcon)

        fun bind(crypto: TokenListDto) {
            cryptoName.text = crypto.assetCode.toString().uppercase(Locale.getDefault())
            cryptoIcon.setImageResource(crypto.iconRes)

            itemView.setOnClickListener {
                onItemClick(crypto)
            }
        }
    }
}


