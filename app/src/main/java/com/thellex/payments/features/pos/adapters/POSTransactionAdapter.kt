package com.thellex.payments.features.pos.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
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

    companion object {
        private const val TAG = "POSTransactionAdapter"
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    onItemClick(item)
                } else {
                    Log.w(TAG, "Invalid position on click: $position")
                }
            }
        }

        fun bind(item: PosTransaction) {
            Log.d(TAG, "Binding PosTransaction: id=${item.id}, type=${item.transactionType}, status=${item.paymentStatus}")
            binding.txnIcon.setImageResource(item.iconResId ?: R.drawable.icon_txn)
            binding.txnDescription.text = item.description.uppercase(Locale.getDefault()) ?: "Unknown"
            binding.timeText.text = item.time ?: "N/A"
            binding.amount.text = item.amountWithSymbol ?: "0.00"
            binding.status.text = item.paymentStatus.toString().uppercase(Locale.getDefault()) ?: "UNKNOWN"
            binding.statusIcon.setImageResource(item.statusIconResId)

            val colorRes = Helpers.getPaymentStatusColor(item.paymentStatus ?: PaymentStatusEnum.Unknown)
            binding.status.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        Log.d(TAG, "Creating ViewHolder for viewType: $viewType")
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            Log.d(TAG, "Binding item at position $position: id=${item.id}, type=${item.transactionType}")
            holder.bind(item)
        } else {
            Log.w(TAG, "Null item at position $position")
        }
    }

    fun updateList(newItems: List<ITransactionHistoryDto>) {
        val posTransactions = newItems.mapNotNull { transaction ->
            try {
                val displayAmount = calculateDisplayAmount(transaction)
                PosTransaction(
                    iconResId = getIconResIdForToken(transaction.assetCode),
                    statusIconResId = getStatusIconResId(transaction.transactionType.toString()),
                    description = transaction.assetCode.uppercase(Locale.getDefault()) ?: "Unknown",
                    time = formatTimestamp(transaction.createdAt),
                    amountWithSymbol = formatAmountWithSymbol(displayAmount.toString()),
                    paymentStatus = mapToTransactionStatus(transaction.paymentStatus?.toString() ?: "UNKNOWN"),
                    id = transaction.id,
                    transactionType = transaction.transactionType,
                    rampID = transaction.rampID,
                    amount = displayAmount.toString()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to map transaction ${transaction.id}: ${e.message}", e)
                null
            }
        }
        Log.d(TAG, "Submitting ${posTransactions.size} PosTransactions")
        submitList(posTransactions)
    }

    private fun calculateDisplayAmount(transaction: ITransactionHistoryDto): Double {
        return try {
            when (transaction.transactionType) {
                TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT,
                TransactionTypeEnum.CRYPTO_DEPOSIT,
                TransactionTypeEnum.CRYPTO_WITHDRAWAL,
                TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL -> {
                    transaction.amount.toDoubleOrNull()?.roundToTwoDecimals() ?: 0.0
                }
                TransactionTypeEnum.CRYPTO_TO_FIAT_DEPOSIT,
                TransactionTypeEnum.FIAT_TO_CRYPTO_WITHDRAWAL -> {
                    transaction.mainAssetAmount?.roundToTwoDecimals() ?: 0.0
                }
                TransactionTypeEnum.FIAT_TO_FIAT_DEPOSIT,
                TransactionTypeEnum.FIAT_TO_FIAT_WITHDRAWAL -> {
                    transaction.mainFiatAmount?.roundToTwoDecimals() ?: 0.0
                }
                else -> {
                    Log.w(TAG, "Unknown transaction type: ${transaction.transactionType} for id=${transaction.id}")
                    transaction.amount.toDoubleOrNull()?.roundToTwoDecimals() ?: 0.0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating display amount for transaction ${transaction.id}: ${e.message}", e)
            0.0
        }
    }

    class PosTransactionDiffCallback : DiffUtil.ItemCallback<PosTransaction>() {
        override fun areItemsTheSame(oldItem: PosTransaction, newItem: PosTransaction): Boolean {
            val isSame = oldItem.id == newItem.id
            Log.v("POSTransactionAdapter", "areItemsTheSame: id=${oldItem.id}, isSame=$isSame")
            return isSame
        }

        override fun areContentsTheSame(oldItem: PosTransaction, newItem: PosTransaction): Boolean {
            val isSame = oldItem == newItem
            Log.v("POSTransactionAdapter", "areContentsTheSame: id=${oldItem.id}, isSame=$isSame")
            return isSame
        }
    }
}