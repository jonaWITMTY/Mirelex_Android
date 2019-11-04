package com.example.jonathangalvan.mirelex.Adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.AddressListArrayInterface
import com.example.jonathangalvan.mirelex.Interfaces.AddressListInterface
import com.example.jonathangalvan.mirelex.R

class SelecListAdapterViewHolder(view: View): RecyclerView.ViewHolder(view){
    var addressAdapterName = view.findViewById<TextView>(R.id.addressAdapterName)
    var addressAdapterEmail = view.findViewById<TextView>(R.id.addressAdapterEmail)
    var addressAdapterPhone = view.findViewById<TextView>(R.id.addressAdapterPhone)
    var addressAdapterAddress = view.findViewById<TextView>(R.id.addressAdapterAddress)
}

class SelecListAddressAdapter(var list: ArrayList<AddressListInterface>): RecyclerView.Adapter<SelecListAdapterViewHolder>() {

    override fun onBindViewHolder(p0: SelecListAdapterViewHolder, p1: Int) {
        p0.addressAdapterName.text = list[p1].businessName.toString()
        p0.addressAdapterEmail.text = list[p1].email.toString()
        p0.addressAdapterPhone.text = list[p1].personalPhone.toString()
        p0.addressAdapterAddress.text = "${list[p1].street}, ${list[p1].neighborhood}, ${list[p1].postalCode}, ${list[p1].city}, ${list[p1].state}, ${list[p1].country}"
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SelecListAdapterViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.adapter_address, p0,false)
        return SelecListAdapterViewHolder(view)
    }

    fun loadNewData(newList: ArrayList<AddressListInterface>){
        list = newList
        notifyDataSetChanged()
    }
}