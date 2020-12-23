package com.example.jonathangalvan.mirelex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jonathangalvan.mirelex.Interfaces.ProductInterface
import com.example.jonathangalvan.mirelex.Interfaces.TransactionInterface
import com.example.jonathangalvan.mirelex.R

class TransactionViewHolder(view: View): RecyclerView.ViewHolder(view){
    val transactionDate = view.findViewById<TextView>(R.id.transactionAdapterDate)
    val transactionType = view.findViewById<TextView>(R.id.transactionAdapterType)
    val transactionAmount = view.findViewById<TextView>(R.id.transactionAdapterAmount)
    val transactionFolio = view.findViewById<TextView>(R.id.transactionAdapterFolio)
}

class TransactionsAdapter(private var transactionList: ArrayList<TransactionInterface>): RecyclerView.Adapter<TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adaper_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    fun loadNewData(newOrders: ArrayList<TransactionInterface>){
        transactionList = newOrders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.transactionDate.text = transactionList[position].date
        holder.transactionType.text = transactionList[position].transactionType
        holder.transactionAmount.text = transactionList[position].totalFormated
        holder.transactionFolio.text = transactionList[position].folio
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}