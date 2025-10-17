package com.thellex.pay.features.fiat.adapters

import android.os.Build
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pay.core.utils.Helpers
import com.thellex.pay.core.utils.Helpers.toTwoDecimalString
import com.thellex.pay.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.pay.data.model.PaymentStatusEnum
import com.thellex.pay.databinding.RampItemTransactionBinding
import java.time.Instant

class RampTransactionsAdapter :
    RecyclerView.Adapter<RampTransactionsAdapter.TransactionViewHolder>() {

    private val transactions = mutableListOf<IFiatCryptoRampTransactionsDto>()

    fun submitList(list: List<IFiatCryptoRampTransactionsDto>) {
        transactions.clear()
        transactions.addAll(list.sortedByDescending { it.createdAt })
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(
        val binding: RampItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var countdownTimer: CountDownTimer? = null

        fun cancelTimer() {
            countdownTimer?.cancel()
            countdownTimer = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = RampItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tx = transactions[position]
        val b = holder.binding
        holder.cancelTimer()

        b.transactionDescription.text =
            "${Helpers.getTransactionAction(tx.transactionType)} ${tx.recipientInfo.assetCode.uppercase()}"
        b.transactionTime.text = tx.expiresAt?.let { Helpers.formatTimestamp(it) } ?: "--"

        b.transactionFiatAmount.text =
            "${tx.netFiatAmount.toTwoDecimalString()} NGN"
        b.transactionCryptoAmount.text =
            "${tx.netCryptoAmount.toTwoDecimalString()} ${tx.recipientInfo.assetCode.uppercase()}"
        b.transactionTypeIcon.setImageResource(
            Helpers.getIconResIdForToken(tx.recipientInfo.assetCode)
        )

        val statusText = tx.paymentStatus.name
            .replace("_", " ")
            .lowercase()
            .replaceFirstChar { it.uppercase() }
        b.transactionStatusText.text = statusText.uppercase()
        b.transactionStatusText.setTextColor(
            Helpers.getStatusColor(b.root.context, tx.paymentStatus)
        )

        if (tx.paymentStatus == PaymentStatusEnum.Processing) {
            try {
                val expiryMillis = Instant.parse(tx.expiresAt).toEpochMilli()
                val currentMillis = System.currentTimeMillis()
                val remaining = expiryMillis - currentMillis

                if (remaining > 0) {
                    holder.countdownTimer = object : CountDownTimer(remaining, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val minutes = (millisUntilFinished / 1000) / 60
                            val seconds = (millisUntilFinished / 1000) % 60
                            b.transactionExpiry.text =
                                "EXPIRES IN ${minutes}:${seconds.toString().padStart(2, '0')}"
                        }

                        override fun onFinish() {
                            b.transactionExpiry.text = "EXPIRED"
                        }
                    }.start()
                } else {
                    b.transactionExpiry.text = "EXPIRED"
                }
            } catch (e: Exception) {
                b.transactionExpiry.text = statusText
            }
        } else {
            b.transactionExpiry.text = statusText
        }
    }

    override fun getItemCount(): Int = transactions.size

    override fun onViewRecycled(holder: TransactionViewHolder) {
        super.onViewRecycled(holder)
        holder.cancelTimer()
    }
}