package com.thellex.payments.features.pos.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.thellex.payments.R
import com.thellex.payments.data.model.BlockchainItem
import java.util.Locale

class CryptoSpinnerAdapter(
    context: Context,
    private val items: List<BlockchainItem>
) : ArrayAdapter<BlockchainItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_crypto_dropdown, parent, false)
        val item = items[position]

        val icon = view.findViewById<ImageView>(R.id.cryptoIcon)
        val name = view.findViewById<TextView>(R.id.cryptoName)

        icon.setImageResource(item.iconRes)
        name.text = item.chain.toString().uppercase(Locale.getDefault())

        return view
    }
}
