package com.thellex.payments.features.fiat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.annotations.SerializedName
import com.thellex.payments.databinding.ItemBankBinding
import java.io.Serializable

class BankSearchAdapter(
    private var banks: List<NGBankDto>,
    private val onBankSelected: (NGBankDto) -> Unit
) : RecyclerView.Adapter<BankSearchAdapter.BankViewHolder>() {

    private var filteredBanks: List<NGBankDto> = banks

    class BankViewHolder(val binding: ItemBankBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val binding = ItemBankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val bank = filteredBanks[position]
        with(holder.binding) {
            bankName.text = bank.name
            Glide.with(bankIcon.context)
                .load(bank.logo)
                .into(bankIcon)

            root.setOnClickListener {
                onBankSelected(bank)
            }
        }
    }

    override fun getItemCount(): Int = filteredBanks.size

    fun filter(query: String) {
        filteredBanks = banks.filter {
            it.name.contains(query, ignoreCase = true)
        }
        notifyDataSetChanged()
    }

    fun updateData(newBanks: List<NGBankDto>) {
        banks = newBanks
        filteredBanks = newBanks
        notifyDataSetChanged()
    }
}


@kotlinx.serialization.Serializable
data class NGBankDto(
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("code") val code: String,
    @SerializedName("ussd") val ussd: String,
    @SerializedName("logo") val logo: String
) : Serializable
