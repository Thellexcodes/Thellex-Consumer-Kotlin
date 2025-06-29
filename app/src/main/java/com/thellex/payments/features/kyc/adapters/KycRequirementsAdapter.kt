package com.thellex.payments.features.kyc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R

class KycRequirementsAdapter(
    private val requirements: List<String>
) : RecyclerView.Adapter<KycRequirementsAdapter.RequirementViewHolder>() {

    inner class RequirementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivRequirementIcon)
        val text: TextView = view.findViewById(R.id.tvRequirementText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequirementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kyc_requrement, parent, false)
        return RequirementViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequirementViewHolder, position: Int) {
        val reqText = requirements[position]
        holder.text.text = reqText
//        holder.icon.setImageResource(getIconForRequirement(reqText))
    }

    override fun getItemCount(): Int = requirements.size
}
