package com.thellex.payments.features.auth.viewModel

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.thellex.payments.features.auth.ui.BasicKycReviewFragment
import com.thellex.payments.features.auth.ui.BasicKycStep1Fragment
import com.thellex.payments.features.auth.ui.BasicKycStep2Fragment

class BasicKycPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> BasicKycStep1Fragment()
        1 -> BasicKycStep2Fragment()
        2 -> BasicKycReviewFragment()
        else -> throw IllegalStateException("Invalid step")
    }
}