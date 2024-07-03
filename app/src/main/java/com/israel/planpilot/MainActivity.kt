package com.israel.planpilot

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import com.israel.planpilot.activity.CreateActivityFragment
import com.israel.planpilot.activity.ListAllActivitiesFragment
import com.israel.planpilot.card.CreateActivityCard
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var toolbar: Toolbar
    private lateinit var btnReturnToToday: ImageButton
    private lateinit var createActivityCard: CreateActivityCard
    lateinit var btnActivitiesList: ImageButton
    lateinit var btnAddActivity: ImageButton

    override fun onResume() {
        super.onResume()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(toolbar)

        createActivityCard = CreateActivityCard()

        btnActivitiesList = toolbar.findViewById(R.id.btnActivitiesList)
        btnAddActivity = toolbar.findViewById(R.id.btnAddActivity)
        btnReturnToToday = toolbar.findViewById(R.id.btnReturnToToday)
        btnReturnToToday.visibility = View.GONE

        btnActivitiesList.setOnClickListener {
            showFragmentActivitiesList()
        }

        btnAddActivity.setOnClickListener {
            showAddActivityFragment()
        }

        initializeViews()
        setupNavigation()
        setupActionBarTitleListener()

        lifecycleScope.launch {
            createActivityCard.createCardsForCurrentDate()
        }
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
                R.id.nav_cal_mon_large,
                R.id.nav_stretch_break,
                R.id.nav_activity_frequency
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
                hideKeyboard()
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
            R.id.nav_cal_mon_large -> {
                navController.navigate(R.id.nav_cal_mon_large)
                drawerLayout.closeDrawers()
                return true
            }
            R.id.nav_stretch_break -> {
                navController.navigate(R.id.nav_stretch_break)
                drawerLayout.closeDrawers()
                return true
            }
            R.id.nav_activity_frequency -> {
                navController.navigate(R.id.nav_activity_frequency)
                drawerLayout.closeDrawers()
                return true
            }
            else -> return false
        }
    }

    private fun setupActionBarTitleListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = when (destination.id) {
                R.id.nav_home -> "Acompanhar Atividades"
                R.id.nav_cal_mon_large -> "Calendário Mensal"
                R.id.nav_stretch_break -> "Intervalo Ativo"
                R.id.nav_activity_frequency -> "Controle de Atividade"

                else -> "Plan Pilot"
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

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun showAddActivityFragment() {
        val fragment = CreateActivityFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        supportActionBar?.title = "Criar atividade"
    }

    private fun showFragmentActivitiesList() {
        val fragment = ListAllActivitiesFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        supportActionBar?.title = "Todas atividades"
    }
}
