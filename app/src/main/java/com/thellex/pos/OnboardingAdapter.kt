package com.thellex.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(private val slides: List<OnboardItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardViewHolder>() {

    inner class OnboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById<ImageView>(R.id.onboardImage)
        val title: TextView = view.findViewById<TextView>(R.id.onboardTitle)
        val desc: TextView = view.findViewById<TextView>(R.id.onboardDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        return OnboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardViewHolder, position: Int) {
        val item = slides[position]
        holder.image.setImageResource(item.imageRes)
        holder.title.text = item.title
        holder.desc.text = item.description
    }

    override fun getItemCount(): Int = slides.size
}

data class OnboardItem(val imageRes: Int, val title: String, val description: String)
