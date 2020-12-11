package com.example.jonathangalvan.mirelex.Adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.PaymentCard
import com.example.jonathangalvan.mirelex.R

class PaymenrCardsViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view){
    var paymentCardAdapterCardNumber = view.findViewById<TextView>(R.id.paymentCardAdapterCardNumber)
    var paymentCardAdapterDate = view.findViewById<TextView>(R.id.paymentCardAdapterDate)
    var paymentCardAdapterDefault = view.findViewById<TextView>(R.id.paymentCardAdapterIsDefault)
}

class PaymentCardsAdapter(private var context: Context, private var paymentCards: ArrayList<PaymentCard>): androidx.recyclerview.widget.RecyclerView.Adapter<PaymenrCardsViewHolder>() {

    fun getItem(position: Int): PaymentCard {
        return paymentCards[position]
    }

    fun loadPaymentCards(newPaymentCards: ArrayList<PaymentCard>) {
        paymentCards = newPaymentCards
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return paymentCards.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PaymenrCardsViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.adapter_payment_card, p0, false)
        return PaymenrCardsViewHolder(view)
    }

    override fun onBindViewHolder(p0: PaymenrCardsViewHolder, p1: Int) {
        p0.paymentCardAdapterCardNumber.text = "•••• •••• •••• ${paymentCards[p1].lastDigits}"
        p0.paymentCardAdapterDate.text = "${paymentCards[p1].expireMonth}/${paymentCards[p1].expireYear}"
        p0.paymentCardAdapterDefault.text =
            if (paymentCards[p1].default == "1") context.resources.getString(R.string.principal) else ""
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
