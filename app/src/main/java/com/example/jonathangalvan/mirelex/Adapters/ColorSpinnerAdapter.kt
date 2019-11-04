package com.example.jonathangalvan.mirelex.Adapters

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.productColorCatalog
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.UI.MultiSpinnerCustom


class ColorSpinnerAdapter(private var context: Context, private var colorArr: ArrayList<productColorCatalog>, private var spinner: MultiSpinnerCustom, private var selected: BooleanArray?): BaseAdapter() {

    override fun getItem(position: Int): Any {
        return colorArr[position]
    }

    override fun getCount(): Int {
        return colorArr.size
    }

    override fun getItemId(position: Int): Long {
        return colorArr[position].productColorCatalogId!!.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.view_spinner_item_color, null)
        var color: Int = try {
            Color.parseColor(colorArr[position].hex)
        } catch (e: IllegalArgumentException) {
            Color.parseColor("#E53935")
        }

        val colorCheckBox = view.findViewById<CheckBox>(R.id.spinnerItemColor)

        /*Set value color*/
        colorCheckBox.text = colorArr[position].name

        /*Checked if selected*/
        if(selected!![position]){
            colorCheckBox.isChecked = true
        }

        /*Set existing color*/
        view.findViewById<View>(R.id.spinnerItemColorHex).setBackgroundColor(color)

        /*Create checked listener*/
        colorCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            spinner.selectItem(position, isChecked)
        }

        return view
    }
}