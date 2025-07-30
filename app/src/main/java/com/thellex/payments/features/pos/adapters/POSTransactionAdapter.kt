package com.thellex.payments.features.pos.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers.formatAmountWithSymbol
import com.thellex.payments.data.model.PosTransaction
import com.thellex.payments.core.utils.Helpers.formatTimestamp
import com.thellex.payments.core.utils.Helpers.getIconResIdForToken
import com.thellex.payments.core.utils.Helpers.getStatusIconResId
import com.thellex.payments.core.utils.Helpers.mapToTransactionStatus
import com.thellex.payments.data.model.ITransactionHistoryDto
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.TransactionTypeEnum
import java.util.Locale

class POSTransactionAdapter(
    private val onItemClick: (PosTransaction) -> Unit
) : ListAdapter<PosTransaction, POSTransactionAdapter.TransactionViewHolder>(PosTransactionDiffCallback()) {

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
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: PosTransaction) {
            item.iconResId?.let { txnIcon.setImageResource(it) }
            item.statusIconResId?.let { statusIcon.setImageResource(it) }
            description.text = item.description
            timeText.text = item.time
            amount.text = item.amount
            status.text = item.paymentStatus.name

            if (item.paymentStatus == PaymentStatusEnum.Complete) {
                status.setTextColor(ContextCompat.getColor(status.context, R.color.green))
            } else {
                status.setTextColor(ContextCompat.getColor(status.context, R.color.white))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(newItems: List<ITransactionHistoryDto>) {
            val posTransactions = newItems.map { transaction ->
            val displayAmount = when (transaction.transactionType) {
                TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT -> transaction.amount
                TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> transaction.mainAssetAmount
                TransactionTypeEnum.CRYPTO_TO_FIAT_DEPOSIT -> transaction.mainAssetAmount
                TransactionTypeEnum.FIAT_TO_CRYPTO_WITHDRAWAL -> transaction.mainAssetAmount
                TransactionTypeEnum.FIAT_TO_FIAT_DEPOSIT -> transaction.mainFiatAmount
                TransactionTypeEnum.FIAT_TO_FIAT_WITHDRAWAL -> transaction.mainFiatAmount
                TransactionTypeEnum.CRYPTO_DEPOSIT -> transaction.amount
                TransactionTypeEnum.CRYPTO_WITHDRAWAL -> transaction.amount
                else -> transaction.amount
            }

            PosTransaction(
                iconResId = getIconResIdForToken(transaction.assetCode),
                statusIconResId = getStatusIconResId(transaction.paymentStatus),
                description = transaction.assetCode.uppercase(Locale.getDefault()),
                time = formatTimestamp(transaction.createdAt),
                amountWithSymbol = formatAmountWithSymbol(displayAmount.toString()),
                paymentStatus = mapToTransactionStatus(transaction.paymentStatus),
                id = transaction.id,
                transactionType = transaction.transactionType,
                rampID = transaction.rampID,
                amount = displayAmount.toString()
            )
        }
        submitList(posTransactions)
    }

}

class PosTransactionDiffCallback : DiffUtil.ItemCallback<PosTransaction>() {
    override fun areItemsTheSame(oldItem: PosTransaction, newItem: PosTransaction): Boolean {
        // Assuming PosTransaction has a unique identifier like transactionId
        return oldItem.time == newItem.time
    }

    override fun areContentsTheSame(oldItem: PosTransaction, newItem: PosTransaction): Boolean {
        return oldItem == newItem
    }
}