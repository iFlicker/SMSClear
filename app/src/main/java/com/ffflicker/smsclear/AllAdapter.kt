package com.ffflicker.smsclear

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

public class CAdapter constructor(recyclerView: RecyclerView) : RecyclerView.Adapter<CAdapter.CustomViewHolder>() {

    var mData: MutableList<String> = ArrayList()
    private var mContext: Context = recyclerView.context

    fun add(string: String) {
        mData.add(string)
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        mData.removeAt(position)
        notifyDataSetChanged()
    }

    fun clearData() {
        mData.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.layout_scan_item_key, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.textView.text = mData.get(position)
        holder.removeView.setOnClickListener { remove(position) }
    }


    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.item_key_text)
        var removeView: Button = itemView.findViewById(R.id.item_key_remove)
    }
}




public class RAdapter constructor(recyclerView: RecyclerView) : RecyclerView.Adapter<RAdapter.CustomViewHolder>() {

    var mData: MutableList<SMSModel> = ArrayList()
    private var mContext: Context = recyclerView.context

    public fun fillData(data: ArrayList<SMSModel>) {
        this.mData.addAll(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.layout_result_item_key, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.senderTextView.text = " " + mData[position].address
        holder.bodyTextView.text = mData[position].body
        holder.itemView.setOnClickListener{
            AlertDialog.Builder(holder.itemView.context)
                .setMessage(mData.get(position).body)
                .setTitle(mData.get(position).address)
                .create()
                .show()
        }
    }


    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var senderTextView: TextView = itemView.findViewById(R.id.item_sender_text)
        var bodyTextView: TextView = itemView.findViewById(R.id.item_body_text)
    }
}