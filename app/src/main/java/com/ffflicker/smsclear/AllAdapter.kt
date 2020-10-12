package com.ffflicker.smsclear

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


class CAdapter constructor(recyclerView: RecyclerView) : RecyclerView.Adapter<CAdapter.CustomViewHolder>() {
    var mData: MutableList<String> = ArrayList()
    private var mContext: Context = recyclerView.context
    val mScreenWidth = mContext.resources.displayMetrics.widthPixels

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
        holder.textView.width = (mScreenWidth / 3 - App.dp2px(mContext, 30f)).roundToInt()
        holder.removeView.setOnClickListener { remove(position) }
    }


    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.item_key_text)
        var removeView: ImageView = itemView.findViewById(R.id.item_key_remove)
    }
}




class RAdapter constructor(recyclerView: RecyclerView) : RecyclerView.Adapter<RAdapter.CustomViewHolder>() {

    var mData: MutableList<SMSModel> = ArrayList()
    var mSelectedPositions = SparseBooleanArray()
    private var mContext: Context = recyclerView.context
    private val mDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private fun setItemChecked(position: Int, isChecked: Boolean) {
        mSelectedPositions.put(position, isChecked)
    }

    public fun isItemChecked(position: Int): Boolean {
        return mSelectedPositions[position]
    }

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
        holder.dateTextView.text = mDateFormat.format(Date(mData[position].date))
        holder.itemView.setOnClickListener{
            AlertDialog.Builder(holder.itemView.context)
                .setMessage(mData[position].body)
                .setTitle(mData[position].address)
                .create()
                .show()
        }
        holder.checkBox.isChecked = true
        setItemChecked(position, true)
        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
                setItemChecked(position, isChecked)
                mSelectCallBack?.OnSelectItemChanged()
            }
        }
    }

    var mSelectCallBack: SelectCallBack? = null

    public fun setSelectCallBack(selectCallBack: SelectCallBack) {
        this.mSelectCallBack = selectCallBack
    }



    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var senderTextView: TextView = itemView.findViewById(R.id.item_sender_text)
        var bodyTextView: TextView = itemView.findViewById(R.id.item_body_text)
        var dateTextView: TextView = itemView.findViewById(R.id.item_sender_date)
        var checkBox: CheckBox = itemView.findViewById(R.id.item_checkbox)
    }

    interface SelectCallBack {
        fun OnSelectItemChanged()
    }

}