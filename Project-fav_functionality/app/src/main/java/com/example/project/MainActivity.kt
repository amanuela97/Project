package com.example.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import com.example.project.utility.GlobalObject
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val SEARCH_RADIUS_INTENT = 0
    }
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //action bar for fragments
//        setupActionBarWithNavController( findNavController(R.id.fragment))

        setUpSharedPreferences()
        setUpDrawer()

//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.search_radius -> {
                    navigateToSearchRadiusActivity()
                    true
                }
                R.id.favourites ->{
                    navigateToFavouritesActivity()
                    true
                }
                R.id.weather -> {
                    navigateToWeatherActivity()
                    true
                }
                else -> false
            }
        }
    }

    //to make back button works on fragments
    override fun onSupportNavigateUp(): Boolean {
        val navControl = findNavController(R.id.fragment)
        return navControl.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpSharedPreferences() {
        val sharedPreferences = getSharedPreferences(resources.getString(R.string.app_settings_key), Context.MODE_PRIVATE) ?: return
        val searchRadius = sharedPreferences.getInt(resources.getString(R.string.search_radius_key), resources.getInteger(R.integer.default_search_radius))
        GlobalObject.SEARCH_RADIUS = searchRadius
    }

    private fun setUpDrawer() {
        toggle = ActionBarDrawerToggle(this, drawer_layout, R.string.drawer_open, R.string.drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }

    fun toggleDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    private fun navigateToSearchRadiusActivity() {
        val intent = Intent(this, SearchRadiusActivity::class.java)
        startActivityForResult(intent, SEARCH_RADIUS_INTENT)
    }

    private fun navigateToFavouritesActivity() {
        val intent = Intent(this, FavouritesActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToWeatherActivity() {
        val intent = Intent(this, WeatherActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SEARCH_RADIUS_INTENT) {
            val fragmentManager = supportFragmentManager
            val navHostFragment = fragmentManager.findFragmentById(R.id.fragment)
            val mapFragment = navHostFragment?.childFragmentManager?.fragments?.get(0) as MapsFragment
            mapFragment.getDeviceLocation()
        }
    }
}