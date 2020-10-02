package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        val SEARCH_RADIUS_INTENT = "searchRadius"
    }
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //action bar for fragments
//        setupActionBarWithNavController( findNavController(R.id.fragment))

        setUpDrawer()

//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nav_view.setNavigationItemSelectedListener {
            println(it)
            when (it.itemId) {
                R.id.search_radius -> {
                    navigateToSearchRadiusActivity()
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
        startActivity(intent);
    }
}