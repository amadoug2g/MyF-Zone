package com.example.myf_zone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChildRecyclerAdapter(private val items: MutableList<String>) :
    RecyclerView.Adapter<ChildRecyclerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var textView: TextView = view.findViewById(R.id.childTextView)
        var recyclerView = view.findViewById<RecyclerView>(R.id.eventRecyclerView)

//        var title: TextView = view.findViewById(R.id.title)
//        var year: TextView = view.findViewById(R.id.year)
//        var genre: TextView = view.findViewById(R.id.genre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.event_section_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = items[position]
    }

}