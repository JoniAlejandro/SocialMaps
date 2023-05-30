package com.example.socialmaps.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmaps.R
import com.example.socialmaps.models.Message


class MessageAdapter(private var messages: List<Message>): RecyclerView.Adapter<MessageAdapter.MessageHolder>()  {
    //var messages: List<Message> = messages
    inner class MessageHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        var tvMensaje: TextView
        var tvnombre: TextView
        init {
            tvMensaje = itemView.findViewById(R.id.tvMensaje)
            tvnombre = itemView.findViewById(R.id.tvNombre)
        }
    }
    fun setData(list: List<Message>){
        messages = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val mView = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageHolder(mView)
    }
    override fun onBindViewHolder(holder: MessageHolder, i: Int) {
        holder.tvnombre.text = messages[i].name
        holder.tvMensaje.text = messages[i].mensaje
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
