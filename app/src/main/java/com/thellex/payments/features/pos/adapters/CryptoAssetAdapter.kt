package com.thellex.payments.features.pos.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.thellex.payments.R

class CryptoAssetAdapter(context: Context, private val assets: List<CryptoAsset>) :
    ArrayAdapter<CryptoAsset>(context, 0, assets) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = createView(position, convertView, parent)

        // Add bottom margin to space out dropdown items
        val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
            ?: ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        layoutParams.bottomMargin = context.resources.getDimensionPixelSize(R.dimen.margin_2dp)
        view.layoutParams = layoutParams

        return view
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_crypto_dropdown, parent, false)

        val asset = getItem(position)
        val icon = view.findViewById<ImageView>(R.id.cryptoIcon)
        val name = view.findViewById<TextView>(R.id.cryptoName)

        asset?.let {
            icon.setImageResource(it.iconRes)
            name.text = it.name
        }

        return view
    }
}

data class CryptoAsset(val name: String, val iconRes: Int)
