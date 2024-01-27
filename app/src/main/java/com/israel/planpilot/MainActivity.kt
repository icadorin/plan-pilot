package com.israel.planpilot

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
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
    private lateinit var toolbar: Toolbar
    private lateinit var btnReturnToToday: ImageButton

    override fun onResume() {
        super.onResume()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(toolbar)

        btnReturnToToday = toolbar.findViewById(R.id.btnReturnToToday)
        btnReturnToToday.visibility = View.GONE

        initializeViews()
        setupNavigation()
        setupActionBarTitleListener()
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.homeDrawerLayout)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController


        toolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupNavigation() {
        setupActionBar()
        setupDrawerMenu()
    }

    private fun setupActionBar() {
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_login,
                R.id.nav_cal_mon_small,
                R.id.nav_cal_week,
                R.id.nav_cal_mon_large
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupDrawerMenu() {
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        toggle.isDrawerIndicatorEnabled = false

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)
            }

            override fun onDrawerClosed(drawerView: View) {
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)
            }
        })

        toggle.syncState()

        toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem)
        }
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_home -> {
                navController.navigate(R.id.nav_home)
                drawerLayout.closeDrawers()
                return true
            }
            R.id.nav_login -> {
                navController.navigate(R.id.nav_login)
                drawerLayout.closeDrawers()
                return true
            }
            R.id.nav_cal_mon_small -> {
                navController.navigate(R.id.nav_cal_mon_small)
                drawerLayout.closeDrawers()
                return true
            }
            R.id.nav_cal_week -> {
                navController.navigate(R.id.nav_cal_week)
                drawerLayout.closeDrawers()
                return true
            }
            R.id.nav_cal_mon_large -> {
                navController.navigate(R.id.nav_cal_mon_large)
                drawerLayout.closeDrawers()
                return true
            }
            else -> return false
        }
    }

    private fun setupActionBarTitleListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = when (destination.id) {
                R.id.nav_home -> "Home"
                R.id.nav_login -> "Login"
                R.id.nav_cal_mon_small -> "Cal. Mensal"
                R.id.nav_cal_week -> "Cal. Semanal"
                R.id.nav_cal_mon_large -> "Cal. Mensal Full Screen"
                R.id.fragmentAddActivity -> "Criar Atividade"

                else -> "Não encontrado"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setActionBarIcon(iconResource: Int) {
        supportActionBar?.setHomeAsUpIndicator(iconResource)
    }

    fun showReturnToTodayButton() {
        btnReturnToToday.visibility = View.VISIBLE
    }

    fun hideReturnToTodayButton() {
        btnReturnToToday.visibility = View.GONE
    }
}
