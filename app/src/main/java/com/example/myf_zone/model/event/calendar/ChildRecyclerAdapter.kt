package com.example.myf_zone.model.event.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myf_zone.R
import kotlinx.android.synthetic.main.list_event_item.view.*

class ChildRecyclerAdapter(private val items: MutableList<String>) :
    RecyclerView.Adapter<ChildRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_event_item, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.childTextView.text = items[position]
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textView: TextView = view.childTextView
    }
}
