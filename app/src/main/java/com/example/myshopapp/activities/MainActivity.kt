package com.example.myshopapp.activities

import OrdersFragment
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.myshopapp.R
import com.example.myshopapp.fragments.CartFragment
import com.example.myshopapp.fragments.FavoritesFragment
import com.example.myshopapp.fragments.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.*

class MainActivity : AppCompatActivity() {
    // Declare variables for UI elements and shared preferences
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rightNavigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var darkModeSwitch: SwitchMaterial
    private lateinit var languageSwitch: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userOrders: TextView
    private lateinit var logOut: TextView
    private var currentFragment: Fragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("myshopapp_preferences", Context.MODE_PRIVATE)
// Initialize shared preference
        if (sharedPreferences.getBoolean("dark_mode", false)) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }

        setAppLocale(sharedPreferences.getString("language", "el") ?: "el")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        rightNavigationView = findViewById(R.id.navigation_view_right)
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar)

        // Set up drawer and bottom navigation
        setupDrawer()
        setupBottomNavigation()

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_home
            replaceFragment(HomeFragment())
        } else {
            currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            updateBottomNavigationSelection()
        }




    }

    // Setup the drawer and its toggle
    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupRightNavigationContent()
    }
    // Setup bottom navigation and handle item selection
    private fun setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    if (!isCurrentFragment(HomeFragment::class.java)) {
                        replaceFragment(HomeFragment())
                        true
                    } else {
                        false
                    }
                }
                R.id.navigation_favorites -> {
                    if (!isCurrentFragment(FavoritesFragment::class.java)) {
                        replaceFragment(FavoritesFragment())
                        true
                    } else {
                        false
                    }
                }
                R.id.navigation_cart -> {
                    if (!isCurrentFragment(CartFragment::class.java)) {
                        replaceFragment(CartFragment())
                        true
                    } else {
                        false
                    }
                }
                R.id.navigation_profile -> {
                    drawerLayout.openDrawer(GravityCompat.END)
                    true
                }
                else -> false
            }
        }
    }
    // Replace the current fragment with a new one
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            if (!fragment.isAdded) {
                addToBackStack(null)
            }
            commit()
        }
        currentFragment = fragment
        updateBottomNavigationSelection()
    }
    // Check if the current fragment is of the specified class
    private fun isCurrentFragment(fragmentClass: Class<out Fragment>): Boolean {
        return currentFragment?.javaClass == fragmentClass
    }

    private fun updateBottomNavigationSelection() {
        when (currentFragment) {
            is HomeFragment -> bottomNavigationView.menu.findItem(R.id.navigation_home)?.isChecked = true
            is FavoritesFragment -> bottomNavigationView.menu.findItem(R.id.navigation_favorites)?.isChecked = true
        }
    }
    // Setup the content of the right navigation drawer
    private fun setupRightNavigationContent() {

        val headerView = rightNavigationView.getHeaderView(0)
        darkModeSwitch = headerView.findViewById(R.id.switch_dark_mode)
        languageSwitch = headerView.findViewById(R.id.switch_language)
        userOrders = headerView.findViewById(R.id.tvOrders)
        logOut = headerView.findViewById(R.id.tvLogOut)

        darkModeSwitch.isChecked = sharedPreferences.getBoolean("dark_mode", false)
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
            recreate()
        }

        languageSwitch.isChecked = sharedPreferences.getString("language", "el") == "en"
        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val newLanguage = if (isChecked) "en" else "el"
            sharedPreferences.edit().putString("language", newLanguage).apply()
            setAppLocale(newLanguage)
            recreate()
        }


        userOrders.setOnClickListener {
            if (!isCurrentFragment(OrdersFragment::class.java)) {
                replaceFragment(OrdersFragment())
                true
            } else {
                false
            }
        }


        logOut.setOnClickListener {
            showLogoutConfirmationDialog()
        }


    }
    // Show a confirmation dialog for logout
    private fun showLogoutConfirmationDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.log_out)
            .setMessage(R.string.sure_log_out)
            .setPositiveButton(R.string.log_out) { dialog, _ ->
                // Clear user session data
                sharedPreferences.edit().clear().apply()

                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Close MainActivity go to LoginPage

                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    // Handle back button press
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
// Set the application locale based on the selected language
    private fun setAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
