package com.israel.planpilot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupNavigation()
        setupActionBarTitleListener()
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.homeDrawerLayout)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        supportActionBar?.let {
            it.title = "Undefined"
        }
    }

    private fun setupNavigation() {
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_login), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.nav_home)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_login -> {
                    navController.navigate(R.id.nav_login)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_calendar -> {
                    navController.navigate(R.id.nav_calendar)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupActionBarTitleListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = when (destination.id) {
                R.id.nav_home -> "Home"
                R.id.nav_login -> "Login"
                R.id.nav_calendar -> "Calendar"

                else -> "Não encontrado"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}



