package com.example.jonathangalvan.mirelex.Listeners

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.jonathangalvan.mirelex.R
import java.security.AccessControlContext


class SelectedItemsListener(val context: Context, var fm: androidx.fragment.app.FragmentManager){
    interface SelectedItemsListenerInterface {
        fun callback(tag: String, inputName: String, id: String, inputText: String, obj: Any)
        fun callbackClose(tag: String)
    }

    fun openTab(fragment: androidx.fragment.app.Fragment, inputName: String, listType: String, list: String){
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