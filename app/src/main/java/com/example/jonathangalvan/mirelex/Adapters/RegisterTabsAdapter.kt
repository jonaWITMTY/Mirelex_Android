package com.example.jonathangalvan.mirelex.Adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.jonathangalvan.mirelex.Fragments.Register.RegisterCustomerTab
import com.example.jonathangalvan.mirelex.Fragments.Register.RegisterStoreTab
import com.example.jonathangalvan.mirelex.R

class RegisterTabsAdapter(fm: FragmentManager, private var context: Context): FragmentPagerAdapter(fm) {

    override fun getItem(p0: Int): Fragment? {
        when (p0) {
            0 -> {
                return RegisterCustomerTab()
            }
            1 -> {
                return RegisterStoreTab()
            }
            else -> return null
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> {
                return context.resources.getString(R.string.customer)
            }
            1 -> {
                return context.resources.getString(R.string.storeAndDesign)
            }
            else -> return null
        }
    }
}