package com.thellex.pay.features.fiat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pay.data.model.IBankAccountDto
import com.thellex.pay.databinding.ItemPaymentMethodBinding

class PaymentMethodAdapter(
    private val onItemClicked: (IBankAccountDto) -> Unit
) : ListAdapter<IBankAccountDto, PaymentMethodAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemPaymentMethodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IBankAccountDto) = with(binding) {
            accountName.text = item.accountName ?: "Unnamed Account"
            accountNumber.text = item.accountNumber
            bankName.text = item.bankName

            root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentMethodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<IBankAccountDto>() {
        override fun areItemsTheSame(oldItem: IBankAccountDto, newItem: IBankAccountDto): Boolean =
            oldItem.accountNumber == newItem.accountNumber

        override fun areContentsTheSame(oldItem: IBankAccountDto, newItem: IBankAccountDto): Boolean =
            oldItem == newItem
    }
}
