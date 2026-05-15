package com.madhusiri.app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.madhusiri.app.services.AlertService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enforce native full-bleed OS System UI window drawing layouts
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val sharedPrefs = getSharedPreferences("MadhuSiriPrefs", Context.MODE_PRIVATE)
        val userRole = sharedPrefs.getString("USER_ROLE", "BEEKEEPER")

        // Inflate dynamic bottom navigation mapping structures contextually
        navView.menu.clear()
        if (userRole == "FARMER") {
            navView.inflateMenu(R.menu.farmer_nav_menu)
        } else {
            navView.inflateMenu(R.menu.bottom_nav_menu)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        if (userRole == "FARMER") {
            navGraph.setStartDestination(R.id.navigation_farmer_dashboard)
        } else {
            navGraph.setStartDestination(R.id.navigation_beekeeper_dashboard)
        }
        navController.graph = navGraph

        navView.setupWithNavController(navController)

        // Prevent double layout shifts or broken listeners when navigating state groups
        navView.setOnItemReselectedListener { 
            // Purposely do nothing to avoid resetting stack layouts
        }

        if (userRole == "FARMER") {
            val serviceIntent = Intent(this, AlertService::class.java)
            startService(serviceIntent)
        }
    }
}
