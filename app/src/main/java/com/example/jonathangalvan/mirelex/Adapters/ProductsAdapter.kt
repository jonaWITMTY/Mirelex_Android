package com.example.jonathangalvan.mirelex.Adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Interfaces.ProductInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ProductActivity
import com.example.jonathangalvan.mirelex.ProductDetailActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.createProductFavorite
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ProductViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view){
//    var productAdapterName = view.findViewById<TextView>(R.id.productAdapterName)
    var productAdapaterImage = view.findViewById<ImageView>(R.id.productAdapaterImage)
//    var productadapterPrice = view.findViewById<TextView>(R.id.productadapterPrice)
    var productAdapterWishlist = view.findViewById<ImageView>(R.id.productAdapterWishlist)
}

class ProductsAdapter(private var productsList: ArrayList<ProductInterface>): androidx.recyclerview.widget.RecyclerView.Adapter<ProductViewHolder>(){
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

        if(productsList[p1].isFavorite == "0"){
            p0.productAdapterWishlist.setColorFilter(ContextCompat.getColor(p0.productAdapterWishlist.context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
        }else{
            p0.productAdapterWishlist.setColorFilter(ContextCompat.getColor(p0.productAdapterWishlist.context, R.color.colorPinkRed), android.graphics.PorterDuff.Mode.SRC_IN)
        }

        if(
            SessionModel(p0.productAdapaterImage.context).getUser().person?.userTypeId == UserType.Store.userTypeId ||
            (SessionModel(p0.productAdapaterImage.context).getUser().person?.userId == productsList[p1].userId && SessionModel(p0.productAdapaterImage.context).getUser().person?.userTypeId == UserType.Customer.userTypeId)
        ){
            p0.productAdapterWishlist.visibility = View.GONE
        }

        p0.productAdapaterImage.setOnClickListener(View.OnClickListener {
            val goToProductDetail: Intent
            when(SessionModel(p0.productAdapaterImage.context).getSessionUserType()){
                UserType.Store.userTypeId -> {
                    goToProductDetail = Intent(p0.productAdapaterImage.context, ProductActivity::class.java)
                }
                else -> {
                    goToProductDetail = Intent(p0.productAdapaterImage.context, ProductDetailActivity::class.java)
                }
            }
            val b = Bundle()
            b.putString("productId", productsList[p1].productId.toString())
            goToProductDetail.putExtras(b)
            p0.productAdapaterImage.context.startActivity(goToProductDetail)
        })

        p0.productAdapterWishlist.setOnClickListener(View.OnClickListener {
            val imgHeart: ImageView = it as ImageView
            if(productsList[p1].isFavorite == "0"){
                val reqObj = UtilsModel.getGson().toJson(createProductFavorite(productsList[p1].productId.toString()))
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(imgHeart.context, imgHeart.context.resources.getString(R.string.createProductFavorite), reqObj)).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) { }

                    override fun onResponse(call: Call, response: Response) {
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getPostResponse(imgHeart.context, responseStr)
                        if(responseObj.status == "success") {
                            productsList[p1].isFavorite = "1"
                            imgHeart.setColorFilter(ContextCompat.getColor(imgHeart.context, R.color.colorPinkRed), android.graphics.PorterDuff.Mode.SRC_IN)
                        }
                    }
                })

            }else{
                val reqObj = UtilsModel.getGson().toJson(createProductFavorite(productsList[p1].productId.toString()))
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(imgHeart.context, imgHeart.context.resources.getString(R.string.deleteProductFavorite), reqObj)).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) { }

                    override fun onResponse(call: Call, response: Response) {
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getPostResponse(imgHeart.context, responseStr)
                        if(responseObj.status == "success") {
                            productsList[p1].isFavorite = "0"
                            imgHeart.setColorFilter(ContextCompat.getColor(imgHeart.context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
                        }
                    }
                })
            }
        })
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