package com.example.thellex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
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
                    if (navController.currentDestination?.id != R.id.nav_dashboard) {
                        navController.navigate(R.id.dashboardFragment)
                    }
                    true
                }
                R.id.nav_pos -> {
                    if (navController.currentDestination?.id != R.id.nav_pos) {
                        navController.navigate(R.id.posFragment, null, NavOptions.Builder()
                            .setPopUpTo(R.id.posFragment, false)
                            .build())
                    }
                    true
                }
                else -> false
            }
        }
        binding.bottomNav.selectedItemId = R.id.nav_dashboard
    }
}