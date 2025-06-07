package com.thellex.pos.features.pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pos.R
import com.thellex.pos.data.model.PosTransaction
import com.thellex.pos.data.model.TransactionHistoryEntity
import com.thellex.pos.utils.Helpers.formatTimestamp
import com.thellex.pos.utils.Helpers.getIconResIdForToken
import com.thellex.pos.utils.Helpers.getStatusIconResId
import com.thellex.pos.utils.Helpers.mapToTransactionStatus
import java.util.Locale

class POSTransactionAdapter(
    private var items: List<PosTransaction>,
    private val onItemClick: (PosTransaction) -> Unit
) : RecyclerView.Adapter<POSTransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txnIcon: ImageView = view.findViewById(R.id.txn_icon)
        val statusIcon: ImageView = view.findViewById(R.id.status_icon)
        val description: TextView = view.findViewById(R.id.txn_description)
        val timeText: TextView = view.findViewById(R.id.time_text)
        val amount: TextView = view.findViewById(R.id.amount)
        val status: TextView = view.findViewById(R.id.status)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = items[position]
        item.iconResId?.let { holder.txnIcon.setImageResource(it) }
        item.statusIconResId?.let { holder.statusIcon.setImageResource(it) }
        holder.description.text = item.description
        holder.timeText.text = item.time
        holder.amount.text = item.amountWithSymbol
        holder.status.text = item.status.toString()
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<TransactionHistoryEntity>) {
        items = newItems.map { entity ->
            PosTransaction(
                iconResId = getIconResIdForToken(entity.currency),
                statusIconResId = getStatusIconResId(entity.status),
                description = entity.currency.uppercase(Locale.getDefault()),
                time = formatTimestamp("${entity.createdAt}"),
                amountWithSymbol = entity.amount,
                status = mapToTransactionStatus(entity.status)
            )
        }
        notifyDataSetChanged()
    }
}


