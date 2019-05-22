package com.example.jonathangalvan.mirelex

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import com.example.jonathangalvan.mirelex.Fragments.Orders
import com.example.jonathangalvan.mirelex.Fragments.Products
import com.example.jonathangalvan.mirelex.Fragments.Services
import kotlinx.android.synthetic.main.activity_customer_tabs.*

class CustomerTabsActivity : AppCompatActivity(),
    Products.OnFragmentInteractionListener,
    Services.OnFragmentInteractionListener,
    Orders.OnFragmentInteractionListener{

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.storeNavigationProducts -> {
                supportActionBar?.title = resources.getString(R.string.storeTabProducts)
                openTab(Products())
                return@OnNavigationItemSelectedListener true
            }
            R.id.storeNavigationServices -> {
                supportActionBar?.title = resources.getString(R.string.services)
                openTab(Services())
                return@OnNavigationItemSelectedListener true
            }
            R.id.storeNavigationOrders -> {
                supportActionBar?.title = resources.getString(R.string.storeTabOrders)
                openTab(Orders())
                return@OnNavigationItemSelectedListener true
            }
            R.id.storeNavigationNotifications -> {
                supportActionBar?.title = resources.getString(R.string.storeTabNotifications)
                return@OnNavigationItemSelectedListener true
            }
            R.id.storeNavigationProfile -> {
                supportActionBar?.title = resources.getString(R.string.storeTabProfile)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_tabs)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        supportActionBar?.title = resources.getString(R.string.storeTabProducts)
        openTab(Products())
    }

    fun openTab(fragment: Fragment){
        findViewById<FrameLayout>(R.id.customerTabsFrameLayout).removeAllViews()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.customerTabsFrameLayout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed(){}

    override fun onFragmentInteraction(uri: Uri) {

    }
}
