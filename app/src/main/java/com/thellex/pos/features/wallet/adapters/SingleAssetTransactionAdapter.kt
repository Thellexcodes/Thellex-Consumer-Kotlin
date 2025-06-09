package com.thellex.pos.features.wallet.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pos.R
import com.thellex.pos.core.utils.Helpers.convertToUsd
import com.thellex.pos.core.utils.Helpers.formatTransactionDate
import com.thellex.pos.data.model.TransactionHistoryEntity
import com.thellex.pos.data.model.WalletWebhookEvent


class SingleAssetTransactionAdapter(
    private val currencyFilter: String,
    private val transactions: List<TransactionHistoryEntity>
) : RecyclerView.Adapter<SingleAssetTransactionAdapter.TransactionViewHolder>() {

    private val filteredTransactions = transactions.filter {
        it.currency.equals(currencyFilter, ignoreCase = true)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStatusLabel: TextView = itemView.findViewById(R.id.tvStatusLabel)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvEquivalent: TextView = itemView.findViewById(R.id.tvEquivalent)

        fun bind(transaction: TransactionHistoryEntity) {
            val context = itemView.context
            val eventEnum = WalletWebhookEvent.fromValue(transaction.event)
            val isWithdraw = eventEnum == WalletWebhookEvent.WITHDRAW_SUCCESSFUL

            tvStatusLabel.text = if (isWithdraw) "Withdraw" else "Deposit"
            val withdrawColor = ContextCompat.getColor(context, R.color.pinkRed)
            val depositColor = ContextCompat.getColor(context, R.color.green)
            tvStatusLabel.setTextColor(if (isWithdraw) withdrawColor else depositColor)

            // Show createdAt date/time or N/A
            tvTime.text = formatTransactionDate(transaction.createdAt)

            // Amount with minus sign for withdraws
            val amountText = if (isWithdraw) "-${transaction.amount}" else transaction.amount
            tvAmount.text = amountText
            tvAmount.setTextColor(if (isWithdraw) withdrawColor else depositColor)

            // Equivalent USD (stub conversion, update with actual logic)
            val equivalentUsd = convertToUsd(transaction.currency, transaction.amount)
            tvEquivalent.text = "$$equivalentUsd"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_single_asset_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount(): Int = filteredTransactions.size

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(filteredTransactions[position])
    }


}
