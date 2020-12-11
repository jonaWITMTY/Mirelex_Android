package com.example.jonathangalvan.mirelex.Adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.ConversationInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.R

class ConversationViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view){
    var conversationViewSendTo = view.findViewById<TextView>(R.id.conversationViewSendTo)
    var conversationViewLastMessage = view.findViewById<TextView>(R.id.conversationViewLastMessage)
}

class ConversationsAdapter(var context: Context, private var conversations: ArrayList<ConversationInterface>): androidx.recyclerview.widget.RecyclerView.Adapter<ConversationViewHolder>() {

    fun loadNewData(newObj: ArrayList<ConversationInterface>){
        conversations = newObj
        notifyDataSetChanged()
    }

    fun getItem(position: Int): ConversationInterface{
        return conversations[position]
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ConversationViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.adapter_conversation, p0,false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(p0: ConversationViewHolder, p1: Int) {
        p0.conversationViewSendTo.text = if(SessionModel(context).getUser().person?.userId == conversations[p1].userIdTo) conversations[p1].userFrom else conversations[p1].userTo
        p0.conversationViewLastMessage.text = conversations[p1].message.toString()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}