package com.example.contact.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contact.R
import com.example.contact.model.MyContact


class ContactAdapter internal constructor(
    context: Context?,
    private val mData: List<MyContact>
) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    init {
        mInflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = mData[position]
        holder.tvContactName.text = contact.contactName
        holder.llMain.setOnClickListener{
            mClickListener?.onItemClick(contact)
        }
        holder.llMain.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(p0: View?): Boolean {
                mClickListener?.onItemLongClick(contact)
                return true
            }
        })
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView){
        var tvContactName: TextView
        var llMain: LinearLayout

        init {
            tvContactName = itemView.findViewById<TextView>(R.id.tvContactName)
            llMain = itemView.findViewById<LinearLayout>(R.id.llMain)
        }

    }

    // convenience method for getting data at click position
    fun getItem(id: Int): MyContact {
        return mData[id]
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(myContact: MyContact)
        fun onItemLongClick(myContact: MyContact)
    }
}