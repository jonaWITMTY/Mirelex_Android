package com.example.jonathangalvan.mirelex.Adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.jonathangalvan.mirelex.Fragments.Register.RegisterCustomerTab
import com.example.jonathangalvan.mirelex.Fragments.Register.RegisterStoreTab

class RegisterTabsAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

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
                return "Particular"
            }
            1 -> {
                return "Tienda"
            }
            else -> return null
        }
    }
}