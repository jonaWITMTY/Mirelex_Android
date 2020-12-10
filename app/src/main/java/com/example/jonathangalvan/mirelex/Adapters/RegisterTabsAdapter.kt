package com.example.jonathangalvan.mirelex.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.jonathangalvan.mirelex.Fragments.Register.RegisterCustomerTab
import com.example.jonathangalvan.mirelex.Fragments.Register.RegisterStoreTab
import com.example.jonathangalvan.mirelex.R

class RegisterTabsAdapter(fm: androidx.fragment.app.FragmentManager, private var context: Context): androidx.fragment.app.FragmentPagerAdapter(fm) {

    override fun getItem(p0: Int): androidx.fragment.app.Fragment? {
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