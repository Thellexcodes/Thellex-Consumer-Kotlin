package com.thellex.payments.features.notifications.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R
import com.thellex.payments.data.model.NotificationKindEnum
import com.thellex.payments.databinding.ItemSortOptionBinding

class SortOptionsAdapter(
    private val items: List<Pair<String, NotificationKindEnum?>>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<SortOptionsAdapter.SortViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortViewHolder {
        val binding = ItemSortOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SortViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SortViewHolder, position: Int) {
        val (displayText, _) = items[position]
        holder.bind(displayText, position == selectedPosition)

        holder.binding.root.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition != selectedPosition) {
                val previousPosition = selectedPosition
                selectedPosition = currentPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                val selectedText = items[selectedPosition].first
                onSelected(selectedText)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class SortViewHolder(val binding: ItemSortOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String, isSelected: Boolean) {
            binding.sortOptionText.text = text
            val backgroundRes = if (isSelected) {
                R.drawable.bg_sort_option_selected
            } else {
                R.drawable.bg_sort_option_unselected
            }
            binding.sortOptionText.setBackgroundResource(backgroundRes)
        }
    }
}


