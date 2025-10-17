package com.thellex.pay.features.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.thellex.pay.R
import com.thellex.pay.core.utils.ActivityTracker
import com.thellex.pay.databinding.ActivityQuickActionsBinding

class POSQuickActionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickActionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    if (navController.currentDestination?.id != R.id.dashboardFragment) {
                        navController.navigate(R.id.home_graph)
                    }
                    true
                }
                R.id.nav_pos -> {
                    // Ensure that the navigation from dashboardGraph to posGraph is smooth
                    if (navController.currentDestination?.id != R.id.posFragment) {
                        // Navigate to the pos_graph and make sure it doesn't pop the dashboard from the back stack
                        navController.navigate(
                            R.id.pos_graph, null, NavOptions.Builder()
                            .setPopUpTo(R.id.home_graph, false)
                            .build())
                    }
                    true
                }
                else -> false
            }
        }
        binding.bottomNav.selectedItemId = R.id.home_graph
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        if (navController.currentDestination?.id == R.id.posFragment) {
            if (!navController.navigateUp()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}