package com.thellex.pay.features.pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pay.R
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.databinding.ItemAssetBinding
import com.thellex.pay.settings.LocalValue
import java.util.Locale

class AssetAdapter(
    private var assets: MutableList<Asset>,
    private var isBalanceVisible: Boolean,
    private val onItemClick: (Asset) -> Unit,
    private val onActivateWalletClick: (Asset, (Boolean) -> Unit) -> Unit
) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    inner class AssetViewHolder(val binding: ItemAssetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = ItemAssetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = assets[position]
        val padding = holder.binding.clAssetItemRoot.context.resources.getDimensionPixelSize(R.dimen.padding_16dp)

        with(holder.binding) {
            tvAssetSymbolSecondary.text = asset.symbol.uppercase(Locale.getDefault())
            ivAssetIcon.setImageResource(asset.iconResId)

            if (Helpers.isValidEvmAddress(asset.address)) {
                clAssetDetails.visibility = View.VISIBLE
                tvTokenAmount.visibility = View.VISIBLE
                tvTokenValueUsd.visibility = View.VISIBLE
                tvTokenValueLocal.visibility = View.GONE
                btnActivateWallet.visibility = View.GONE
                tvAssetSymbolMain.visibility = View.GONE
                tvAssetSymbolSecondary.visibility = View.VISIBLE

                tvTokenAmount.text = if (isBalanceVisible) "${asset.amount} ${asset.symbol}" else "****"
                tvTokenValueUsd.text = if (isBalanceVisible) "$ ${asset.usdValue}" else "****"
                tvTokenValueLocal.text = if (isBalanceVisible) "\u2248 ${asset.valueInLocal} $LocalValue" else "****"

                root.setBackgroundResource(R.drawable.rounded_border)
                clAssetItemRoot.setPadding(padding, padding, padding, padding)

                root.setOnClickListener {
                    onItemClick(asset)
                }
            } else {
                clAssetDetails.visibility = View.GONE
                tvTokenAmount.visibility = View.GONE
                tvTokenValueUsd.visibility = View.GONE
                tvTokenValueLocal.visibility = View.GONE
                btnActivateWallet.visibility = View.VISIBLE
                tvAssetSymbolMain.visibility = View.VISIBLE
                tvAssetSymbolSecondary.visibility = View.GONE

                root.setBackgroundResource(R.drawable.rounded_border_midnight)
                clAssetItemRoot.setPadding(padding, padding, padding, padding)

                root.setOnClickListener(null)
                btnActivateWallet.setOnClickListener {
//                    btnActivateWallet.setSubmitting(true)
                    onActivateWalletClick(asset) { success ->
                        if (success) {
//                            btnActivateWallet.setSubmitting(false)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = assets.size

    fun updateData(newAssets: List<Asset>) {
        assets.clear()
        assets.addAll(newAssets)
        notifyDataSetChanged()
    }

    fun setBalanceVisibility(visible: Boolean) {
        isBalanceVisible = visible
        notifyDataSetChanged()
    }
}

data class Asset(
    val symbol: String,
    val amount: String,
    val usdValue: String,
    val valueInLocal: String,
    val iconResId: Int,
    val address: String
)