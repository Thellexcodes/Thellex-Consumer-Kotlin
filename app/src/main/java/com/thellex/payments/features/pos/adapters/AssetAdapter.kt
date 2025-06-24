package com.thellex.payments.features.pos.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R
import com.thellex.payments.settings.LocalValue
import java.util.Locale

class AssetAdapter(
    private var assets: MutableList<Asset>,
    private val onItemClick: (Asset) -> Unit
) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    inner class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageAssetIcon: ImageView = itemView.findViewById(R.id.imageAssetIcon)
        val textAssetSymbol: TextView = itemView.findViewById(R.id.textAssetSymbol)
        val textTokenAmount: TextView = itemView.findViewById(R.id.textTokenAmount)
        val textTokenValueUsd: TextView = itemView.findViewById(R.id.textTokenValueUsd)
        val textTokenValueInLocal: TextView = itemView.findViewById(R.id.textTokenValueLocal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asset, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = assets[position]
        holder.textAssetSymbol.text = asset.symbol.uppercase(Locale.getDefault())
        holder.textTokenAmount.text = "${asset.amount} ${asset.symbol}"
        holder.textTokenValueUsd.text = "$ ${asset.usdValue}"
        holder.textTokenValueInLocal.text = "\u2248 ${asset.valueInLocal} $LocalValue"
        holder.imageAssetIcon.setImageResource(asset.iconResId)


        holder.itemView.setOnClickListener {
            onItemClick(asset)
        }
    }

    override fun getItemCount(): Int = assets.size

    fun updateData(newAssets: List<Asset>) {
        assets.clear()
        assets.addAll(newAssets)
        notifyDataSetChanged()
    }
}

data class Asset(
    val symbol: String,
    val amount: String,
    val usdValue: String,
    val valueInLocal: String,
    val iconResId: Int
)
