package com.thellex.pay.features.pos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thellex.pay.core.utils.Helpers.setScaledImage
import com.thellex.pay.databinding.ItemOnboardingBinding

class OnboardingAdapter(private val slides: List<OnboardItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardViewHolder>() {

    inner class OnboardViewHolder(val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardViewHolder {
        val binding = ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardViewHolder, position: Int) {
        val item = slides[position]
        // Dynamically scale image to fit width and maintain aspect ratio
        holder.binding.onboardImage.setScaledImage(item.imageRes)

        // Set title and description
        holder.binding.onboardTitle.text = item.title
        holder.binding.onboardDesc.text = item.description
    }


    override fun getItemCount(): Int = slides.size
}


data class OnboardItem(val imageRes: Int, val title: String, val description: String)
