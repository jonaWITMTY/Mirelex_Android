package com.example.jonathangalvan.mirelex

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Fragments.*
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomBottomAlert
import com.example.jonathangalvan.mirelex.Interfaces.AndroidUpdateVersion
import com.example.jonathangalvan.mirelex.Interfaces.BottomAlertInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

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
                openTab(Notifications())
                return@OnNavigationItemSelectedListener true
            }
            R.id.storeNavigationProfile -> {
                supportActionBar?.title = resources.getString(R.string.storeTabProfile)
                openTab(Profile())
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

        /*Check for updates*/
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, "android-update.php")).enqueue(object:
                Callback {
                override fun onFailure(call: Call, e: IOException) {}

                override fun onResponse(call: Call, response: Response) {
                    val responseStr = response.body()?.string()
                    val repsonseObj = UtilsModel.getGson().fromJson(responseStr, AndroidUpdateVersion::class.java)
                    if(repsonseObj?.version!!.toDouble() > version.toDouble()){
                        val alertObj = UtilsModel.getGson().toJson(
                            BottomAlertInterface(
                                alertType = "newUpdateVersion"
                            )
                        )
                        CustomBottomAlert().bottomSheetDialogInstance(alertObj).show(supportFragmentManager, "alert")
                    }
                }
            })

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun openTab(fragment: androidx.fragment.app.Fragment){
        findViewById<FrameLayout>(R.id.contentTabsFrameLayout).removeAllViews()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.contentTabsFrameLayout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.store_tabs_icons, menu)
        menu?.getItem(0)?.icon?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(this, android.R.color.white))
        }
        menu?.getItem(1)?.icon?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(this, android.R.color.white))
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed(){}

    override fun onFragmentInteraction(uri: Uri) { }
}
