package com.example.jonathangalvan.mirelex.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jonathangalvan.mirelex.Interfaces.ProductInterface
import com.example.jonathangalvan.mirelex.R

class ProductViewHolder(view: View): RecyclerView.ViewHolder(view){
//    var productAdapterName = view.findViewById<TextView>(R.id.productAdapterName)
    var productAdapaterImage = view.findViewById<ImageView>(R.id.productAdapaterImage)
//    var productadapterPrice = view.findViewById<TextView>(R.id.productadapterPrice)
}

class ProductsAdapter(private var productsList: ArrayList<ProductInterface>): RecyclerView.Adapter<ProductViewHolder>(){
    override fun getItemCount(): Int {
        return if(productsList.isNotEmpty()) productsList.size else 0
    }

    fun loadNewData(newOrders: ArrayList<ProductInterface>){
        productsList = newOrders
        notifyDataSetChanged()
    }

    fun getProduct(position: Int): ProductInterface{
        return productsList[position]
    }

    override fun onBindViewHolder(p0: ProductViewHolder, p1: Int) {
//        p0.productAdapterName.text = "${p0.productAdapterName.context.resources.getText(R.string.size)}: ${productsList[p1].size}"
//        p0.productadapterPrice.text = productsList[p1].priceFormatted
        if(productsList[p1].productFeaturedImage != null){
            Glide.with(p0.productAdapaterImage.context).load(productsList[p1].productFeaturedImage).apply( RequestOptions().override(300, 0)).into(p0.productAdapaterImage)
        }else{
            Glide.with(p0.productAdapaterImage.context).load(R.drawable.mirelex_logo_cian).into(p0.productAdapaterImage)
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ProductViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.adapter_product, p0,false)
        return ProductViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}