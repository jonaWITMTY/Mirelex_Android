package com.example.jonathangalvan.mirelex.Adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.OrderUpdates
import com.example.jonathangalvan.mirelex.R

class OrderStatusViewHolder(view: View): RecyclerView.ViewHolder(view){
    val name = view.findViewById<TextView>(R.id.orderStatusListAdapterName)
}

class OrderStatusAdapater(var orderStatus: ArrayList<OrderUpdates>): RecyclerView.Adapter<OrderStatusViewHolder>() {

    fun loadNewData(orderStatusNew: ArrayList<OrderUpdates>){
        orderStatus = orderStatusNew
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): OrderStatusViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.adapter_order_status_list, p0, false)
        return OrderStatusViewHolder(v)
    }

    override fun getItemCount(): Int {
        return orderStatus.size
    }

    override fun onBindViewHolder(p0: OrderStatusViewHolder, p1: Int) {
        p0.name.text = orderStatus[p1].newStatus
    }
}