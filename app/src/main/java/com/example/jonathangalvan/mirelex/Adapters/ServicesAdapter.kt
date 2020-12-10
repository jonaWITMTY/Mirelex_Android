package com.example.jonathangalvan.mirelex.Adapters

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Enums.OrderStatus
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Interfaces.ServiceInterface
import com.example.jonathangalvan.mirelex.R
import com.squareup.picasso.Picasso

class ServiceViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view){
    var serviceAdapterFeaturedImage = view.findViewById<ImageView>(R.id.serviceAdapterFeaturedImage)
    var serviceAdapterFolio = view.findViewById<TextView>(R.id.serviceAdapterFolio)
    var serviceAdapterStartDate = view.findViewById<TextView>(R.id.serviceAdapterStartDate)
    var serviceAdapterServiceType = view.findViewById<TextView>(R.id.serviceAdapterServiceType)
    var serviceAdapterStatus = view.findViewById<TextView>(R.id.serviceAdapterStatus)
    var serviceAdapterTotal = view.findViewById<TextView>(R.id.serviceAdapterTotal)
}

class ServicesAdapter( private var services: ArrayList<ServiceInterface>): androidx.recyclerview.widget.RecyclerView.Adapter<ServiceViewHolder>() {
    override fun getItemCount(): Int {
        return if(services.isNotEmpty()) services.size else 0
    }

    fun loadNewData(newServices: ArrayList<ServiceInterface>){
        services = newServices
        notifyDataSetChanged()
    }

    fun getService(position: Int): ServiceInterface{
        return services[position]
    }

    override fun onBindViewHolder(p0: ServiceViewHolder, p1: Int) {
        var drawableStr = ""

        p0.serviceAdapterFolio.text = services[p1].folio
        p0.serviceAdapterStartDate.text = services[p1].startDate
        p0.serviceAdapterServiceType.text = services[p1].orderType
        p0.serviceAdapterStatus.text = services[p1].orderStatus
        p0.serviceAdapterTotal.text = services[p1].totalFormatted

        when(services[p1].orderTypeId){
            OrderType.Cleaning.orderTypeId -> {
                drawableStr = p0.serviceAdapterFeaturedImage.context.resources.getString(R.string.cleaningImg)
                p0.serviceAdapterServiceType.backgroundTintList = ContextCompat.getColorStateList(p0.serviceAdapterServiceType.context, R.color.colorGoogleBlue)
            }
            OrderType.Sewing.orderTypeId -> {
                drawableStr = p0.serviceAdapterFeaturedImage.context.resources.getString(R.string.sewingImg)
                p0.serviceAdapterServiceType.backgroundTintList = ContextCompat.getColorStateList(p0.serviceAdapterServiceType.context, R.color.colorFuchsia)
            }
        }

        Picasso.with(p0.serviceAdapterFeaturedImage.context).load(drawableStr).into(p0.serviceAdapterFeaturedImage)

        when(services[p1].orderStatusId){
            OrderStatus.Open.orderStatusId -> {
                p0.serviceAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.serviceAdapterStatus.context, R.color.colorGreenLight)
            }
            OrderStatus.Gathering.orderStatusId -> {
                p0.serviceAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.serviceAdapterStatus.context, android.R.color.black)
            }
            OrderStatus.Processing.orderStatusId -> {
                p0.serviceAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.serviceAdapterStatus.context, R.color.colorBlueLight)
            }
            OrderStatus.DeliveringProcess.orderStatusId -> {
                p0.serviceAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.serviceAdapterStatus.context, android.R.color.black)
            }
            OrderStatus.Delivered.orderStatusId -> {
                p0.serviceAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.serviceAdapterStatus.context, android.R.color.black)
            }
            OrderStatus.Received.orderStatusId -> {
                p0.serviceAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.serviceAdapterStatus.context, android.R.color.black)
            }
            OrderStatus.Finished.orderStatusId -> {
                p0.serviceAdapterStatus.backgroundTintList = ContextCompat.getColorStateList(p0.serviceAdapterStatus.context, android.R.color.black)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ServiceViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.adapter_service_list_row, p0, false)
        return ServiceViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}