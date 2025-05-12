package com.example.thellex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class POSTransactionAdapter(private val items: List<PosTransaction>) :
    RecyclerView.Adapter<POSTransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon = view.findViewById<ImageView>(R.id.token_icon)
        val status = view.findViewById<ImageView>(R.id.status_icon)
        val sender = view.findViewById<TextView>(R.id.sender_name)
        val time = view.findViewById<TextView>(R.id.time_text)
        val tokenAmount = view.findViewById<TextView>(R.id.token_amount)
        val fiatAmount = view.findViewById<TextView>(R.id.fiat_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconResId)
        holder.status.setImageResource(item.statusIconResId)
        holder.sender.text = "${item.senderName} sent you"
        holder.time.text = item.time
        holder.tokenAmount.text = item.amount
        holder.fiatAmount.text = item.fiatValue
    }

    override fun getItemCount(): Int = items.size
}
