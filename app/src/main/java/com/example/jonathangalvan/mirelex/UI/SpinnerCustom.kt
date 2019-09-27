package com.example.jonathangalvan.mirelex.UI

import android.content.Context
import android.util.AttributeSet
import android.widget.AdapterView
import android.widget.Spinner
import java.lang.reflect.Array.setInt
import java.lang.reflect.AccessibleObject.setAccessible



class SpinnerCustom(context: Context, attrs: AttributeSet): Spinner(context, attrs) {
    private var listener: OnItemSelectedListener? = null

    override fun setSelection(position: Int, animate: Boolean) {
        ignoreOldSelectionByReflection()
        super.setSelection(position, animate)
    }

    private fun ignoreOldSelectionByReflection() {
        try {
            val c = this.javaClass.superclass!!.superclass!!.superclass
            val reqField = c!!.getDeclaredField("mOldSelectedPosition")
            reqField.isAccessible = true
            reqField.setInt(this, -1)
        } catch (e: Exception) {
            // TODO: handle exception
        }

    }

    override fun setSelection(position: Int) {
        ignoreOldSelectionByReflection()
        super.setSelection(position)
    }
}