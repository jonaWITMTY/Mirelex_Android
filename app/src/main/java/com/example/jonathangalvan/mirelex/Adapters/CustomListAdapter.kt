package com.example.jonathangalvan.mirelex.Adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.jonathangalvan.mirelex.Interfaces.ProductInterface

open class CustomListAdapter(private var context: Context, private var productsList: ArrayList<ProductInterface>): BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        return convertView
    }

    override fun getItem(position: Int): Any {
        return productsList[position]
    }

    override fun getItemId(position: Int): Long {
        return productsList[position].productId!!
    }

    override fun getCount(): Int {
        return productsList.size
    }
}