package com.example.jonathangalvan.mirelex.Adapters

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.OrderInterface
import com.example.jonathangalvan.mirelex.Enums.OrderStatus
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.R
import com.squareup.picasso.Picasso

class OrderViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view){
    var orderAdapterBrand = view.findViewById<TextView>(R.id.orderAdapterBrand)
    var orderAdapterFolio = view.findViewById<TextView>(R.id.orderAdapterFolio)
    var orderAdapterPrice = view.findViewById<TextView>(R.id.orderAdapterPrice)
    var orderAdapterFeaturedImage = view.findViewById<ImageView>(R.id.orderAdapterFeaturedImage)
    var orderAdapterOrderType = view.findViewById<TextView>(R.id.orderAdapterOrderType)
    var orderAdapterStartDate = view.findViewById<TextView>(R.id.orderAdapterStartDate)
    var orderAdapterStatus = view.findViewById<TextView>(R.id.orderAdapterStatus)
}

class OrdersAdapter(private var orders: ArrayList<OrderInterface>): androidx.recyclerview.widget.RecyclerView.Adapter<OrderViewHolder>(){
    override fun getItemCount(): Int {
        return if(orders.isNotEmpty()) orders.size else 0
    }

    fun loadNewData(newOrders: ArrayList<OrderInterface>){
        orders = newOrders
        notifyDataSetChanged()
    }

    fun getOrder(position: Int): OrderInterface{
        return orders[position]
    }

    override fun onBindViewHolder(p0: OrderViewHolder, p1: Int) {
        if(orders[p1].product.productFeaturedImage != null){
            Picasso.with(p0.orderAdapterFeaturedImage.context).load(orders[p1].product.productFeaturedImage).resize(300, 0).into(p0.orderAdapterFeaturedImage)
        }
        p0.orderAdapterBrand.text = orders[p1].product.brand
        p0.orderAdapterFolio.text = orders[p1].folio
        p0.orderAdapterPrice.text = orders[p1].totalFormatted
        p0.orderAdapterOrderType.text = orders[p1].orderType
        p0.orderAdapterStartDate.text = orders[p1].startDate
        p0.orderAdapterStatus.text = orders[p1].orderStatus

        when(orders[p1].orderTypeId){
            OrderType.Lease.orderTypeId ->{
                p0.orderAdapterOrderType.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterOrderType.context, R.color.colorEmerald)
            }
            OrderType.Purchase.orderTypeId ->{
                p0.orderAdapterOrderType.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterOrderType.context, R.color.colorYellow)
                p0.orderAdapterOrderType.setTextColor(ContextCompat.getColorStateList(p0.orderAdapterOrderType.context, R.color.black))
            }
            OrderType.Fitting.orderTypeId ->{
                p0.orderAdapterOrderType.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterOrderType.context, R.color.colorLilac)
            }
        }

        when(orders[p1].orderStatusId){
            OrderStatus.AcceptDate.orderStatusId -> {
                p0.orderAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterStatus.context, R.color.colorGreenLight)
            }
            OrderStatus.YesItCould.orderStatusId -> {
                p0.orderAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterStatus.context, android.R.color.black)
            }
            OrderStatus.RentInCourse.orderStatusId -> {
                p0.orderAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterStatus.context, R.color.colorBlueLight)
            }
            OrderStatus.ReadyInStore.orderStatusId -> {
                p0.orderAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterStatus.context, android.R.color.black)
            }
            OrderStatus.IsInStore.orderStatusId -> {
                p0.orderAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterStatus.context, android.R.color.black)
            }
            OrderStatus.FitiingDone.orderStatusId -> {
                p0.orderAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterStatus.context, android.R.color.black)
            }
            OrderStatus.SeeYouSoon.orderStatusId -> {
                p0.orderAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.orderAdapterStatus.context, android.R.color.black)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): OrderViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.adapter_order_list_row, p0,false)
        return OrderViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}