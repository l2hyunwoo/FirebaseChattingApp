package com.example.firebasechatapp

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val img_message = itemView.findViewById<ImageView>(R.id.photoImageView)
    val tv_message = itemView.findViewById<TextView>(R.id.messageTextView)
    val tv_name = itemView.findViewById<TextView>(R.id.nameTextView)

    fun bind(messageData: MessageData) {
        val isPhoto = messageData.photoUrl.isNullOrBlank()
        if(isPhoto) {
            tv_message.visibility = View.GONE
            img_message.visibility = View.VISIBLE
            Glide.with(itemView).load(messageData.photoUrl).into(img_message)
        }
        else {
            tv_message.visibility = View.VISIBLE
            img_message.visibility = View.GONE
            tv_message.text = messageData.text
        }
        tv_name.text = messageData.name
    }

}