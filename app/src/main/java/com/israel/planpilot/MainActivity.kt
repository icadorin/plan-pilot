package com.israel.planpilot

import android.content.Context
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
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
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

    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                navigateToLogin()
            } else {
                initializeViews()
                setupMainActivity()
                if (navController.currentDestination?.id != R.id.nav_home) {
                    navigateToHome()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun setupMainActivity() {
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

        setupNavigation()
        setupActionBarTitleListener()

        lifecycleScope.launch {
            createActivityCard.createCardsForCurrentDate()
        }

        toolbar.visibility = View.VISIBLE
    }

    private fun navigateToLogin() {
        val navController = findNavController(R.id.nav_host_fragment)
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()
        navController.navigate(R.id.loginFragment, null, options)
        toolbar.visibility = View.GONE
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.homeDrawerLayout)
        setSupportActionBar(toolbar)
    }

    private fun setupNavigation() {
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_cal_mon_large,
                R.id.nav_stretch_break,
                R.id.nav_activity_frequency,
                R.id.fragmentAddActivity,
                R.id.fragmentActivitiesList
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)

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
                navigateToHome()
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
            R.id.nav_logout -> {
                logoutUser()
                drawerLayout.closeDrawers()
                return true
            }
            else -> return false
        }
    }

    private fun setupActionBarTitleListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.apply {
                title = when (destination.id) {
                    R.id.nav_home -> "Acompanhar Atividades"
                    R.id.nav_cal_mon_large -> "Calendário Mensal"
                    R.id.nav_stretch_break -> "Intervalo Ativo"
                    R.id.nav_activity_frequency -> "Controle de Atividade"
                    else -> "Plan Pilot"
                }
            }
        }
    }

    private fun logoutUser() {
        auth.signOut()
        navigateToLogin()
    }

    private fun navigateToHome() {
        navController.navigate(R.id.nav_home)
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
        navController.navigate(R.id.fragmentAddActivity)
    }

    private fun showFragmentActivitiesList() {
        navController.navigate(R.id.fragmentActivitiesList)
    }
}
