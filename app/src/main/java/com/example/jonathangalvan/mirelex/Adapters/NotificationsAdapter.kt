package com.example.jonathangalvan.mirelex.Adapters

import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.NotificationInterface
import com.example.jonathangalvan.mirelex.R

class NotificationsViewHolder(view: View): RecyclerView.ViewHolder(view) {
    var notificationAdapterText = view.findViewById<TextView>(R.id.notificationAdapterText)
    var notificationDate = view.findViewById<TextView>(R.id.notificationAdapterDate)
}

class NotificationsAdapter(private var notifications: ArrayList<NotificationInterface>): RecyclerView.Adapter<NotificationsViewHolder>() {

    fun getItem(position: Int): NotificationInterface{
        return notifications[position]
    }

    fun loadNewData(newOrders: ArrayList<NotificationInterface>){
        notifications = newOrders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NotificationsViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.adapter_notification, p0,false)
        return NotificationsViewHolder(view)
    }

    override fun onBindViewHolder(p0: NotificationsViewHolder, p1: Int) {
        if(notifications[p1].read != "1"){
            val boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD)
            p0.notificationAdapterText.typeface = boldTypeface
        }
        p0.notificationAdapterText.text = notifications[p1].message.toString()
        p0.notificationDate.text = notifications[p1].created.toString()

    }
}