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
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.formatAmountWithSymbol
import com.thellex.payments.data.model.PosTransaction
import com.thellex.payments.core.utils.Helpers.formatTimestamp
import com.thellex.payments.core.utils.Helpers.getIconResIdForToken
import com.thellex.payments.core.utils.Helpers.getStatusIconResId
import com.thellex.payments.core.utils.Helpers.mapToTransactionStatus
import com.thellex.payments.core.utils.Helpers.roundToTwoDecimals
import com.thellex.payments.data.model.ITransactionHistoryDto
import com.thellex.payments.data.model.PaymentStatusEnum
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.databinding.ItemTransactionBinding
import java.util.Locale

class POSTransactionAdapter(
    private val onItemClick: (PosTransaction) -> Unit
) : ListAdapter<PosTransaction, POSTransactionAdapter.TransactionViewHolder>(PosTransactionDiffCallback()) {

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: PosTransaction) {
            item.iconResId?.let { binding.txnIcon.setImageResource(it) }
            item.statusIconResId?.let { binding.statusIcon.setImageResource(it) }
            binding.txnDescription.text = item.description
            binding.timeText.text = item.time
            binding.amount.text = item.amount

            binding.status.text = item.paymentStatus.toString()
            val colorRes = Helpers.getPaymentStatusColor(item.paymentStatus)
            binding.status.text = item.paymentStatus.toString().uppercase()
            binding.status.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(newItems: List<ITransactionHistoryDto>) {
        val posTransactions = newItems.map { transaction ->
            val displayAmount = when (transaction.transactionType) {
                TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT,
                TransactionTypeEnum.CRYPTO_DEPOSIT,
                TransactionTypeEnum.CRYPTO_WITHDRAWAL,
                TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> transaction.amount.toDoubleOrNull()?.roundToTwoDecimals() ?: 0.0

                TransactionTypeEnum.CRYPTO_TO_FIAT_DEPOSIT,
                TransactionTypeEnum.FIAT_TO_CRYPTO_WITHDRAWAL -> transaction.mainAssetAmount.roundToTwoDecimals()

                TransactionTypeEnum.FIAT_TO_FIAT_DEPOSIT,
                TransactionTypeEnum.FIAT_TO_FIAT_WITHDRAWAL -> transaction.mainFiatAmount.roundToTwoDecimals()

                else -> transaction.amount.toDoubleOrNull()?.roundToTwoDecimals() ?: 0.0
            }


            PosTransaction(
                iconResId = getIconResIdForToken(transaction.assetCode),
                statusIconResId = getStatusIconResId(transaction.transactionType.toString()),
                description = transaction.assetCode.uppercase(Locale.getDefault()),
                time = formatTimestamp(transaction.createdAt),
                amountWithSymbol = formatAmountWithSymbol(displayAmount.toString()),
                paymentStatus = mapToTransactionStatus(transaction.paymentStatus.toString()),
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
        return oldItem.id == newItem.id // or use a unique field
    }

    override fun areContentsTheSame(oldItem: PosTransaction, newItem: PosTransaction): Boolean {
        return oldItem == newItem
    }
}
