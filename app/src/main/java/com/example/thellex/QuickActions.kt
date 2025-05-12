package com.example.thellex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.thellex.databinding.ActivityQuickActionsBinding

class QuickActions : AppCompatActivity() {

    private lateinit var binding: ActivityQuickActionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    if (navController.currentDestination?.id != R.id.dashboardFragment) {
                        // Navigate to the dashboard fragment
                        navController.navigate(R.id.home_graph)
                    }
                    true
                }
                R.id.nav_pos -> {
                    // Ensure that the navigation from dashboardGraph to posGraph is smooth
                    if (navController.currentDestination?.id != R.id.posFragment) {
                        // Navigate to the pos_graph and make sure it doesn't pop the dashboard from the back stack
                        navController.navigate(R.id.pos_graph, null, NavOptions.Builder()
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
            // If we're on the posFragment, navigate up to the dashboard
            if (!navController.navigateUp()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}