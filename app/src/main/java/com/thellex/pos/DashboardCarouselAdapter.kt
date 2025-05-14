package com.thellex.pos

import androidx.fragment.app.Fragment
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
