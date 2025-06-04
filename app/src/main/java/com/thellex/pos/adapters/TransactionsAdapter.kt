package com.thellex.pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pos.R
import com.thellex.pos.data.model.Transaction
import com.thellex.pos.data.model.TransactionType

class TransactionsAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.user_dashboard_txn_img_placeholder)
        private val description: TextView = view.findViewById(R.id.txn_description)
        private val time: TextView = view.findViewById(R.id.txn_time)
        private val amount: TextView = view.findViewById(R.id.txn_amount)

        fun bind(transaction: Transaction) {
            icon.setImageResource(transaction.iconResId)
            description.text = transaction.description
            time.text = transaction.timestamp
            amount.text = transaction.amount

            // Customize based on transaction type
            when (transaction.type) {
                TransactionType.DEPOSIT -> icon.setBackgroundResource(R.drawable.icon_usdc)
                TransactionType.WITHDRAW -> icon.setBackgroundResource(R.drawable.icon_usdt)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_dashboard_item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size
}
