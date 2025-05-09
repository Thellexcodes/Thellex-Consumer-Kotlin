package com.example.thellex

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
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
                R.id.nav_home -> {
                    if (navController.currentDestination?.id != R.id.nav_home) {
                        navController.navigate(R.id.nav_home)
                    }
                    true
                }
                R.id.nav_cash -> {
                    if (navController.currentDestination?.id != R.id.nav_cash) {
                        navController.navigate(R.id.nav_cash)
                    }
                    true
                }
                R.id.nav_pos -> {
                    if (navController.currentDestination?.id != R.id.nav_pos) {
                        navController.navigate(R.id.nav_pos)
                    }
                    true
                }
                else -> false
            }
        }

        binding.bottomNav.selectedItemId = R.id.nav_home
    }

}