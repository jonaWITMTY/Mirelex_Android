package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Fragments.*

class StoreTabsActivity : AppCompatActivity(),
Profile.OnFragmentInteractionListener,
Products.OnFragmentInteractionListener,
Services.OnFragmentInteractionListener,
Orders.OnFragmentInteractionListener,
Notifications.OnFragmentInteractionListener{

    private lateinit var textMessage: TextView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.storeNavigationProducts -> {
                supportActionBar?.title = resources.getString(R.string.storeTabProducts)
                openTab(Products())
                findViewById<View>(R.id.storeProductAdd).visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.storeNavigationServices -> {
                supportActionBar?.title = resources.getString(R.string.services)
                openTab(Services())
                findViewById<View>(R.id.storeProductAdd).visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
            R.id.storeNavigationOrders -> {
                supportActionBar?.title = resources.getString(R.string.storeTabOrders)
                openTab(Orders())
                findViewById<View>(R.id.storeProductAdd).visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
            R.id.storeNavigationNotifications -> {
                supportActionBar?.title = resources.getString(R.string.storeTabNotifications)
                openTab(Notifications())
                findViewById<View>(R.id.storeProductAdd).visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
            R.id.storeNavigationProfile -> {
                supportActionBar?.title = resources.getString(R.string.storeTabProfile)
                openTab(Profile())
                findViewById<View>(R.id.storeProductAdd).visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_tabs)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportActionBar?.title = resources.getString(R.string.storeTabProducts)
        openTab(Products())
    }

    fun openTab(fragment: Fragment){
        findViewById<FrameLayout>(R.id.storeTabsFrameLayout).removeAllViews()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.storeTabsFrameLayout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.store_tabs_icons, menu)
        menu?.getItem(0)?.icon?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(this, android.R.color.white))
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.storeProductAdd ->{
                startActivity(Intent(this, ProductActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed(){}

    override fun onFragmentInteraction(uri: Uri) { }
}
