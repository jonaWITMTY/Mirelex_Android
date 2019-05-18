package com.example.jonathangalvan.mirelex.Adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.ServiceInterface
import com.example.jonathangalvan.mirelex.R

class ServiceViewHolder(view: View): RecyclerView.ViewHolder(view){
    var serviceAdapterFolio = view.findViewById<TextView>(R.id.serviceAdapterFolio)
    var serviceAdapterStartDate = view.findViewById<TextView>(R.id.serviceAdapterStartDate)
    var serviceAdapterServiceType = view.findViewById<TextView>(R.id.serviceAdapterServiceType)
    var serviceAdapterStatus = view.findViewById<TextView>(R.id.serviceAdapterStatus)
    var serviceAdapterTotal = view.findViewById<TextView>(R.id.serviceAdapterTotal)
}

class ServicesAdapter( private var services: ArrayList<ServiceInterface>): RecyclerView.Adapter<ServiceViewHolder>() {
    override fun getItemCount(): Int {
        return if(services.isNotEmpty()) services.size else 0
    }

    fun loadNewData(newServices: ArrayList<ServiceInterface>){
        services = newServices
        notifyDataSetChanged()
    }

    fun getOrder(position: Int): ServiceInterface{
        return services[position]
    }

    override fun onBindViewHolder(p0: ServiceViewHolder, p1: Int) {
        p0.serviceAdapterFolio.text = services[p1].folio
        p0.serviceAdapterStartDate.text = services[p1].startDate
        p0.serviceAdapterServiceType.text = services[p1].orderType
        p0.serviceAdapterStatus.text = services[p1].orderStatus
        p0.serviceAdapterTotal.text = services[p1].totalFormatted
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ServiceViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.adapter_service_list_row, p0, false)
        return ServiceViewHolder(view)
    }
}