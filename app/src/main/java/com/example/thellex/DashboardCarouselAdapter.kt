package com.example.thellex

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter

class DashboardCarouselAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    private val fragments = listOf(
        WalletCardFragment(),
//        DollarCardFragment()
    )

    // Return the number of pages (fragments) in the adapter
    override fun getItemCount(): Int = fragments.size

    // Return the fragment for each position in the ViewPager2
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
