package com.example.jonathangalvan.mirelex.Adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Interfaces.ProductInterface
import com.example.jonathangalvan.mirelex.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_product.view.*

class ProductsAdapter(private var context: Context, private var productsList: ArrayList<ProductInterface>): CustomListAdapter(context, productsList){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        if(convertView == null){
            view = View.inflate(context, R.layout.adapter_product, null)
            view.productAdapterName.text = "${context.resources.getText(R.string.size)}: ${productsList[position].size}"
            view.productadapterPrice.text = productsList[position].priceFormatted
            Picasso.with(view.productAdapaterImage.context).load(productsList[position].productFeaturedImage).into(view.productAdapaterImage)
        }else{
            view = convertView
        }
        return view
    }
}