package com.example.jonathangalvan.mirelex.Listeners

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.example.jonathangalvan.mirelex.R
import java.security.AccessControlContext


class SelectedItemsListener(val context: Context, var fm: FragmentManager){
    interface SelectedItemsListenerInterface {
        fun callback(tag: String, inputName: String, id: String, inputText: String)
        fun callbackClose(tag: String)
    }

    fun openTab(fragment: Fragment, inputName: String, listType: String, list: String){
        val b = Bundle()
        b.putString("inputName", inputName)
        b.putString("listType", listType)
        b.putString("list", list)
        b.putString("tag", "SelectedItemsFragment")
        fragment.arguments = b
        val transaction = fm.beginTransaction()
        transaction.setCustomAnimations(R.anim.abc_slide_in_bottom,  R.anim.abc_fade_out)
        transaction.replace(android.R.id.content, fragment, "SelectedItemsFragment")
        transaction.addToBackStack(null)
        transaction.commit()
    }
}