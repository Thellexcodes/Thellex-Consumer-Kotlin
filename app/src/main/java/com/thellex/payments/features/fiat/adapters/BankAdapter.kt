package com.thellex.payments.features.fiat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R

// RecyclerView Adapter for Banks
class BankAdapter(
    private var banks: List<String>,
    private val onBankSelected: (String) -> Unit
) : RecyclerView.Adapter<BankAdapter.BankViewHolder>() {

    inner class BankViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bankNameTextView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_bank, parent, false)
        return BankViewHolder(view)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val bank = banks[position]
        holder.bankNameTextView.text = bank
        holder.itemView.setOnClickListener { onBankSelected(bank) }
    }

    override fun getItemCount(): Int = banks.size

    fun updateBanks(newBanks: List<String>) {
        banks = newBanks
        notifyDataSetChanged()
    }
}